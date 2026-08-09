package net.sanctuaryblock.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SanctuaryOpenScreenS2CPayload(BlockPos pos) implements CustomPayload {

	public static final CustomPayload.Id<SanctuaryOpenScreenS2CPayload> ID =
			new CustomPayload.Id<>(Identifier.of("sanctuaryblock", "sanctuary_open_screen"));

	public static final PacketCodec<RegistryByteBuf, SanctuaryOpenScreenS2CPayload> CODEC =
			PacketCodec.tuple(BlockPos.PACKET_CODEC, SanctuaryOpenScreenS2CPayload::pos, SanctuaryOpenScreenS2CPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void registerPayloadType() {
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}

	public static void send(ServerPlayerEntity player, BlockPos pos) {
		ServerPlayNetworking.send(player, new SanctuaryOpenScreenS2CPayload(pos));
	}
}
