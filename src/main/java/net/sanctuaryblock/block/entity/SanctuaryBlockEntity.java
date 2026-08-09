package net.sanctuaryblock.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sanctuaryblock.block.SanctuaryBlock;

public class SanctuaryBlockEntity extends BlockEntity {

	public boolean active = true;
	public boolean explosionProtection = true;
	public boolean blockPlacement = true;
	public boolean blockBreaking = true;
	public boolean mobSpawning = true;
	public int horizontalRadius = 50;
	public int verticalRadius = 50;

	public SanctuaryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SANCTUARY_BE, pos, state);
	}

	public void fromData(SanctuaryBlock.SanctuaryData data) {
		this.active = data.active;
		this.explosionProtection = data.explosionProtection;
		this.blockPlacement = data.blockPlacement;
		this.blockBreaking = data.blockBreaking;
		this.mobSpawning = data.mobSpawning;
		this.horizontalRadius = data.horizontalRadius;
		this.verticalRadius = data.verticalRadius;
		this.markDirty();
	}

	public SanctuaryBlock.SanctuaryData toData() {
		return new SanctuaryBlock.SanctuaryData(active, explosionProtection, blockPlacement, blockBreaking,
				mobSpawning, horizontalRadius, verticalRadius);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putBoolean("Active", active);
		nbt.putBoolean("ExplosionProtection", explosionProtection);
		nbt.putBoolean("BlockPlacement", blockPlacement);
		nbt.putBoolean("BlockBreaking", blockBreaking);
		nbt.putBoolean("MobSpawning", mobSpawning);
		nbt.putInt("HorizontalRadius", horizontalRadius);
		nbt.putInt("VerticalRadius", verticalRadius);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		if (nbt.contains("Active")) active = nbt.getBoolean("Active");
		if (nbt.contains("ExplosionProtection")) explosionProtection = nbt.getBoolean("ExplosionProtection");
		if (nbt.contains("BlockPlacement")) blockPlacement = nbt.getBoolean("BlockPlacement");
		if (nbt.contains("BlockBreaking")) blockBreaking = nbt.getBoolean("BlockBreaking");
		if (nbt.contains("MobSpawning")) mobSpawning = nbt.getBoolean("MobSpawning");
		if (nbt.contains("HorizontalRadius")) horizontalRadius = nbt.getInt("HorizontalRadius");
		if (nbt.contains("VerticalRadius")) verticalRadius = nbt.getInt("VerticalRadius");
	}

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		if (world != null && !world.isClient()) {
			SanctuaryBlock.SanctuaryData data = SanctuaryBlock.getSanctuaryData(this.getPos());
			data.active = active;
			data.explosionProtection = explosionProtection;
			data.blockPlacement = blockPlacement;
			data.blockBreaking = blockBreaking;
			data.mobSpawning = mobSpawning;
			data.horizontalRadius = horizontalRadius;
			data.verticalRadius = verticalRadius;
			try {
				data.dimensionKey = world.getRegistryKey().getValue().toString();
			} catch (Throwable t) {
				data.dimensionKey = "unknown";
			}
		}
	}
}
