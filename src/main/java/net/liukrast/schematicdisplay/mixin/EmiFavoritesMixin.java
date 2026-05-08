package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(EmiFavorites.class)
public abstract class EmiFavoritesMixin {
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
}
