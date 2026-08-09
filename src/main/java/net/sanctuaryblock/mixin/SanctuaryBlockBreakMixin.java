package net.sanctuaryblock.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.sanctuaryblock.block.SanctuaryBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class SanctuaryBlockBreakMixin {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
	private void sanctuaryblock$onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!this.player.isCreative() && SanctuaryBlock.isBlockBreakingBlocked(this.player.getWorld(), pos)) {
			this.player.sendMessage(Text.literal("This area is protected by a Sanctuary Block. You cannot break blocks here.")
					.formatted(Formatting.RED), true);
			cir.setReturnValue(false);
		}
	}
}
