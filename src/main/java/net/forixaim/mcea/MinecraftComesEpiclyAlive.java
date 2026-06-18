package net.forixaim.mcea;

import net.conczin.mca.registry.EntitiesMCA;
import net.forixaim.mcea.entity_patch.MCAVillagerEntityPatch;
import net.forixaim.mcea.renderer.PMCAVillagerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightAttributes;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MinecraftComesEpiclyAlive.MOD_ID)
public class MinecraftComesEpiclyAlive {
    public static final String MOD_ID = "mcea";
    public MinecraftComesEpiclyAlive(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(EntityAttributeModificationEvent.class, event -> {
            mcaVillager(EntitiesMCA.MALE_VILLAGER, event);
            mcaVillager(EntitiesMCA.FEMALE_VILLAGER, event);
        });
        if (FMLEnvironment.dist.isClient())
            modEventBus.addListener(this::clientSetup);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private static void common(EntityType<? extends LivingEntity> entityType, EntityAttributeModificationEvent event) {
        event.add(entityType, EpicFightAttributes.WEIGHT);
        event.add(entityType, EpicFightAttributes.ARMOR_NEGATION);
        event.add(entityType, EpicFightAttributes.IMPACT);
        event.add(entityType, EpicFightAttributes.MAX_STRIKES);
        event.add(entityType, EpicFightAttributes.STUN_ARMOR);
    }

    private static void mcaVillager(EntityType<? extends LivingEntity> entityType, EntityAttributeModificationEvent event) {
        common(entityType, event);
        event.add(entityType, EpicFightAttributes.OFFHAND_ATTACK_SPEED);
        event.add(entityType, EpicFightAttributes.OFFHAND_MAX_STRIKES);
        event.add(entityType, EpicFightAttributes.OFFHAND_ARMOR_NEGATION);
        event.add(entityType, EpicFightAttributes.OFFHAND_IMPACT);
        event.add(entityType, EpicFightAttributes.MAX_STAMINA);
        event.add(entityType, EpicFightAttributes.STAMINA_REGEN);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(entityPatch -> {
            entityPatch.registerEntityPatch(EntitiesMCA.MALE_VILLAGER, MCAVillagerEntityPatch::new);
            entityPatch.registerEntityPatch(EntitiesMCA.FEMALE_VILLAGER, MCAVillagerEntityPatch::new);
        });
        event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(EntitiesMCA.MALE_VILLAGER, Armatures.BIPED);
            Armatures.registerEntityTypeArmature(EntitiesMCA.FEMALE_VILLAGER, Armatures.BIPED);
        });
    }

    private void clientSetup(final FMLClientSetupEvent setupEvent) {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(event -> {
            event.addPatchedEntityRenderer(EntitiesMCA.MALE_VILLAGER, entityType -> new PMCAVillagerRenderer(event.getContext(),entityType));
            event.addPatchedEntityRenderer(EntitiesMCA.FEMALE_VILLAGER, entityType -> new PMCAVillagerRenderer(event.getContext(),entityType));
        });
    }
}
