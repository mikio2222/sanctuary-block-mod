package net.sanctuaryblock.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.sanctuaryblock.block.SanctuaryBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class SanctuaryMobSpawnMixin {

	@Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
	private void sanctuaryblock$onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (!(entity instanceof MobEntity mobEntity)) {
			return;
		}
		ServerWorld world = (ServerWorld) (Object) this;
		BlockPos pos = entity.getBlockPos();

		if (!SanctuaryBlock.isMobSpawningBlocked(world, pos)) {
			return;
		}

		Identifier idCheck = EntityType.getId(entity.getType());
		if (!"minecraft".equals(idCheck.getNamespace())) {
			return;
		}

		// Let naturally-tamed / named / persistent mobs through, and don't despawn mobs
		// that already have nearby players (covers e.g. mobs led in by the player).
		if (mobEntity.isPersistent()) {
			return;
		}
		if (mobEntity.hasCustomName()) {
			return;
		}
		if (mobEntity.age == 0) {
			boolean playerNearby = !world.getPlayers(player -> player.squaredDistanceTo(entity) <= 100.0).isEmpty();
			if (playerNearby) {
				return;
			}
		}

		cir.setReturnValue(false);
	}
}
