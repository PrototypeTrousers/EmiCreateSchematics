package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.jemi.JemiRecipeHandler;
import dev.emi.emi.jemi.impl.JemiRecipeSlotsView;
import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(JemiRecipeHandler.class)
public abstract class JemiRecipeHandlerMixin implements EmiRecipeHandler {

    boolean isLoaded = ModList.get().isLoaded("toomanyrecipeviewers");
    @Final
    @Shadow private RecipeType type;

    @Inject(method = "getRawRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;"),cancellable = true)
    void a(EmiRecipe recipe, CallbackInfoReturnable cir) {
        if (isLoaded) {
            if (this.type != null && this.type.getRecipeClass() != null) {
                if (recipe instanceof TMRVRecipe<?> tmRecipe) {
                    RecipeHolder<?> r = (RecipeHolder<?>) ((TMRVRecipeAccessor) tmRecipe).getJeiRecipe();
                    if (this.type.getRecipeClass().isAssignableFrom(r.getClass())) {
                        cir.setReturnValue(this.type.getRecipeClass().cast(r));
                        cir.cancel();
                    }
                }
            }
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext context) {
        IRecipeTransferError err = this.jeiCraft(recipe, context, false, (JemiRecipeSlotsView) null);
        if (err != null) {
            return err.getTooltip().stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).toList();
        } else {
            return List.of();
        }
    }

    @Shadow
    protected abstract IRecipeTransferError jeiCraft(EmiRecipe recipe, EmiCraftContext context, boolean b, JemiRecipeSlotsView jemiRecipeSlotsView);
}
