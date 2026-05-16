package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.runtime.EmiPersistentData;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.emi.emi.runtime.EmiFavorites.removeFavorite;

@Mixin(EmiFavorites.class)
public abstract class EmiFavoritesMixin {
    @Shadow
    public static List<EmiFavorite> favorites;

    @WrapOperation(method = "updateSynthetic", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private static boolean a(List<EmiFavorite.Synthetic> instance, Object o, Operation<Boolean> original) {
        if (o instanceof EmiFavorite.Synthetic synthetic) {
            if (BoM.tree.goal instanceof ClipboardScreenUtils.DummyMaterialNode dm) {
                if (dm.recipe.getOutputs().get(0).isEqual(synthetic.getStack().getEmiStacks().get(0))) {
                    return false;
                }
            }
        }
        return original.call(instance, o);
    }

    @Inject(method = "addFavorite(Ldev/emi/emi/api/stack/EmiIngredient;Ldev/emi/emi/api/recipe/EmiRecipe;)V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/serializer/EmiIngredientSerializer;getDeserialized(Lcom/google/gson/JsonElement;)Ldev/emi/emi/api/stack/EmiIngredient;"), cancellable = true)
    private static void addJemiFavorite(EmiIngredient stack, EmiRecipe context, CallbackInfo ci) {
        if (stack instanceof JemiStack<?> jemiStack) {
            for (EmiFavorite fav : favorites) {
                if (fav.getRecipe() == context && fav.strictEquals(stack)) {
                    return;
                }
            }
            if (!removeFavorite(jemiStack)) {
                favorites.add(new EmiFavorite(stack.copy().setAmount(1), context));
            }
            EmiPersistentData.save();
            ci.cancel();
        }
    }
}
