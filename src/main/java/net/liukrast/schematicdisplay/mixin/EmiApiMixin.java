package net.liukrast.schematicdisplay.mixin;

import com.simibubi.create.content.schematics.SchematicItem;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiApi.class)
public class EmiApiMixin {
    @Inject(method = "displayUses", at = @At("TAIL"))
    private static void displayUses(EmiIngredient stack, CallbackInfo ci) {
        if (!stack.isEmpty() ) {
            EmiStack zero = stack.getEmiStacks().get(0);
            if (zero.getItemStack().getItem() instanceof SchematicItem si)
            {
                int i = 0;
            }

        }
    }
}
