package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.*;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(TreeCost.class)
public abstract class TreeCostMixin {
    @Shadow
    public Map<EmiIngredient, FlatMaterialCost> costs;

    @Inject(method = "addRemainder", at = @At(value = "HEAD"), cancellable = true)
    private void addRemainder(EmiStack stack, long amount, ChanceState chance, CallbackInfo ci) {
        if (stack.isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "getRemainder", at = @At(value = "HEAD"), cancellable = true)
    private void getRemainder(EmiStack stack, long desired, boolean catalyst, CallbackInfoReturnable<Long> cir) {
        if (stack.isEmpty()) {
            cir.setReturnValue(desired);
        }
    }

    @Inject(method = "complete", at = @At(value = "HEAD"), cancellable = true)
    private void complete(MaterialNode node, CallbackInfo ci) {
        if (node instanceof ClipboardScreenUtils.ParentOnlyMaterialNode) {
            node.progress = ProgressState.COMPLETED;
            node.totalNeeded = 0;
            node.neededBatches = 0;
            ci.cancel();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void calculateCost(MaterialNode node, long amount, ChanceState chance, boolean trackProgress) {
        if (trackProgress) {
            node.progress = ProgressState.UNSTARTED;
            node.totalNeeded = 0;
            node.neededBatches = 0;
        }
        boolean catalyst = node.catalyst;
        if (catalyst) {
            amount = node.amount;
        }
        EmiRecipe recipe = node.recipe;
        if (recipe instanceof EmiResolutionRecipe err) {
            calculateCost(node.children.get(0), amount, chance, trackProgress);
            if (catalyst) {
                addRemainder(err.stack, amount, chance);
            }
            return;
        }
        long original = amount;
        List<EmiStack> ingredientStacks = node.ingredient.getEmiStacks();
        boolean emptyIngredient = false;
        for (int i = 0; i < ingredientStacks.size(); i++) {
            if (chance.chanced()) {
                double desired = amount * chance.chance();
                double given = getChancedRemainder(ingredientStacks.get(i), desired, catalyst, chance);
                if (given > 0) {
                    double scaled = given / chance.chance();
                    amount -= (long) scaled;
                    if (amount > 0) {
                        chance = new ChanceState((float) ((amount - (scaled % 1)) * chance.chance() / amount), true);
                    }
                }
            } else {
                EmiStack stack = ingredientStacks.get(i);
                if (stack.isEmpty()) {
                    emptyIngredient = true;
                } else {
                    amount -= getRemainder(ingredientStacks.get(i), amount, catalyst);
                }
            }
        }

        if (emptyIngredient) {
            if (trackProgress) {
                complete(node);
            }
        }


        if (amount == 0) {
            if (trackProgress) {
                complete(node);
            }
            return;
        }
        if (trackProgress && amount != original) {
            node.progress = ProgressState.PARTIAL;
        }

        long effectiveCrafts = amount;
        if (recipe != null) {
            long minBatches = (long) Math.ceil(amount / (double) node.divisor);
            effectiveCrafts = minBatches * node.divisor;
            if (trackProgress) {
                node.totalNeeded = amount;
                node.neededBatches = minBatches;
            }
            ChanceState produced = chance.produce(node.produceChance);
            for (MaterialNode n : node.children) {
                calculateCost(n, minBatches * n.amount, produced.consume(n.consumeChance), trackProgress);
            }

            if (emptyIngredient) {
                if (node.children.stream().allMatch(p -> p.progress == ProgressState.COMPLETED)){
                    complete(node);
                    return;
                }
            }

            EmiStack stack = node.ingredient.getEmiStacks().get(0);
            addRemainder(stack, effectiveCrafts - amount, produced);

            for (EmiStack es : recipe.getOutputs()) {
                if (!stack.equals(es)) {
                    addRemainder(es, minBatches * es.getAmount(), produced.consume(es.getChance()));
                }
            }

            for (MaterialNode n : node.children) {
                if (!n.remainder.isEmpty() && n.remainderAmount > 0) {
                    if (n.catalyst) {
                        addRemainder(n.remainder, n.remainderAmount, produced.consume(n.consumeChance));
                    } else {
                        addRemainder(n.remainder, minBatches * n.remainderAmount, produced.consume(n.consumeChance));
                    }
                }
            }
        } else {
            addCost(node.ingredient, amount, node.amount, chance);
        }
    }

    @Shadow
    protected abstract double getChancedRemainder(EmiStack stack, double desired, boolean catalyst, ChanceState chance);

    @Shadow
    protected abstract void addCost(EmiIngredient stack, long amount, long minBatch, ChanceState chance);

    @Shadow
    protected abstract void addRemainder(EmiStack stack, long amount, ChanceState chance);

    @Shadow
    protected abstract long getRemainder(EmiStack stack, long desired, boolean catalyst);

    @Shadow
    protected abstract void complete(MaterialNode node);
}
