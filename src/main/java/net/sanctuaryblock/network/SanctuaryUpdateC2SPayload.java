package net.sanctuaryblock.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sanctuaryblock.block.SanctuaryBlock;
import net.sanctuaryblock.block.entity.SanctuaryBlockEntity;

/**
 * The 5 boolean toggles are packed into a single byte (bit flags) so that the whole
 * payload only needs 4 fields total, well within PacketCodec.tuple's 6-field limit.
 */
public record SanctuaryUpdateC2SPayload(BlockPos pos, byte flags, int horizontalRadius, int verticalRadius) implements CustomPayload {

	private static final int FLAG_ACTIVE = 1;
	private static final int FLAG_EXPLOSION = 1 << 1;
	private static final int FLAG_PLACEMENT = 1 << 2;
	private static final int FLAG_BREAKING = 1 << 3;
	private static final int FLAG_MOB_SPAWNING = 1 << 4;

	public static final CustomPayload.Id<SanctuaryUpdateC2SPayload> ID =
			new CustomPayload.Id<>(Identifier.of("sanctuaryblock", "sanctuary_update"));

	public static final PacketCodec<RegistryByteBuf, SanctuaryUpdateC2SPayload> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, SanctuaryUpdateC2SPayload::pos,
			PacketCodecs.BYTE, SanctuaryUpdateC2SPayload::flags,
			PacketCodecs.VAR_INT, SanctuaryUpdateC2SPayload::horizontalRadius,
			PacketCodecs.VAR_INT, SanctuaryUpdateC2SPayload::verticalRadius,
			SanctuaryUpdateC2SPayload::new
	);

	public SanctuaryUpdateC2SPayload(BlockPos pos, boolean active, boolean explosionProtection, boolean blockPlacement,
									  boolean blockBreaking, boolean mobSpawning, int horizontalRadius, int verticalRadius) {
		this(pos, packFlags(active, explosionProtection, blockPlacement, blockBreaking, mobSpawning), horizontalRadius, verticalRadius);
	}

	private static byte packFlags(boolean active, boolean explosionProtection, boolean blockPlacement,
								   boolean blockBreaking, boolean mobSpawning) {
		int value = 0;
		if (active) value |= FLAG_ACTIVE;
		if (explosionProtection) value |= FLAG_EXPLOSION;
		if (blockPlacement) value |= FLAG_PLACEMENT;
		if (blockBreaking) value |= FLAG_BREAKING;
		if (mobSpawning) value |= FLAG_MOB_SPAWNING;
		return (byte) value;
	}

	public boolean active() {
		return (flags & FLAG_ACTIVE) != 0;
	}

	public boolean explosionProtection() {
		return (flags & FLAG_EXPLOSION) != 0;
	}

	public boolean blockPlacement() {
		return (flags & FLAG_PLACEMENT) != 0;
	}

	public boolean blockBreaking() {
		return (flags & FLAG_BREAKING) != 0;
	}

	public boolean mobSpawning() {
		return (flags & FLAG_MOB_SPAWNING) != 0;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> context.server().execute(() -> {
			if (!context.player().isCreative()) {
				return;
			}
			World world = context.player().getWorld();
			int h = Math.max(1, Math.min(100, payload.horizontalRadius()));
			int v = Math.max(1, Math.min(100, payload.verticalRadius()));

			BlockEntity be = world.getBlockEntity(payload.pos());
			if (be instanceof SanctuaryBlockEntity sbe) {
				SanctuaryBlock.SanctuaryData data = new SanctuaryBlock.SanctuaryData(
						payload.active(), payload.explosionProtection(), payload.blockPlacement(),
						payload.blockBreaking(), payload.mobSpawning(), h, v);
				sbe.fromData(data);
			}

			SanctuaryBlock.updateSanctuaryDataWithWorld(world, payload.pos(), payload.active(),
					payload.explosionProtection(), payload.blockPlacement(), payload.blockBreaking(),
					payload.mobSpawning(), h, v);
		}));
	}
}
