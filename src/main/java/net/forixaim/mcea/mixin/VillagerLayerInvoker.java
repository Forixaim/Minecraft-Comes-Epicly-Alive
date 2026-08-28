package net.forixaim.mcea.mixin;

import net.conczin.mca.client.render.layer.VillagerLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(value = VillagerLayer.class, remap = false)
public interface VillagerLayerInvoker
{
    @Invoker(value = "cached", remap = false)
    ResourceLocation invokeCached(String name, Function<String, ResourceLocation> supplier);
}
