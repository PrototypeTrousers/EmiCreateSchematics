package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.StockKeeperTransferHandler;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (c > 9) {
            Set<Ingredient> uniqueIngredients = new HashSet<>(recipe.getIngredients());
            uniqueIngredients.removeIf(Ingredient::isEmpty);
            return uniqueIngredients.size();
        }
        return c;
    }

    @WrapOperation(method = "transferRecipeOnClient", at = @At(value = "NEW", target = "(Lnet/neoforged/neoforge/items/IItemHandlerModifiable;)Lcom/simibubi/create/foundation/blockEntity/ItemHandlerContainer;", ordinal = 0))
    ItemHandlerContainer b(IItemHandlerModifiable inv, Operation<ItemHandlerContainer> original, @Local(name = "recipe") Recipe<?> recipe) {
        return new ItemHandlerContainer(new ItemStackHandler(Math.max(recipe.getIngredients().size(), 9)));
    }
}