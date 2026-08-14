package net.sanctuaryblock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.sanctuaryblock.block.ModBlocks;
import net.sanctuaryblock.network.SanctuaryOpenScreenS2CPayload;
import net.sanctuaryblock.network.SanctuaryScreenHandler;
import net.sanctuaryblock.screen.SanctuaryScreen;

public class SanctuaryBlockModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		HandledScreens.register(SanctuaryScreenHandler.SANCTUARY_SCREEN_HANDLER, SanctuaryScreen::new);

		// The block has custom transparent geometry (the crystal shards), so it needs
		// to render on the cutout/translucent layers rather than the default solid one.
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SANCTUARY_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SANCTUARY_BLOCK, RenderLayer.getTranslucent());

		ClientPlayNetworking.registerGlobalReceiver(SanctuaryOpenScreenS2CPayload.ID, (payload, context) ->
				context.client().execute(() -> {
					MinecraftClient client = context.client();
					if (client.player == null) {
						return;
					}
					SanctuaryScreenHandler handler = new SanctuaryScreenHandler(0, client.player.getInventory(), payload.pos());
					client.setScreen(new SanctuaryScreen(handler, client.player.getInventory(), Text.literal("Sanctuary Configuration")));
				}));
	}
}
