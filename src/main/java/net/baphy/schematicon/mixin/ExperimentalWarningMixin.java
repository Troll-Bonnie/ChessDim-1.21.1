package net.baphy.schematicon.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldOpenFlows.class)
public class ExperimentalWarningMixin {

    //@Inject(method = "checkForExperimentalWarning", at = @At("HEAD"), cancellable = true)
    private void suppressExperimentalWarning(CallbackInfoReturnable<List<?>> cir) {
        cir.setReturnValue(List.of());
    }
}
