package net.forixaim.mcea.mixin;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntityMCA.class)
public class MixinVillagerMCA {
    @Inject(method = "hurt", at = @At("HEAD"),remap = false, cancellable = true)
    public void hurt(DamageSource source, float damageAmount, CallbackInfoReturnable<Boolean> cir)
    {
        if (source.getEntity() instanceof Player)
        {
            cir.setReturnValue(false);
        }
    }
}
