package net.forixaim.mcea.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.conczin.mca.client.model.CommonVillagerModel;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.client.resources.EyeTextureLayers;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.Traits;
import net.forixaim.mcea.mixin.FaceLayerInvoker;
import net.forixaim.mcea.mixin.VillagerLayerInvoker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

import java.util.Objects;

public class MCAPatchedFaceLayer extends PatchedLayer<VillagerEntityMCA, HumanoidMobPatch<VillagerEntityMCA>, VillagerEntityModelMCA<VillagerEntityMCA>, FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>>>
{
    final PMCAVillagerRenderer parent;
    final FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer;
    MCAPatchedFaceLayer(PMCAVillagerRenderer parent, FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer)
    {
        this.parent = parent;
        this.vanillaLayer = vanillaLayer;
    }
    @Override
    protected void renderLayer(HumanoidMobPatch<VillagerEntityMCA> entityPatch, VillagerEntityMCA original, @Nullable FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks)
    {
        int overlay = LivingEntityRenderer.getOverlayCoords(original, 0.0F);
        ResourceLocation skin = vanillaLayer.getSkin(original);
        if (vanillaLayer instanceof FaceLayerInvoker accessor && accessor.invokeIsBlinking(original)) {
            ResourceLocation blink = accessor.invokeGetBlinkSkin();
            if (vanillaLayer.canUse(blink)) {
                this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, blink, -1);
            }
        } else {
            VillagerLike<?> villagerLike = this.getVillager(original);
            if (vanillaLayer.canUse(skin) && vanillaLayer instanceof FaceLayerInvoker accessor) {

                this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, accessor.invokeGetOrGenerateEyeLayer(skin, EyeTextureLayers.Layer.SCLERA, EyeTextureLayers.Side.FULL), -1);
                this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, accessor.invokeGetOrGenerateEyeLayer(skin, EyeTextureLayers.Layer.DETAILS, EyeTextureLayers.Side.FULL), -8355712);

                if (villagerLike.getTraits().hasTrait(Traits.HETEROCHROMIA)) {
                    this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, accessor.invokeGetOrGenerateEyeLayer(skin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.LEFT), this.getEyeColor(original, partialTicks, true));
                    this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, accessor.invokeGetOrGenerateEyeLayer(skin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.RIGHT), this.getEyeColor(original, partialTicks, false));
                } else {
                    this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, accessor.invokeGetOrGenerateEyeLayer(skin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.FULL), this.getEyeColor(original, partialTicks, false));
                }
            }

            ResourceLocation extraOverlay = null;
            if (!Objects.equals(skin, null) && vanillaLayer.canUse(null)) {
                this.renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, null, -1);
            }

        }
    }

    private ResourceLocation getBlinkSkin(FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer) {
        if (vanillaLayer instanceof VillagerLayerInvoker invoker)
            return invoker.invokeCached("skins/face/blink.png", ResourceLocation::parse);
        return null;
    }

    private int getEyeColor(VillagerEntityMCA villager, float tickDelta, boolean left) {
        VillagerLike<?> villagerLike = this.getVillager(villager);
        int color;
        if (villagerLike.getTraits().hasTrait(Traits.RAINBOW_EYES)) {
            int offset = left && villagerLike.getTraits().hasTrait(Traits.HETEROCHROMIA) ? 25 * DyeColor.values().length / 2 : 0;
            color = this.getRainbow(villager, tickDelta, offset);
        } else {
            color = EyeTextureLayers.getStaticEyeColor(villagerLike, left);
        }

        return EyeTextureLayers.applyBrightness(color, villagerLike.getGenetics().getGene(Genetics.EYE_BRIGHTNESS));
    }

    private int getRainbow(VillagerEntityMCA villager, float tickDelta, int offset) {
        int ticks = Math.abs(villager.tickCount) + offset;
        int block = ticks / 25 + villager.getId();
        int count = DyeColor.values().length;
        int first = block % count;
        int second = (block + 1) % count;
        float mix = ((float)(ticks % 25) + tickDelta) / 25.0F;
        return FastColor.ARGB32.lerp(mix, Sheep.getColor(DyeColor.byId(first)), Sheep.getColor(DyeColor.byId(second)));
    }

    private boolean isBlinking(VillagerEntityMCA villager) {
        int time = villager.tickCount / 2 + (int)(this.getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536.0F);
        return time % 50 == 1 || time % 57 == 1 || villager.isSleeping() || villager.isDeadOrDying();
    }

    private VillagerLike<?> getVillager(VillagerEntityMCA villager) {
        return CommonVillagerModel.getVillager(villager);
    }

    private void renderMCALayer(HumanoidMobPatch<VillagerEntityMCA> entitypatch, VillagerEntityMCA villager, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float partialTicks, ResourceLocation resourceLocation, int color)
    {
        int packerOverlay = LivingEntityRenderer.getOverlayCoords(villager, 0);
        RenderType renderType = RenderType.entityCutoutNoCull(resourceLocation);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float alpha = a / 255.0F;
        float red   = r / 255.0F;
        float green = g / 255.0F;
        float blue  = b / 255.0F;
        parent.getDefaultMesh().get().draw(poseStack, buffer, renderType, packedLight, red, green, blue, alpha, packerOverlay, entitypatch.getArmature(), poses);
    }
}
