package net.forixaim.mcea.entity_patch;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.BowTask;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.registry.ProfessionsMCA;
import net.forixaim.mcea.MinecraftComesEpiclyAlive;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightExpandedEntityDataAccessors;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SkeletonPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.data.ExpandedSyncedData;

public class MCAVillagerEntityPatch extends HumanoidMobPatch<VillagerEntityMCA> {
    private float stamina;
    protected int tickSinceLastAction;
    protected int staminaRegenAwaitTicks;

    public MCAVillagerEntityPatch(VillagerEntityMCA original) {
        super(original, Factions.VILLAGER);
    }

    @Override
    public void onConstructed(VillagerEntityMCA original) {
        super.onConstructed(original);
    }

    @Override
    public LivingEntity getTarget() {
        if (original.getMCABrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            return original.getMCABrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        }
        return super.getTarget();
    }

    @Override
    protected void initAI() {
        super.initAI();
    }

    public float getMaxStamina() {
        AttributeInstance maxStamina = this.original.getAttribute(EpicFightAttributes.MAX_STAMINA);
        return (float)(maxStamina == null ? 0 : maxStamina.getValue());
    }

    public float getStamina() {
        return this.getMaxStamina() <= 0.0F ? 0.0F : this.stamina;
    }

    @Override
    protected void registerExpandedEntityDataAccessors(final ExpandedSyncedData expandedSynchedData) {
        super.registerExpandedEntityDataAccessors(expandedSynchedData);
        expandedSynchedData.register(EpicFightExpandedEntityDataAccessors.STAMINA);
    }

    public static void initAttributes(EntityAttributeModificationEvent event) {
        event.add(EntitiesMCA.MALE_VILLAGER, EpicFightAttributes.IMPACT, 1.0D);
        event.add(EntitiesMCA.FEMALE_VILLAGER, EpicFightAttributes.IMPACT, 1.0D);
    }

