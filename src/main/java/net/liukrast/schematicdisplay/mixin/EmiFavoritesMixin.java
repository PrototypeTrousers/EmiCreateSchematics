package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EmiFavorites.class)
public abstract class EmiFavoritesMixin {
    @WrapOperation(method = "updateSynthetic", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private static boolean a(List<EmiFavorite.Synthetic> instance, Object o, Operation<Boolean> original) {
        if (o instanceof EmiFavorite.Synthetic synthetic) {
            if (synthetic.isEmpty()) {
                return false;
            }
        }
        return original.call(instance, o);
    }
}
