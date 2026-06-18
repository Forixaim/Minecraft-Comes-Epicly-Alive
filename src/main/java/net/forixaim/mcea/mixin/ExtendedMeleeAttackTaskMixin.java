package net.forixaim.mcea.mixin;

import net.conczin.mca.entity.ai.brain.tasks.ExtendedMeleeAttackTask;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ExtendedMeleeAttackTask.class, remap = false)
public abstract class ExtendedMeleeAttackTaskMixin
{
    @Inject(method = "withinRange", at = @At("RETURN"), remap = false, cancellable = true)
    private void withinRangeHook(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
