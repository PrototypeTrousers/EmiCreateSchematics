package net.liukrast.schematicdisplay.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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

                        Slot slot = menu.getSlot(payload.slotIndex());
                        if (slot != null && slot.hasItem()) {
                            // Extract the requested amount
                            ItemStack extracted = slot.remove(payload.amount());

                            // Attempt to push it directly into the player's inventory
                            if (!player.getInventory().add(extracted)) {
                                // If their inventory is full, drop the remaining items on the ground
                                player.drop(extracted, false);
                            }
                        }
                    });
                }
        );
    }
}
