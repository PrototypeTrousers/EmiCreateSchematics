package net.liukrast.schematicdisplay.clipboard;

import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.liukrast.schematicdisplay.EMICreateSchematics.*;
import static net.liukrast.schematicdisplay.SchematicPlugin.CLIPBOARD;

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
        BoM.craftingMode = true;
        BoM.tree = new GoallessMaterialTree(recipe);
    }

    public static class GoallessMaterialTree extends MaterialTree {
        public GoallessMaterialTree(EmiRecipe recipe) {
            super(recipe);
            EmiStack output = recipe.getOutputs().get(0);
            goal = new DummyMaterialNode(output);
            goal.defineRecipe(recipe);
            goal.recalculate(this);
        }
    }

    public static class DummyMaterialNode extends MaterialNode {
        public DummyMaterialNode(EmiIngredient ingredient) {
            super(ingredient);
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