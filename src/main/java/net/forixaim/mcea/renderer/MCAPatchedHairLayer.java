package net.forixaim.mcea.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.conczin.mca.MCA;
import net.conczin.mca.client.gui.immersive_library.SkinCache;
import net.conczin.mca.client.model.CommonVillagerModel;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.layer.HairLayer;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.forixaim.mcea.mixin.VillagerLayerInvoker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public class MCAPatchedHairLayer extends PatchedLayer<VillagerEntityMCA, HumanoidMobPatch<VillagerEntityMCA>, VillagerEntityModelMCA<VillagerEntityMCA>, HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>>>
{
    final PMCAVillagerRenderer parent;
    final HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer;
    MCAPatchedHairLayer(PMCAVillagerRenderer parent, HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer)
    {
        this.parent = parent;
        this.vanillaLayer = vanillaLayer;
    }



    @Override
    protected void renderLayer(HumanoidMobPatch<VillagerEntityMCA> entityPatch, VillagerEntityMCA original, @Nullable HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks)
    {
        boolean renderedLayeredHair = false;

        for(LayeredHair.Category category : LayeredHair.Category.RENDER_ORDER) {
            String identifier = CommonVillagerModel.getVillager(original).getLayeredHair(category);
            if (!identifier.isBlank()) {
                renderedLayeredHair = true;
                ResourceLocation texture = this.getTexture(identifier, vanillaLayer);
                if (vanillaLayer.canUse(texture)) {
                    renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, texture);
                }

                ResourceLocation overlayTexture = this.getOverlayTexture(identifier, vanillaLayer);
                if (vanillaLayer.canUse(overlayTexture)) {
                    renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, overlayTexture);
                }
            }
        }

        if (!renderedLayeredHair) {
            ResourceLocation texture = this.getSkin(original, vanillaLayer);
            if (vanillaLayer.canUse(texture)) {
                renderMCALayer(entityPatch, original, poseStack, buffer, packedLight, poses, partialTicks, texture);
            }
        }
    }

    private void renderMCALayer(HumanoidMobPatch<VillagerEntityMCA> entityPatch, VillagerEntityMCA original, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float partialTicks, ResourceLocation texture)
    {
        int packerOverlay = LivingEntityRenderer.getOverlayCoords(original, 0);
        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        int color = this.vanillaLayer.getColor(original, partialTicks);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float alpha = a / 255.0F;
        float red   = r / 255.0F;
        float green = g / 255.0F;
        float blue  = b / 255.0F;
        parent.getDefaultMesh().get().draw(poseStack, buffer, renderType, packedLight, red, green, blue, alpha, packerOverlay, entityPatch.getArmature(), poses);
    }

    private ResourceLocation getOverlayTexture(String identifier, HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer) {
        if (vanillaLayer instanceof VillagerLayerInvoker invoker)
            return !identifier.startsWith("immersive_library:") && identifier.endsWith(".png") ? invoker.invokeCached(identifier.replace(".png", "_overlay.png"), ResourceLocation::parse) : null;
        return null;
    }

    public ResourceLocation getSkin(VillagerEntityMCA villager, HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer) {
        return this.getTexture(CommonVillagerModel.getVillager(villager).getHair(), vanillaLayer);
    }

    private ResourceLocation getTexture(String identifier, HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer)
    {
        if (MCA.isBlankString(identifier) || !(vanillaLayer instanceof VillagerLayerInvoker invoker))
        {
            return null;
        } else
        {
            return identifier.startsWith("immersive_library:") ? SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring("immersive_library:".length()))) : ((VillagerLayerInvoker)vanillaLayer).invokeCached(identifier, ResourceLocation::parse);
        }
    }
}
