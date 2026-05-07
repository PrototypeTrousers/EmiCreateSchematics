package net.liukrast.schematicdisplay.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.screen.EmiScreenManager;
import net.liukrast.schematicdisplay.network.ExtractItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Unique
    private static final EmiBind grabStackToInventory = new EmiBind("key.emi.cheat_stack_to_inventory",
            new EmiBind.ModifiedKey(InputConstants.Type.MOUSE.getOrCreate(0), EmiInput.SHIFT_MASK));

    @Inject(method = "stackInteraction", at = @At(value = "FIELD", target = "Ldev/emi/emi/config/EmiConfig;cheatMode:Z", opcode = Opcodes.GETSTATIC))
    private static void a(EmiStackInteraction stack, Function<EmiBind, Boolean> function, CallbackInfoReturnable<Boolean> cir) {
        if (function.apply(grabStackToInventory)) {
            for (Slot slot : Minecraft.getInstance().player.containerMenu.slots) {
                if (slot.container == Minecraft.getInstance().player.getInventory()) {
                    continue;
                }
                for (EmiStack es : stack.getStack().getEmiStacks()) {
                    if (es.isEqual(EmiStack.of(slot.getItem()))) {
                        PacketDistributor.sendToServer(new ExtractItemPayload(
                                Minecraft.getInstance().player.containerMenu.containerId,
                                slot.index,
                                (int) stack.getStack().getAmount()
                        ));
                    }
                }
            }
        }
    }
}
