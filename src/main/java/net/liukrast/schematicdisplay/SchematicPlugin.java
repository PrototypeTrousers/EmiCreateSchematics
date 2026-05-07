package net.liukrast.schematicdisplay;

import com.simibubi.create.AllBlocks;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

import static net.liukrast.schematicdisplay.EMICreateSchematics.MOD_ID;

@EmiEntrypoint
public class SchematicPlugin implements EmiPlugin {

    public static EmiRecipeCategory CLIPBOARD = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "clipboard"),
            EmiStack.of(AllBlocks.SCHEMATICANNON), (draw,x , y, delta) -> {});

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CLIPBOARD);
        registry.addWorkstation(CLIPBOARD, EmiStack.of(AllBlocks.SCHEMATICANNON));
    }
}
