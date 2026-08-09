package net.sanctuaryblock.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.sanctuaryblock.block.SanctuaryBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class SanctuaryItemPlaceMixin {

	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
	private void sanctuaryblock$onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		if (player != null && !player.isCreative() && SanctuaryBlock.isBlockPlacementBlocked(world, context.getBlockPos())) {
			if (world.isClient()) {
				player.sendMessage(Text.literal("This area is protected by a Sanctuary Block. You cannot place blocks here.")
						.formatted(Formatting.RED), true);
			}
			cir.setReturnValue(ActionResult.FAIL);
		}
	}
}
