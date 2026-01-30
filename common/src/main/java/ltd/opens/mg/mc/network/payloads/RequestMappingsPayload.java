package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestMappingsPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestMappingsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "request_mappings"));
    
    public static final StreamCodec<FriendlyByteBuf, RequestMappingsPayload> STREAM_CODEC = StreamCodec.unit(new RequestMappingsPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
