package net.liukrast.schematicdisplay.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

import static net.liukrast.schematicdisplay.EMICreateSchematics.MOD_ID;

public record ExtractItemPayload(int containerId, int slotIndex, int amount) implements CustomPacketPayload {
    
    // Define the unique identifier for this packet
    public static final Type<ExtractItemPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "extract_item"));

    // Codec to efficiently serialize/deserialize the data
    public static final StreamCodec<ByteBuf, ExtractItemPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ExtractItemPayload::containerId,
        ByteBufCodecs.VAR_INT, ExtractItemPayload::slotIndex,
        ByteBufCodecs.VAR_INT, ExtractItemPayload::amount,
        ExtractItemPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}