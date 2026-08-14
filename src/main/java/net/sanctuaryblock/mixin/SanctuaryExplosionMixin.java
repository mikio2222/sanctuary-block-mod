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

	// After the explosion has figured out which blocks it would destroy, strip out
	// any block inside a protected Sanctuary zone.
	@Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
	private void sanctuaryblock$onCollectBlocksAndDamageEntities(CallbackInfo ci) {
		Explosion self = (Explosion) (Object) this;
		self.getAffectedBlocks().removeIf(pos -> SanctuaryBlock.isExplosionBlocked(this.world, pos));
	}

	// If the explosion itself originates inside a protected Sanctuary zone, cancel it entirely
	// (no block damage, no entity damage, no particles).
	@Inject(method = "affectWorld", at = @At("HEAD"), cancellable = true)
	private void sanctuaryblock$onAffectWorld(boolean particles, CallbackInfo ci) {
		BlockPos center = BlockPos.ofFloored(this.x, this.y, this.z);
		if (SanctuaryBlock.isExplosionBlocked(this.world, center)) {
			ci.cancel();
		}
	}
}
