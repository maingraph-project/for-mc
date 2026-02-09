package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientActionResponsePayload(String blueprintName, String nodeId, String data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientActionResponsePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "client_action_response"));
    
    public static final StreamCodec<FriendlyByteBuf, ClientActionResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClientActionResponsePayload::blueprintName,
            ByteBufCodecs.STRING_UTF8,
            ClientActionResponsePayload::nodeId,
            ByteBufCodecs.STRING_UTF8,
            ClientActionResponsePayload::data,
            ClientActionResponsePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
