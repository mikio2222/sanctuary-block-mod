package net.sanctuaryblock.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.sanctuaryblock.block.SanctuaryBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Explosion.class)
public class SanctuaryExplosionMixin {

	@Shadow
	@Final
	private World world;

	@Shadow
	@Final
	private double x;

	@Shadow
	@Final
	private double y;

	@Shadow
	@Final
	private double z;

	@Inject(method = "getBlocksToDestroy", at = @At("RETURN"), cancellable = true)
	private void sanctuaryblock$onGetBlocksToDestroy(CallbackInfoReturnable<List<BlockPos>> cir) {
		List<BlockPos> affected = cir.getReturnValue();
		affected.removeIf(pos -> SanctuaryBlock.isExplosionBlocked(this.world, pos));
		cir.setReturnValue(affected);
	}

	@Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"), cancellable = true)
	private void sanctuaryblock$onCollectBlocksAndDamageEntities(CallbackInfo ci) {
		BlockPos center = BlockPos.ofFloored(this.x, this.y, this.z);
		if (SanctuaryBlock.isExplosionBlocked(this.world, center)) {
			ci.cancel();
		}
	}
}
