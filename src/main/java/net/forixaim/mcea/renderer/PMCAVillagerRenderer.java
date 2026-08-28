package net.forixaim.mcea.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.VillagerEntityMCARenderer;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.client.render.layer.HairLayer;
import net.conczin.mca.client.render.layer.VillagerLayer;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.registry.ProfessionsMCA;
import net.forixaim.mcea.MinecraftComesEpiclyAlive;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public class PMCAVillagerRenderer extends PHumanoidRenderer<VillagerEntityMCA, HumanoidMobPatch<VillagerEntityMCA>, VillagerEntityModelMCA<VillagerEntityMCA>, VillagerEntityMCARenderer, HumanoidMesh> {
    public PMCAVillagerRenderer(
            EntityRendererProvider.Context context,
                                EntityType<?> entityType) {
        super(Meshes.BIPED, context, entityType);
        EntityRenderer<?> renderer = context.getEntityRenderDispatcher().renderers.get(entityType);

        if (renderer instanceof VillagerEntityMCARenderer entityMCARenderer) {
            LogUtils.getLogger().debug("Patching Villager Renderer with layer size: {}", entityMCARenderer.layers.size());
            for (var layer : entityMCARenderer.layers)
            {
                LogUtils.getLogger().debug("Current Layer: {}", layer.getClass());
                if (layer instanceof VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> villagerLayer)
                {
                    LogUtils.getLogger().debug("Patching Villager Layer: {}", villagerLayer.getClass());
                    if (layer instanceof HairLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> hairLayer)
                        this.addPatchedLayer(layer.getClass(), new MCAPatchedHairLayer(this, hairLayer));
                    else if (layer instanceof FaceLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> faceLayer)
                        this.addPatchedLayer(layer.getClass(), new MCAPatchedFaceLayer(this, faceLayer));
                    else
                        this.addPatchedLayer(layer.getClass(), new MCAPatchedLayer(this, villagerLayer));
                }
            }
        }
    }


    @Override
    protected void prepareModel(HumanoidMesh mesh, VillagerEntityMCA entity, HumanoidMobPatch<VillagerEntityMCA> entitypatch, VillagerEntityMCARenderer renderer) {
        super.prepareModel(mesh, entity, entitypatch, renderer);

    }

    @Override
    protected void renderLayer(LivingEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, HumanoidMobPatch<VillagerEntityMCA> entitypatch, VillagerEntityMCA entity, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        float height = entity.getGenetics().getVerticalScaleFactor();
        float width = entity.getGenetics().getHorizontalScaleFactor();
        poseStack.pushPose();
        poseStack.scale(width, height, 1);
        super.renderLayer(renderer, entitypatch, entity, poses, buffer, poseStack, packedLight, partialTicks);
        poseStack.popPose();
    }
}
