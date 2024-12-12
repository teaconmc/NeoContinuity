package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.mixinterface.BlockAndTintGetterExtension;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterMixin extends BlockAndTintGetterExtension {

}
