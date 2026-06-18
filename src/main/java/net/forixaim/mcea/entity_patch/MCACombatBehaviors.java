package net.forixaim.mcea.entity_patch;

import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class MCACombatBehaviors
{
    public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> MCA_SWORD = CombatBehaviors.<HumanoidMobPatch<?>>builder()
            .newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SWORD_AUTO1).withinEyeHeight().withinDistance(0.0D, 2.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SWORD_AUTO2).withinEyeHeight().withinDistance(0.0D, 2.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SWORD_AUTO3).withinEyeHeight().withinDistance(0.0D, 2.0D))
            );
    public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_SPEAR_TWOHAND = CombatBehaviors.<HumanoidMobPatch<?>>builder()
            .newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SPEAR_TWOHAND_AUTO1).withinEyeHeight().withinDistance(0.0D, 4.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SPEAR_TWOHAND_AUTO2).withinEyeHeight().withinDistance(0.0D, 4.0D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().behavior(h -> {
                                h.getOriginal().jumpFromGround();
                                h.playAnimationSynchronized(Animations.SPEAR_TWOHAND_AIR_SLASH, 0);
                            }).custom(h -> h.getTarget().getY() > h.getOriginal().getEyeY() + 1d)
            ));



    public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_FIST = CombatBehaviors.<HumanoidMobPatch<?>>builder()
            .newBehaviorSeries(
                    CombatBehaviors.BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.FIST_AUTO1).withinEyeHeight().withinDistance(0.0D, 1.8D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.FIST_AUTO2).withinEyeHeight().withinDistance(0.0D, 1.8D))
                            .nextBehavior(CombatBehaviors.Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.FIST_AUTO3).withinEyeHeight().withinDistance(0.0D, 1.8D))
            );
}
