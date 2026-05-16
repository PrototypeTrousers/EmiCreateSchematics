package net.liukrast.schematicdisplay.mixin;

import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TMRVRecipe.class)
public interface TMRVRecipeAccessor<T> {
    @Accessor("jeiRecipe")
    T getJeiRecipe();
}
