package net.sanctuaryblock.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sanctuaryblock.block.entity.SanctuaryBlockEntity;
import net.sanctuaryblock.network.SanctuaryOpenScreenS2CPayload;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Standalone reimplementation of the "Sanctuary Block" feature originally found in
 * "Cobblemon: Legendary Monuments" by JorgaoMC (MPL 2.0). This is a 2-tall, placeable
 * block that lets you protect a configurable radius around it from explosions, block
 * placement, block breaking and mob spawning. Toggled on/off with a simple right click
 * in survival, or configured through a dedicated screen while in creative mode.
 */
public class SanctuaryBlock extends Block {

	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	private static final int DEFAULT_RADIUS = 50;

	// Fast in-memory lookup cache, keyed by the position of the LOWER half of the block.
	private static final Map<BlockPos, SanctuaryData> SANCTUARY_DATA = new HashMap<>();

	public SanctuaryBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState()
				.with(HALF, DoubleBlockHalf.LOWER)
				.with(FACING, Direction.NORTH)
				.with(ACTIVE, true));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HALF, FACING, ACTIVE);
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		// The sanctuary block has no collision box: players and mobs simply walk through it.
		return VoxelShapes.empty();
	}

	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			return new SanctuaryBlockEntity(pos, state);
		}
		return null;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		if (pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx)) {
			return this.getDefaultState()
					.with(FACING, ctx.getHorizontalPlayerFacing())
					.with(HALF, DoubleBlockHalf.LOWER)
					.with(ACTIVE, true);
		}
		return null;
	}

	@Override
	protected void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		BlockPos upperPos = pos.up();
		world.setBlockState(upperPos, state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);

		if (!world.isClient() && state.get(HALF) == DoubleBlockHalf.LOWER) {
			SanctuaryData data = new SanctuaryData();
			data.dimensionKey = worldKey(world);

			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof SanctuaryBlockEntity sbe) {
				sbe.fromData(data);
			}
			SANCTUARY_DATA.put(pos, data);

			if (world instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.ENCHANT,
						pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
						50, data.horizontalRadius / 2.0, 0.5, data.horizontalRadius / 2.0, 0.02);
			}
		}
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(HALF) != DoubleBlockHalf.LOWER) {
			BlockState below = world.getBlockState(pos.down());
			return below.isOf(this) && below.get(HALF) == DoubleBlockHalf.LOWER;
		}
		return super.canPlaceAt(state, world, pos);
	}

	@Override
	protected BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		DoubleBlockHalf half = state.get(HALF);
		BlockPos otherHalfPos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
		BlockState otherHalfState = world.getBlockState(otherHalfPos);

		if (otherHalfState.isOf(this) && otherHalfState.get(HALF) != half) {
			world.setBlockState(otherHalfPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
			world.syncWorldEvent(player, 2001, otherHalfPos, Block.getRawIdFromState(otherHalfState));
		}

		if (!world.isClient()) {
			BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.down();
			SANCTUARY_DATA.remove(lowerPos);
		}

		return super.onBreak(world, pos, state, player);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
		BlockState lowerState = world.getBlockState(lowerPos);
		BlockPos upperPos = lowerPos.up();

		if (player.isCreative()) {
			if (player instanceof ServerPlayerEntity serverPlayer) {
				SanctuaryOpenScreenS2CPayload.send(serverPlayer, lowerPos);
			}
		} else {
			SanctuaryData data = SANCTUARY_DATA.get(lowerPos);
			if (data == null) {
				data = new SanctuaryData();
				data.dimensionKey = worldKey(world);
				SANCTUARY_DATA.put(lowerPos, data);
			} else if (data.dimensionKey == null) {
				data.dimensionKey = worldKey(world);
			}

			boolean newActive = !data.active;
			data.active = newActive;

			BlockEntity be = world.getBlockEntity(lowerPos);
			if (be instanceof SanctuaryBlockEntity sbe) {
				sbe.fromData(data);
			}

			world.setBlockState(lowerPos, lowerState.with(ACTIVE, newActive), Block.NOTIFY_ALL);
			world.setBlockState(upperPos, world.getBlockState(upperPos).with(ACTIVE, newActive), Block.NOTIFY_ALL);

			if (world instanceof ServerWorld serverWorld) {
				if (newActive) {
					serverWorld.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.0f);
					serverWorld.spawnParticles(ParticleTypes.ENCHANT,
							pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							50, data.horizontalRadius / 2.0, 1.0, data.horizontalRadius / 2.0, 0.02);
					player.sendMessage(Text.literal("The Sanctuary has been activated. Area is now protected.")
							.formatted(Formatting.GREEN), true);
				} else {
					serverWorld.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.0f);
					serverWorld.spawnParticles(ParticleTypes.SMOKE,
							pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							25, data.horizontalRadius / 4.0, 0.5, data.horizontalRadius / 4.0, 0.05);
					player.sendMessage(Text.literal("The Sanctuary has been deactivated. Area protection removed.")
							.formatted(Formatting.GRAY), true);
				}
			}
		}

		return ActionResult.SUCCESS;
	}

	// ---------------------------------------------------------------------
	// Static protection-lookup API, called from the mixins.
	// ---------------------------------------------------------------------

	public static SanctuaryData getSanctuaryData(BlockPos pos) {
		return SANCTUARY_DATA.computeIfAbsent(pos, p -> new SanctuaryData());
	}

	public static void updateSanctuaryData(BlockPos pos, boolean active, boolean explosionProtection, boolean blockPlacement,
											boolean blockBreaking, boolean mobSpawning, int horizontalRadius, int verticalRadius) {
		SanctuaryData data = SANCTUARY_DATA.get(pos);
		if (data == null) {
			return;
		}
		data.active = active;
		data.explosionProtection = explosionProtection;
		data.blockPlacement = blockPlacement;
		data.blockBreaking = blockBreaking;
		data.mobSpawning = mobSpawning;
		data.horizontalRadius = Math.max(1, Math.min(100, horizontalRadius));
		data.verticalRadius = Math.max(1, Math.min(100, verticalRadius));
	}

	public static void updateSanctuaryDataWithWorld(World world, BlockPos pos, boolean active, boolean explosionProtection,
													  boolean blockPlacement, boolean blockBreaking, boolean mobSpawning,
													  int horizontalRadius, int verticalRadius) {
		SanctuaryData data = SANCTUARY_DATA.computeIfAbsent(pos, p -> new SanctuaryData());
		data.active = active;
		data.explosionProtection = explosionProtection;
		data.blockPlacement = blockPlacement;
		data.blockBreaking = blockBreaking;
		data.mobSpawning = mobSpawning;
		data.horizontalRadius = Math.max(1, Math.min(100, horizontalRadius));
		data.verticalRadius = Math.max(1, Math.min(100, verticalRadius));
		data.dimensionKey = worldKey(world);

		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof SanctuaryBlockEntity sbe) {
			sbe.fromData(data);
		}

		if (!world.isClient()) {
			BlockState lowerState = world.getBlockState(pos);
			BlockState upperState = world.getBlockState(pos.up());
			if (lowerState.getBlock() instanceof SanctuaryBlock && lowerState.get(HALF) == DoubleBlockHalf.LOWER) {
				world.setBlockState(pos, lowerState.with(ACTIVE, active), Block.NOTIFY_ALL);
			}
			if (upperState.getBlock() instanceof SanctuaryBlock && upperState.get(HALF) == DoubleBlockHalf.UPPER) {
				world.setBlockState(pos.up(), upperState.with(ACTIVE, active), Block.NOTIFY_ALL);
			}
		}
	}

	public static boolean isPositionProtected(World world, BlockPos pos) {
		return checkFlag(world, pos, null);
	}

	public static boolean isBlockPlacementBlocked(World world, BlockPos pos) {
		return checkFlag(world, pos, d -> d.blockPlacement);
	}

	public static boolean isBlockBreakingBlocked(World world, BlockPos pos) {
		return checkFlag(world, pos, d -> d.blockBreaking);
	}

	public static boolean isExplosionBlocked(World world, BlockPos pos) {
		return checkFlag(world, pos, d -> d.explosionProtection);
	}

	public static boolean isMobSpawningBlocked(World world, BlockPos pos) {
		return checkFlag(world, pos, d -> d.mobSpawning);
	}

	private interface FlagCheck {
		boolean test(SanctuaryData data);
	}

	private static boolean checkFlag(World world, BlockPos pos, FlagCheck flagCheck) {
		if (world.isClient()) {
			return false;
		}
		String dim = worldKey(world);
		Iterator<Map.Entry<BlockPos, SanctuaryData>> it = SANCTUARY_DATA.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BlockPos, SanctuaryData> entry = it.next();
			BlockPos sanctuaryPos = entry.getKey();
			SanctuaryData data = entry.getValue();

			if (!isValidSanctuaryBlock(world, sanctuaryPos)) {
				it.remove();
				continue;
			}
			if (!data.active) {
				continue;
			}
			if (flagCheck != null && !flagCheck.test(data)) {
				continue;
			}
			if (data.dimensionKey != null && !dim.equals(data.dimensionKey)) {
				continue;
			}
			if (!isWithinRadius(pos, sanctuaryPos, data.horizontalRadius, data.verticalRadius)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private static boolean isWithinRadius(BlockPos pos, BlockPos sanctuaryPos, int horizontalRadius, int verticalRadius) {
		int dx = pos.getX() - sanctuaryPos.getX();
		int dy = pos.getY() - sanctuaryPos.getY();
		int dz = pos.getZ() - sanctuaryPos.getZ();
		int horizontalDistanceSquared = dx * dx + dz * dz;
		int verticalDistance = Math.abs(dy);
		return horizontalDistanceSquared <= horizontalRadius * horizontalRadius && verticalDistance <= verticalRadius;
	}

	private static boolean isValidSanctuaryBlock(World world, BlockPos lowerPos) {
		if (world.isClient()) {
			return true;
		}
		BlockState state = world.getBlockState(lowerPos);
		if (!(state.getBlock() instanceof SanctuaryBlock)) {
			return false;
		}
		if (!state.contains(HALF)) {
			return false;
		}
		return state.get(HALF) == DoubleBlockHalf.LOWER;
	}

	private static String worldKey(World world) {
		try {
			return world.getRegistryKey().getValue().toString();
		} catch (Throwable t) {
			return "unknown";
		}
	}

	/**
	 * Runtime configuration for a single Sanctuary Block instance.
	 */
	public static class SanctuaryData {
		public boolean active = true;
		public boolean explosionProtection = true;
		public boolean blockPlacement = true;
		public boolean blockBreaking = true;
		public boolean mobSpawning = true;
		public int horizontalRadius = DEFAULT_RADIUS;
		public int verticalRadius = DEFAULT_RADIUS;
		public String dimensionKey = null;

		public SanctuaryData() {
		}

		public SanctuaryData(boolean active, boolean explosionProtection, boolean blockPlacement, boolean blockBreaking,
							  boolean mobSpawning, int horizontalRadius, int verticalRadius) {
			this.active = active;
			this.explosionProtection = explosionProtection;
			this.blockPlacement = blockPlacement;
			this.blockBreaking = blockBreaking;
			this.mobSpawning = mobSpawning;
			this.horizontalRadius = horizontalRadius;
			this.verticalRadius = verticalRadius;
		}
	}
}
