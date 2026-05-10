package net.liukrast.schematicdisplay.mixin;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(EmiPlayerInventory.class)
public class EmiPlayerInventoryMixin {

    /**
     * @author me
     * @reason reeee
     */
    @Overwrite
    public static EmiPlayerInventory of(Player entity) {
        if (entity != null) {
            return new EmiPlayerInventory(
                    entity.getInventory().items.stream()
                            .filter(i -> !i.isEmpty())
                            .map(EmiStack::of)
                            .toList());
        }
        return new EmiPlayerInventory(List.of());
    }
}