    @Override
    protected void setWeaponMotions() {
        super.setWeaponMotions();
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.SWORD,
                ImmutableMap.of(CapabilityItem.Styles.ONE_HAND,
                        MCACombatBehaviors.MCA_SWORD,
                        CapabilityItem.Styles.TWO_HAND,
                        MobCombatBehaviors.HUMANOID_DUAL_SWORD));
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.FIST,
                ImmutableMap.of(CapabilityItem.Styles.COMMON,
                        MCACombatBehaviors.HUMANOID_FIST));
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.SPEAR,
                ImmutableMap.of(CapabilityItem.Styles.TWO_HAND,
                        MCACombatBehaviors.HUMANOID_SPEAR_TWOHAND));
    }

    public void setStamina(float value) {
        this.stamina = Mth.clamp(value, 0.0F, this.getMaxStamina());
        this.getExpandedSynchedData().set(EpicFightExpandedEntityDataAccessors.STAMINA, stamina);
    }

    @Override
    public void preTickServer() {
        super.preTickServer();
        if (this.state.canBasicAttack()) {
            this.tickSinceLastAction++;
        }

        if (!this.state.inaction()) {
            if (this.staminaRegenAwaitTicks > 0) this.staminaRegenAwaitTicks--;
        }

        float stamina = this.getStamina();
        float maxStamina = this.getMaxStamina();
        float staminaRegen = (float)this.original.getAttributeValue(EpicFightAttributes.STAMINA_REGEN);

        if (staminaRegen > 0.0F) {
            int regenWhenLessThan = 30 - (900 / (int)(30 * staminaRegen));

            if (stamina < maxStamina && this.staminaRegenAwaitTicks <= regenWhenLessThan) {
                float staminaFactor = 1.0F + (float)Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
                this.setStamina(stamina + maxStamina * 0.01F * staminaFactor * staminaRegen);
            }
        }

        if (maxStamina < stamina) {
            this.setStamina(maxStamina);
        }
    }

    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    private boolean isAnticipatingAttack() {
        if (getTarget() == null || !this.original.isGuard())
        {
            return false;
        }
        if (EpicFightCapabilities.getEntityPatch(getTarget(), LivingEntityPatch.class) == null)
        {
            return this.original.distanceTo(getTarget()) < 1.5F && this.getEntityState().canBasicAttack();
        }
        else if (EpicFightCapabilities.getEntityPatch(getTarget(), LivingEntityPatch.class).getEntityState().canBasicAttack())
        {
            return this.getEntityState().inaction();
        }
        return false;
    }

    private AnimationManager.AnimationAccessor<? extends DodgeAnimation> selectDodge()
    {
        ClipContext backClip = new ClipContext(this.original.position(), this.original.position().add(this.original.getLookAngle()).scale(1.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.original);
        ClipContext leftClip = new ClipContext(this.original.position(), this.original.position().add(this.original.getLookAngle().yRot(90)).scale(1.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.original);
        ClipContext rightClip = new ClipContext(this.original.position(), this.original.position().add(this.original.getLookAngle().yRot(-90)).scale(1.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.original);
        BlockHitResult backHit = this.original.level().clip(backClip);
        BlockHitResult leftHit = this.original.level().clip(leftClip);
        BlockHitResult rightHit = this.original.level().clip(rightClip);
        if (backHit.getType() == HitResult.Type.MISS)
            return Animations.BIPED_STEP_BACKWARD;
        if (leftHit.getType() == HitResult.Type.MISS)
            return Animations.BIPED_STEP_LEFT;
        if (rightHit.getType() == HitResult.Type.MISS)
            return Animations.BIPED_STEP_RIGHT;
        return Animations.BIPED_STEP_FORWARD;
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        if (this.original.getHealth() <= 0.0F) {
            currentLivingMotion = LivingMotions.DEATH;
        } else if (this.state.inaction() && considerInaction) {
            currentLivingMotion = LivingMotions.IDLE;
        } else {
            if (original.getVehicle() != null) {
                currentLivingMotion = LivingMotions.MOUNT;
            } else {
                if (this.original.getDeltaMovement().y < -0.55F || this.isAirborneState())
                    currentLivingMotion = LivingMotions.FALL;
                else if (original.walkAnimation.speed() > 0.08F) {
                    if (original.walkAnimation.speed() > 0.7f)
                        currentLivingMotion = LivingMotions.RUN;
                    else
                        currentLivingMotion = LivingMotions.WALK;
                }
                else
                    currentLivingMotion = LivingMotions.IDLE;
            }
        }

        if (!this.state.updateLivingMotion() && considerInaction) {
            this.currentCompositeMotion = LivingMotions.NONE;
        } else {
            CapabilityItem mainhandItemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
            CapabilityItem offhandItemCap = this.getHoldingItemCapability(InteractionHand.OFF_HAND);
            LivingMotion customLivingMotion = mainhandItemCap.getLivingMotion(this, InteractionHand.MAIN_HAND);

            if (customLivingMotion == null) customLivingMotion = offhandItemCap.getLivingMotion(this, InteractionHand.OFF_HAND);
            if (customLivingMotion != null)
                currentCompositeMotion = customLivingMotion;
            else if (this.original.isUsingItem()) {
                UseAnim useAnim = this.original.getUseItem().getUseAnimation();
                if (useAnim == UseAnim.BLOCK)
                    currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
                else if (useAnim == UseAnim.CROSSBOW)
                    currentCompositeMotion = LivingMotions.RELOAD;
                else if (useAnim == UseAnim.DRINK)
                    currentCompositeMotion = LivingMotions.DRINK;
                else if (useAnim == UseAnim.EAT)
                    currentCompositeMotion = LivingMotions.EAT;
                else if (useAnim == UseAnim.SPYGLASS)
                    currentCompositeMotion = LivingMotions.SPECTATE;
                else
                    currentCompositeMotion = currentLivingMotion;
            } else {
                if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getRealAnimation().get().isReboundAnimation())
                    currentCompositeMotion = LivingMotions.SHOT;
                else if (this.isAnticipatingAttack() && this.getHoldingItemCapability(InteractionHand.MAIN_HAND).isWeaponCategory(CapabilityItem.WeaponCategories.SWORD))
                    currentCompositeMotion = LivingMotions.BLOCK;
                else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
                    currentCompositeMotion = LivingMotions.DIGGING;
                else
                    currentCompositeMotion = currentLivingMotion;
            }
        }
    }

    private boolean isValidDamageSource(DamageSource source)
    {
        return source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) ||
                source.is(DamageTypes.MOB_PROJECTILE) || source.is(DamageTypes.ARROW) ||
                source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.THROWN) ||
                source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.WITHER_SKULL) ||
                source.is(DamageTypes.WIND_CHARGE);
    }

    @Override
    public AttackResult tryHurt(DamageSource damageSource, float amount) {
        if (isValidDamageSource(damageSource)) {
            if (this.currentCompositeMotion == LivingMotions.BLOCK || this.currentCompositeMotion == LivingMotions.BLOCK_SHIELD &&
                    !(damageSource.is(EpicFightDamageTypeTags.UNBLOCKALBE) || damageSource.is(EpicFightDamageTypeTags.GUARD_PUNCTURE)) ) {

                return AttackResult.blocked(amount);
            }
            if (this.getStamina() >= 3 && damageSource.is(EpicFightDamageTypeTags.BYPASS_DODGE)) {
                this.setStamina(this.getStamina() - 3);
                this.staminaRegenAwaitTicks = 40;
                this.playAnimationSynchronized(selectDodge(), 0);
                return AttackResult.missed(amount);
            }
        }
        return super.tryHurt(damageSource, amount);
    }

    @Override
    public AttackResult tryHarm(Entity target, EpicFightDamageSource damagesource, float amount) {
        return super.tryHarm(target, damagesource, amount);
    }
}
