package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestBlueprintListPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestBlueprintListPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "request_blueprint_list"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBlueprintListPayload> STREAM_CODEC = StreamCodec.unit(new RequestBlueprintListPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
