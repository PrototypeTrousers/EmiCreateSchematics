package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.IntStream;

@Mixin(CraftableBigItemStack.class)
public class CraftableBigItemStackMixin {
    @WrapOperation(method = "getIngredients", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;getIngredients()Lnet/minecraft/core/NonNullList;"))
    NonNullList<Ingredient> a(Recipe instance, Operation<NonNullList<Ingredient>> original){
        if (instance instanceof SequencedAssemblyRecipe assemblyRecipe) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.add(assemblyRecipe.getIngredient());
            int loops = assemblyRecipe.getLoops();
            assemblyRecipe.getSequence().stream()
                    .map(SequencedRecipe::getRecipe)
                    .map(ProcessingRecipe::getIngredients)
                    .forEach(ing-> IntStream.range(0, loops).mapToObj(i -> ing)
                            .forEach(ingredients::addAll));
            ingredients.removeIf(i-> i.test(assemblyRecipe.getTransitionalItem()));
            return ingredients;
        }
        return original.call(instance);
    }
}
