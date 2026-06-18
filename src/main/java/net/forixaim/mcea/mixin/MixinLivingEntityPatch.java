package net.forixaim.mcea.mixin;

import net.conczin.mca.entity.VillagerLike;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

@Mixin(LivingEntityPatch.class)
public class MixinLivingEntityPatch {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand, CallbackInfoReturnable<AttackResult> cir) {
        if (damageSource.getEntity() instanceof Player)
        {
            if (target instanceof VillagerLike<?>) {
                cir.setReturnValue(AttackResult.missed(0));
            }
        }
    }
}
