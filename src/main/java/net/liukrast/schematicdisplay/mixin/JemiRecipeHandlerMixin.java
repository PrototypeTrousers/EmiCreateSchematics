package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiRecipeHandler;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlotsView;
import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mixin(JemiRecipeHandler.class)
public abstract class JemiRecipeHandlerMixin implements EmiRecipeHandler {

    boolean isLoaded = ModList.get().isLoaded("toomanyrecipeviewers");
    @Final
    @Shadow
    private RecipeType type;

    @Inject(method = "getRawRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;"), cancellable = true)
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

    /**
     * @author
     * @reason
     */
    @Overwrite
    private static void addIngredients(JemiRecipeLayoutBuilder builder, List<SlotWidget> widgets, List<? extends EmiIngredient> stacks, RecipeIngredientRole role) {
        List<SlotWidget> unmatchedWidgets = new ArrayList<>(widgets);

        for(EmiIngredient ing : stacks) {
            int x = 0;
            int y = 0;

            for (Iterator<SlotWidget> iterator = unmatchedWidgets.iterator(); iterator.hasNext(); ) {
                SlotWidget unmatchedWidget = iterator.next();
                if (unmatchedWidget.getStack().equals(ing)) {
                    x = unmatchedWidget.getBounds().x();
                    y = unmatchedWidget.getBounds().y();
                    iterator.remove();
                    break;
                }
            }

            IIngredientAcceptor acceptor = builder.addSlot(role, x, y);

            for(EmiStack stack : ing.getEmiStacks()) {
                Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(stack);
                if (opt.isPresent()) {
                    ITypedIngredient<?> typed = (ITypedIngredient)opt.get();
                    acceptor.addIngredient(typed.getType(), typed.getIngredient());
                }
            }
        }

    }



    @Shadow
    protected abstract IRecipeTransferError jeiCraft(EmiRecipe recipe, EmiCraftContext context, boolean b, JemiRecipeSlotsView jemiRecipeSlotsView);
}
