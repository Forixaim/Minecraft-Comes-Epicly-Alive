package net.forixaim.mcea.mixin;

import net.conczin.mca.entity.ai.brain.tasks.BowTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowTask.class)
public class MixinBowTask {
    @Shadow
    private static LivingEntity getAttackTarget(LivingEntity entity) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique
    boolean powerShot = false;

    @Shadow
    @Final
    private int squaredRange;

    @Shadow
    private int lastShot;

    @Shadow
    @Final
    private int fireInterval;

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/Behavior;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)V", shift = At.Shift.AFTER), cancellable = true)
    public <E extends Mob & CrossbowAttackMob> void tick(ServerLevel world, E entity, long time, CallbackInfo ci) {

        LivingEntity target = getAttackTarget(entity);
        if (entity.tickCount - lastShot > 4)
        {
            if (!entity.isUsingItem())
            {
                entity.startUsingItem(InteractionHand.MAIN_HAND);
                powerShot = entity.getRandom().nextIntBetweenInclusive(1, 4) == 1;
            }
        }
        double d = entity.distanceToSqr(target);
        float backward = 0.0F;
        if (d > (double)((float)this.squaredRange * 1.25F)) {
            backward = 0.5F;
        } else if (d < (double)((float)this.squaredRange * 0.75F)) {
            backward = -0.5F;
        }

        float strafe = (float)(Math.cos(time / 20.0) * 0.5);
        entity.getMoveControl().strafe(backward, strafe);
        entity.lookAt(target, 30.0F, 30.0F);
        if (entity.tickCount - this.lastShot > this.fireInterval * (powerShot ? 1.5 : 1)) {
            entity.performRangedAttack(target, (powerShot ? 2 : 1));
            if (entity.isUsingItem())
            {
                entity.stopUsingItem();
            }
            this.lastShot = entity.tickCount;
        }
        ci.cancel();
    }

    @Inject(method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)V", at = @At("RETURN"))
    public <E extends Mob & CrossbowAttackMob> void stop(ServerLevel world, E entity, long time, CallbackInfo ci) {
        if (entity.isUsingItem())
        {
            entity.stopUsingItem();
        }
    }

    @Inject(method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)V", at = @At("RETURN"))
    public <E extends Mob & CrossbowAttackMob> void start(ServerLevel world, E entity, long time, CallbackInfo ci) {
        if (!entity.isUsingItem())
        {
            entity.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }
}
