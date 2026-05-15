package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.StockKeeperTransferHandler;
import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StockKeeperTransferHandler.class)
public class StockKeeperTransferHandlerMixin {

    /**
     * Wraps the size() check. If the item being transferred is already in the list,
     * we return 0 so the "size >= 9" check always passes.
     */
    @WrapOperation(
            method = "transferRecipeOnClient",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I", ordinal = 0)
    )
    private int bypassSizeCheckForExistingItems(NonNullList<Ingredient> instance, Operation<Integer> original, @Local(name = "recipe") Recipe<?> recipe) {
        int c = original.call(instance);

        if (c >= 9) {
            int repeating = 0;
            for (Ingredient i : recipe.getIngredients()) {
                for (Ingredient i2 : recipe.getIngredients()) {
                    if (i == i2) {
                        continue;
                    }

                    if (i.equals(i2)) {
                        repeating++;
                        break;
                    }
                }
            }
            return c - repeating;
        }
        return c;
    }

    @WrapOperation(method = "transferRecipeOnClient", at = @At(value = "NEW", target = "(Lnet/neoforged/neoforge/items/IItemHandlerModifiable;)Lcom/simibubi/create/foundation/blockEntity/ItemHandlerContainer;", ordinal = 0))
    ItemHandlerContainer b(IItemHandlerModifiable inv, Operation<ItemHandlerContainer> original, @Local(name = "recipe") Recipe<?> recipe) {
        return new ItemHandlerContainer(new ItemStackHandler(recipe.getIngredients().size()));
    }
}