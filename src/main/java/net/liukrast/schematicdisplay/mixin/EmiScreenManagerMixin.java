package net.liukrast.schematicdisplay.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import net.liukrast.schematicdisplay.clipboard.ClipboardScreenUtils;
import net.liukrast.schematicdisplay.network.ExtractItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static net.liukrast.schematicdisplay.EMICreateSchematics.MOD_ID;
import static net.liukrast.schematicdisplay.SchematicPlugin.CLIPBOARD;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
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

                ItemStack out = new ItemStack(AllBlocks.CLIPBOARD);
                ClipboardContent content = out.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
                out.set(AllDataComponents.CLIPBOARD_CONTENT, content.setType(ClipboardOverrides.ClipboardType.EDITING));
                cr.getOutputs().add(EmiStack.of(out));
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
                    EmiFavorites.updateSynthetic(EmiPlayerInventory.of(Minecraft.getInstance().player));
                }
            }
        }
    }
}
