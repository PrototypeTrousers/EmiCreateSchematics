package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.bom.ChanceState;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.bom.ProgressState;
import dev.emi.emi.bom.TreeCost;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TreeCost.class)
public abstract class TreeCostMixin {

    @Inject(method = "calculateCost", at = @At(value = "TAIL"))
    private void a(MaterialNode node, long amount, ChanceState chance, boolean trackProgress, CallbackInfo ci) {
        if (node instanceof ClipboardScreenUtils.ParentOnlyMaterialNode) {
            if (node.children != null && !node.children.isEmpty()) {
                if (node.children.stream().allMatch(p -> p.progress == ProgressState.COMPLETED)) {
                    complete(node);
                }
            }
        }
    }

    @Shadow
    protected abstract void complete(MaterialNode node);
}
