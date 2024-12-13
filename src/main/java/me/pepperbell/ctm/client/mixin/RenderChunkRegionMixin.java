package me.pepperbell.ctm.client.mixin;

import me.pepperbell.ctm.client.mixinterface.BlockAndTintGetterExtension;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin implements BlockAndTintGetterExtension {

    @Shadow
    @Final
    protected Level level;

    @Override
    public boolean continuity$hasBiome() {
        return true;
    }

    @Override
    public Holder<Biome> continuity$getBiome(BlockPos pos) {
        return level.getBiome(pos);
    }
}
