package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenUIPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenUIPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "open_ui"));
    
    public static final StreamCodec<FriendlyByteBuf, OpenUIPayload> STREAM_CODEC = StreamCodec.unit(new OpenUIPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
