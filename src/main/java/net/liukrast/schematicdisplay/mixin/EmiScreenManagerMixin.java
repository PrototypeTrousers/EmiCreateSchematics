package net.liukrast.schematicdisplay.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import net.liukrast.schematicdisplay.network.ExtractItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;

import static net.liukrast.schematicdisplay.EMICreateSchematics.MOD_ID;
import static net.liukrast.schematicdisplay.SchematicPlugin.CLIPBOARD;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Unique
    private static EmiCraftingRecipe DUMMY_CRAFTING_RECIPE = new EmiCraftingRecipe(Collections.EMPTY_LIST,EmiStack.EMPTY, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dummy_crafting")){
        @Override
        public boolean supportsRecipeTree() {
            return true;
        }
    };

    @Unique
    private static final EmiBind grabStackToInventory = new EmiBind("key.emi.cheat_stack_to_inventory",
            new EmiBind.ModifiedKey(InputConstants.Type.MOUSE.getOrCreate(0), EmiInput.SHIFT_MASK));

    @Unique
    private static final EmiBind addStackToCraftingTree = new EmiBind("key.emi.cheat_stack_to_inventory",
            new EmiBind.ModifiedKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_KP_ADD), 0));

    @Unique
    private static final EmiBind removeStackFromCraftingTree = new EmiBind("key.emi.cheat_stack_to_inventory",
            new EmiBind.ModifiedKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_KP_SUBTRACT), 0));

    @Inject(method = "stackInteraction", at = @At(value = "FIELD", target = "Ldev/emi/emi/config/EmiConfig;cheatMode:Z", opcode = Opcodes.GETSTATIC))
    private static void a(EmiStackInteraction stack, Function<EmiBind, Boolean> function, CallbackInfoReturnable<Boolean> cir) {
        if (function.apply(grabStackToInventory)) {
            Player player = Minecraft.getInstance().player;
            long amount;
            if (stack.getStack() instanceof EmiFavorite.Synthetic synthetic) {
                amount = synthetic.amount;
            } else {
                amount = stack.getStack().getAmount();
            }

            List<Integer> slots = new ArrayList<>();
            out:
            for (Slot slot : player.containerMenu.slots) {
                if (slot.container == player.getInventory()) {
                    continue;
                }
                if (!slot.mayPickup(player)) {
                    continue;
                }
                if (!player.containerMenu.canTakeItemForPickAll(slot.getItem(), slot)) {
                    return;
                }
                long remaining = amount;
                for (EmiStack es : stack.getStack().getEmiStacks()) {
                    if (es.isEqual(EmiStack.of(slot.getItem()))) {
                        slots.add(slot.getSlotIndex());
                        remaining -= slot.getItem().getCount();
                    }
                    if (remaining <= 0) {
                        break out;
                    }
                }
            }
            if (!slots.isEmpty()) {
                PacketDistributor.sendToServer(new ExtractItemPayload(
                        player.containerMenu.containerId,
                        slots,
                        (int) amount
                ));
            }
        }
        if (function.apply(addStackToCraftingTree)) {
            if (BoM.tree instanceof ClipboardScreenUtils.GoallessMaterialTree glt) {
                List<EmiIngredient> inputs = glt.goal.recipe.getInputs();
                boolean existing = false;
                out:
                for (EmiIngredient ingredient : inputs) {
                    for (EmiStack es : ingredient.getEmiStacks()) {
                        if (es.isEqual(stack.getStack().getEmiStacks().get(0))) {
                            ingredient.setAmount(ingredient.getAmount() + 1);
                            existing = true;
                            break out;
                        }
                    }
                }
                if (!existing) {
                    inputs.add(stack.getStack().copy().setAmount(1));
                }
                BoM.craftingMode = true;
                glt.recalculate();
                EmiFavorites.updateSynthetic(EmiPlayerInventory.of(Minecraft.getInstance().player));
            } else {
                ClipboardScreenUtils.ClipboardRecipe cr = new ClipboardScreenUtils.ClipboardRecipe(CLIPBOARD, ResourceLocation.fromNamespaceAndPath(MOD_ID, "/schematic/clipboard"), 0, 0);
                cr.getOutputs().add(EmiStack.EMPTY);
                cr.getInputs().add(stack.getStack().copy().setAmount(1));
                if (BoM.tree != null) {
                    cr.getInputs().add(BoM.tree.goal.ingredient.copy().setAmount(BoM.tree.goal.totalNeeded));
                }

                BoM.tree = new ClipboardScreenUtils.GoallessMaterialTree(cr);
                BoM.craftingMode = true;
                EmiFavorites.updateSynthetic(EmiPlayerInventory.of(Minecraft.getInstance().player));
            }
        }
        if (function.apply(removeStackFromCraftingTree)) {
            if (BoM.tree instanceof ClipboardScreenUtils.GoallessMaterialTree glt) {
                List<EmiIngredient> inputs = glt.goal.recipe.getInputs();
                out:
                for (Iterator<EmiIngredient> iterator = inputs.iterator(); iterator.hasNext(); ) {
                    EmiIngredient ingredient = iterator.next();
                    for (EmiStack es : ingredient.getEmiStacks()) {
                        if (es.isEqual(stack.getStack().getEmiStacks().get(0))) {
                            ingredient.setAmount(ingredient.getAmount() - 1);
                            if (ingredient.getAmount() <= 0) {
                                iterator.remove();
                            }
                            break out;
                        }
                    }
                }

                if (inputs.isEmpty()) {
                    BoM.tree = null;
                    BoM.craftingMode = false;
                } else {
                    BoM.craftingMode = true;
                    glt.recalculate();
                }
                EmiFavorites.updateSynthetic(EmiPlayerInventory.of(Minecraft.getInstance().player));
            }
        }
    }

    @WrapOperation(method = "renderSlotOverlays", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/recipe/handler/StandardRecipeHandler;getInputSources(Lnet/minecraft/world/inventory/AbstractContainerMenu;)Ljava/util/List;"))
    private static List<Slot> b(StandardRecipeHandler instance, AbstractContainerMenu menu, Operation<List<Slot>> original) {
        return Collections.EMPTY_LIST;
    }

    @Inject(method = "updateCraftables", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/EmiScreenManager;getSearchPanel()Ldev/emi/emi/screen/EmiScreenManager$SidebarPanel;"))
    private static void updateCraftables(CallbackInfo ci, @Local EmiPlayerInventory inv) {
        if (BoM.tree == null || !BoM.craftingMode) return;

        AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
        if (screen == null) return;

        List<? extends EmiRecipeHandler<?>> handlers = EmiRecipeFiller.getAllHandlers(screen);
        if (handlers.isEmpty() || !(handlers.get(0) instanceof StandardRecipeHandler standard)) return;

        List<Slot> slots = standard.getInputSources(screen.getMenu());
        Map<EmiStack, Long> gridItems = new HashMap<>();
        Inventory playerInv = Minecraft.getInstance().player.getInventory();

        for (Slot slot : slots) {
            if (slot.container == playerInv || !slot.hasItem()) continue;

            EmiStack onSlot = EmiStack.of(slot.getItem());
            gridItems.merge(onSlot, onSlot.getAmount(), Long::sum);
        }
        if (gridItems.isEmpty()) return;

        deductTreeIngredients(BoM.tree.goal, standard, gridItems, inv);
    }

    /**
     * Recursive helper to walk the tree and deduct supported ingredients.
     */
    private static void deductTreeIngredients(MaterialNode node, StandardRecipeHandler<?> handler, Map<EmiStack, Long> gridItems, EmiPlayerInventory inv) {
        if (node == null) return;
        EmiRecipe nr = node.recipe;
        if (nr instanceof EmiResolutionRecipe) {
            nr = DUMMY_CRAFTING_RECIPE;
        }
        if (node.recipe != null && !handler.supportsRecipe(nr)) {
            if (node.children != null) {
                for (MaterialNode child : node.children) {
                    for (EmiStack stack : child.ingredient.getEmiStacks()) {
                        if (gridItems.containsKey(stack)) {
                            long gridAmount = gridItems.get(stack);
                            inv.inventory.computeIfPresent(stack, (k, v) -> {
                                v.setAmount(v.getAmount() - gridAmount);
                                return v.getAmount() <= 0 ? null : v;
                            });
                        }
                    }
                }
            }
        }
        if (node.children != null) {
            for (MaterialNode child : node.children) {
                deductTreeIngredients(child, handler, gridItems, inv);
            }
        }
    }
}
