package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.emi.emi.runtime.EmiFavorites.countRecipes;

@Mixin(EmiFavorites.class)
public abstract class EmiFavoritesMixin {
    @WrapOperation(method = "updateSynthetic", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private static boolean a(List<EmiFavorite.Synthetic> instance, Object o, Operation<Boolean> original) {
        if (o instanceof EmiFavorite.Synthetic synthetic) {
            if (synthetic.isEmpty()) {
                //return false;
            }
        }
        return original.call(instance, o);
    }
}
