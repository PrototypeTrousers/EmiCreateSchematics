package net.liukrast.schematicdisplay;

import com.simibubi.create.AllBlocks;
import net.liukrast.schematicdisplay.network.ExtractItemPayload;
import net.liukrast.schematicdisplay.network.NetworkEventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(EMICreateSchematics.MOD_ID)
public class EMICreateSchematics {


    public static final String MOD_ID = "emi_create_schematics";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final Supplier<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            AllBlocks.CLIPBOARD
    );

    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EMICreateSchematics(IEventBus modBus) {
        ITEMS.register(modBus);
    }



}
