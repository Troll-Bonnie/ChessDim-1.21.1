package net.baphy.chessdim.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldOpenFlows.class)
public class ExperimentalWarningMixin {

    @Inject(method = "checkForExperimentalWarning", at = @At("HEAD"), cancellable = true)
    private void suppressExperimentalWarning(CallbackInfoReturnable<List<?>> cir) {
        cir.setReturnValue(List.of());
    }
}
