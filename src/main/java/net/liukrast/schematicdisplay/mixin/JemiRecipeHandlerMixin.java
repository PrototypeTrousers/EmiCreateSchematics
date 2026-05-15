package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.jemi.JemiRecipeHandler;
import mezz.jei.api.recipe.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JemiRecipeHandler.class)
public class JemiRecipeHandlerMixin {
    @Final
    @Shadow private RecipeType type;

    @Inject(method = "getRawRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;"),cancellable = true)
    void a(EmiRecipe recipe, CallbackInfoReturnable cir) {
        if (this.type != null && this.type.getRecipeClass() != null) {
//            if (recipe instanceof TMRVRecipe<?> tmRecipe) {
//                cir.setReturnValue(tmRecipe);
//                cir.cancel();
//            }
        }
    }
}
