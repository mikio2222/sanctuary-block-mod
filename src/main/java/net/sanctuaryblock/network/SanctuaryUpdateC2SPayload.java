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

public record SanctuaryUpdateC2SPayload(BlockPos pos, boolean active, boolean explosionProtection,
										  boolean blockPlacement, boolean blockBreaking, boolean mobSpawning,
										  int horizontalRadius, int verticalRadius) implements CustomPayload {

	public static final CustomPayload.Id<SanctuaryUpdateC2SPayload> ID =
			new CustomPayload.Id<>(Identifier.of("sanctuaryblock", "sanctuary_update"));

	public static final PacketCodec<RegistryByteBuf, SanctuaryUpdateC2SPayload> CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, SanctuaryUpdateC2SPayload::pos,
			PacketCodecs.BOOLEAN, SanctuaryUpdateC2SPayload::active,
			PacketCodecs.BOOLEAN, SanctuaryUpdateC2SPayload::explosionProtection,
			PacketCodecs.BOOLEAN, SanctuaryUpdateC2SPayload::blockPlacement,
			PacketCodecs.BOOLEAN, SanctuaryUpdateC2SPayload::blockBreaking,
			PacketCodecs.BOOLEAN, SanctuaryUpdateC2SPayload::mobSpawning,
			PacketCodecs.VAR_INT, SanctuaryUpdateC2SPayload::horizontalRadius,
			PacketCodecs.VAR_INT, SanctuaryUpdateC2SPayload::verticalRadius,
			SanctuaryUpdateC2SPayload::new
	);

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
