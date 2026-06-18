package net.forixaim.mcea.events;

import net.conczin.mca.entity.VillagerLike;
import net.forixaim.mcea.MinecraftComesEpiclyAlive;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = MinecraftComesEpiclyAlive.MOD_ID)
public class GameEvents
{
    @SubscribeEvent
    public static void onAttack(LivingIncomingDamageEvent event)
    {
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK))
        {
            if (event.getEntity() instanceof VillagerLike<?>)
            {
                event.setCanceled(true);
            }
        }
    }
}
