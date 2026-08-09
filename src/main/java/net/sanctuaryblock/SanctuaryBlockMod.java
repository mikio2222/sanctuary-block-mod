package net.sanctuaryblock;

import net.fabricmc.api.ModInitializer;
import net.sanctuaryblock.block.ModBlocks;
import net.sanctuaryblock.block.entity.ModBlockEntities;
import net.sanctuaryblock.network.SanctuaryOpenScreenS2CPayload;
import net.sanctuaryblock.network.SanctuaryScreenHandler;
import net.sanctuaryblock.network.SanctuaryUpdateC2SPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SanctuaryBlockMod implements ModInitializer {

	public static final String MOD_ID = "sanctuaryblock";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Sanctuary Block");

		ModBlocks.initialize();
		ModBlockEntities.initialize();

		SanctuaryScreenHandler.register();
		SanctuaryUpdateC2SPayload.register();
		SanctuaryOpenScreenS2CPayload.registerPayloadType();
	}
}
