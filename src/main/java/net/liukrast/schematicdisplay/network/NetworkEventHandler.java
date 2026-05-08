package net.liukrast.schematicdisplay.network;

import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

import static net.liukrast.schematicdisplay.EMICreateSchematics.MOD_ID;
@EventBusSubscriber(modid = MOD_ID)
public class NetworkEventHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToServer(
                ExtractItemPayload.TYPE,
                ExtractItemPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Context runs on the network thread; enqueue work to the main server thread
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        AbstractContainerMenu menu = player.containerMenu;

                        // Validate the player is looking at the correct menu to prevent cheating
                        if (menu.containerId != payload.containerId()) return;
                        int remaining = payload.amount();
                        for (int slotIndex : payload.slotIndices()) {
                            if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;
                            Slot slot = menu.getSlot(slotIndex);
                            if (slot.hasItem()) {
                                // Extract the requested amount
                                if (!menu.canTakeItemForPickAll(slot.getItem(), slot)) {
                                    continue; // Skip this slot instead of aborting the whole packet
                                }
                                if (!slot.mayPickup(player)) {
                                    continue; // Skip this slot
                                }

                                Optional<ItemStack> simextracted = slot.tryRemove(remaining, Integer.MAX_VALUE, player);
                                if (simextracted.isPresent()) {
                                    ItemStack extracted = simextracted.get();
                                    remaining -= extracted.getCount();
                                    if (!player.getInventory().add(extracted)) {
                                        // If their inventory is full, drop the remaining items on the ground
                                        player.drop(extracted, false);
                                    }
                                }
                            }
                            if (remaining == 0) {
                                return;
                            }
                        }
                    });
                }
        );
    }
}
