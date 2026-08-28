package net.forixaim.mcea.mixin;

import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.client.resources.EyeTextureLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(
    value = {FaceLayer.class},
    remap = false
)
public interface FaceLayerInvoker<T extends LivingEntity, M extends HumanoidModel<T>> {
    @Invoker(
        value = "getOrGenerateEyeLayer",
        remap = false
    )
    ResourceLocation invokeGetOrGenerateEyeLayer(ResourceLocation resourceLocation, EyeTextureLayers.Layer layer, EyeTextureLayers.Side side);

    @Invoker("getBlinkSkin")
    ResourceLocation invokeGetBlinkSkin();

    @Invoker("isBlinking")
    boolean invokeIsBlinking(T entity);
}
