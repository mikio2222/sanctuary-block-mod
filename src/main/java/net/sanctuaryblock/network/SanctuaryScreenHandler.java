package net.sanctuaryblock.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SanctuaryScreenHandler extends ScreenHandler {

	public static ScreenHandlerType<SanctuaryScreenHandler> SANCTUARY_SCREEN_HANDLER;

	private final BlockPos sanctuaryPos;

	public SanctuaryScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, BlockPos.ORIGIN);
	}

	public SanctuaryScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
		super(SANCTUARY_SCREEN_HANDLER, syncId);
		this.sanctuaryPos = pos;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.isCreative();
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	public BlockPos getSanctuaryPos() {
		return sanctuaryPos;
	}

	public static void register() {
		SANCTUARY_SCREEN_HANDLER = Registry.register(
				Registries.SCREEN_HANDLER,
				Identifier.of("sanctuaryblock", "sanctuary"),
				new ScreenHandlerType<>(SanctuaryScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES)
		);
	}
}
