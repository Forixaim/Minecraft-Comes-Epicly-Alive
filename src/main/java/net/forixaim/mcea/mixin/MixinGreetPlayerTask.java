package net.forixaim.mcea.mixin;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.brain.tasks.GreetPlayerTask;
import net.forixaim.mcea.entity_patch.MCAVillagerEntityPatch;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(GreetPlayerTask.class)
public abstract class MixinGreetPlayerTask {
    @Inject(method = "lambda$start$2", at = @At(value = "INVOKE", target = "Lnet/conczin/mca/entity/ai/ConversationManager;addMessage(Lnet/conczin/mca/entity/ai/ConversationManager$Message;)V", shift = At.Shift.AFTER), remap = false)
    private static void injectMethod(VillagerEntityMCA villager, Player player, CallbackInfo ci) {
        Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
        if (EpicFightCapabilities.getEntityPatch(villager, LivingEntityPatch.class) instanceof MCAVillagerEntityPatch entityPatch && memories.getHearts() >= 0)
        {
            if (villager.isGuard())
                entityPatch.playAnimationSynchronized(Animations.BIPED_SALUTE, 0);
            else
                entityPatch.playAnimationSynchronized(Animations.BIPED_WAVE_HAND, 0);
        }
    }
}
