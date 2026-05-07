package net.liukrast.schematicdisplay.clipboard;

import com.google.common.collect.Lists;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.liukrast.schematicdisplay.EMICreateSchematics.*;
import static net.liukrast.schematicdisplay.SchematicPlugin.CLIPBOARD;
import static net.minecraft.core.component.DataComponents.CUSTOM_NAME;

public final class ClipboardScreenUtils {
    public static void load(List<List<ClipboardEntry>> pages) {
        Minecraft.getInstance().setScreen(null);

        ClipboardRecipe cr = new ClipboardRecipe(CLIPBOARD, ResourceLocation.fromNamespaceAndPath(MOD_ID, "/schematic/clipboard"), 0, 0);

        for (var page : pages) {
            for (var entry : page) {
                if (entry.icon.isEmpty()) {
                    continue;
                }
                ItemStack stack1 = entry.icon.copy();
                stack1.setCount(entry.itemAmount);
                cr.getInputs().add(EmiStack.of(stack1));
            }
        }
        ItemStack out = Minecraft.getInstance().player.getMainHandItem().copyWithCount(1);
        cr.getOutputs().add(EmiStack.of(out));
        setGoal(cr);
    }


    private static void setGoal(EmiRecipe recipe) {
        BoM.tree = new ChildlessMaterialTree(recipe);
        BoM.craftingMode = true;
    }

    public static class ChildlessMaterialTree extends MaterialTree {
        public ChildlessMaterialTree(EmiRecipe recipe) {
            super(recipe);
            EmiStack output = recipe.getOutputs().get(0);
            goal = new ParentOnlyMaterialNode(output);
            goal.defineRecipe(recipe);
            goal.recalculate(this);
        }
    }

    public static class ChildlessMaterialNode extends MaterialNode {
        public ChildlessMaterialNode(EmiIngredient ingredient) {
            super(ingredient);
            children = List.of();
        }

        @Override
        public void defineRecipe(EmiRecipe recipe) {
        }
    }

    public static class ParentOnlyMaterialNode extends MaterialNode {
        public ParentOnlyMaterialNode(EmiIngredient ingredient) {
            super(ingredient);
        }

        @Override
        public void defineRecipe(EmiRecipe recipe) {
            produceChance = 1;
            if (recipe == null) {
                return;
            }
            this.recipe = recipe;
            divisor = 0;
            for (EmiStack stack : recipe.getOutputs()) {
                if (stack.equals(ingredient)) {
                    if (divisor > 0) {
                        if (produceChance != 1 || stack.getChance() != 1) {
                            produceChance = (stack.getAmount() * stack.getChance() + divisor * produceChance) / (divisor + stack.getAmount());
                        }
                        divisor += stack.getAmount();
                    } else {
                        divisor = stack.getAmount();
                        produceChance = stack.getChance();
                    }
                }
            }
            if (divisor <= 0) {
                divisor = 1;
            }
            this.children = Lists.newArrayList();
            outer:
            for (EmiIngredient i : recipe.getInputs()) {
                EmiStack remainder = EmiStack.EMPTY;
                if (i.getEmiStacks().size() == 1) {
                    remainder = i.getEmiStacks().get(0).getRemainder();
                }
                for (MaterialNode node : children) {
                    if (EmiIngredient.areEqual(i, node.ingredient) && EmiIngredient.areEqual(remainder, node.remainder)) {
                        node.amount += i.getAmount();
                        node.remainderAmount += remainder.getAmount();
                        continue outer;
                    }
                }
                if (!i.isEmpty()) {
                    MaterialNode node = new ChildlessMaterialNode(i);
                    node.consumeChance = i.getChance();
                    children.add(node);
                }
            }
        }
    }

    public static class ClipboardRecipe extends BasicEmiRecipe {

        public ClipboardRecipe(EmiRecipeCategory category, ResourceLocation id, int width, int height) {
            super(category, id, width, height);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
        }

        @Override
        public boolean supportsRecipeTree() {
            return true;
        }

        @Override
        public boolean hideCraftable() {
            return super.hideCraftable();
        }

        @Override
        public @Nullable RecipeHolder<?> getBackingRecipe() {
            return null;
        }
    }
}