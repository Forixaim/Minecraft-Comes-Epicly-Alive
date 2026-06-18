package net.forixaim.mcea.entity_patch;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.conczin.mca.entity.EquipmentSet;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.MemoryModuleTypeMCA;
import net.conczin.mca.entity.ai.brain.VillagerTasksMCA;
import net.conczin.mca.entity.ai.brain.tasks.*;
import net.conczin.mca.server.world.data.villageComponents.VillageGuardsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class MCATasksEF
{
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getGuardCorePackage(VillagerEntityMCA villager) {
        ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> pairs = ImmutableList.of(
                Pair.of(0, new ConditionalTask<>(
                        new VillagerPanicTrigger(),
                        MCATasksEF::guardTooHurt
                )),
                Pair.of(0,
                        new SayTask("villager.retreat", 100, e -> MCATasksEF.guardTooHurt(e) && e.getVillagerBrain().isPanicking())
                ),
                Pair.of(0,
                        new SayTask("villager.attack", 160, e -> !MCATasksEF.guardTooHurt(e) && MCATasksEF.getPreferredTarget(e).isPresent())
                ),
                Pair.of(0, new ConditionalTask<>(
                        new ExtendedMeleeAttackTask(15, 2.5F, MemoryModuleType.NEAREST_HOSTILE),
                        MCATasksEF::guardTooHurt
                )),
                Pair.of(1, new EquipmentTask(VillagerTasksMCA::isOnDuty, v -> v.getResidency().getHomeVillage()
                        .map(vil -> vil.getVillageGuardsManager().getGuardEquipment(v.getProfession(), v.getDominantHand())).orElse(VillageGuardsManager.getEquipmentFor(v.getDominantHand(), EquipmentSet.GUARD_0, EquipmentSet.GUARD_0_LEFT)))),
                Pair.of(2, StartAttacking.create(t -> true, MCATasksEF::getPreferredTarget)),
                Pair.of(3, StopAttackingIfTargetInvalid.create(livingEntity -> !MCATasksEF.isPreferredTarget(villager, livingEntity))),
                Pair.of(6, BehaviorBuilder.triggerIf(v -> v.isHolding(Items.BOW),
                        BackUpIfTooClose.create(7, 0.75F)
                )),
                Pair.of(5, new BowTask<>(20, 12)),
                Pair.of(6, BehaviorBuilder.triggerIf(v -> v.isHolding(Items.CROSSBOW),
                        BackUpIfTooClose.create(5, 0.75F)
                )),
                Pair.of(7, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(0.75F)),
                Pair.of(8, new ExtendedMeleeAttackTask(20, 2.0F)),
                Pair.of(9, new CrossbowAttack<VillagerEntityMCA, VillagerEntityMCA>())
        );
        return pairs;
    }

    private static boolean guardTooHurt(VillagerEntityMCA villager) {
        return villager.getHealth() < villager.getMaxHealth() * 0.25;
    }

    private static Activity getActivity(VillagerEntityMCA villager) {
        return villager.getBrain().getSchedule().getActivityAt((int) (villager.level().getDayTime() % 24000L));
    }

    private static Optional<? extends LivingEntity> getPreferredTarget(VillagerEntityMCA villager) {
        if (guardTooHurt(villager)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> primary = villager.getBrain().getMemoryInternal(MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY);
            if (primary.isPresent() && (getActivity(villager) != Activity.REST || primary.get().distanceTo(villager) < 8.0)) {
                return primary;
            } else {
                return villager.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
            }
        }
    }

    private static boolean isPreferredTarget(VillagerEntityMCA villager, LivingEntity entity) {
        Optional<? extends LivingEntity> target = getPreferredTarget(villager);
        return target.filter(livingEntity -> livingEntity == entity).isPresent();
    }

}
