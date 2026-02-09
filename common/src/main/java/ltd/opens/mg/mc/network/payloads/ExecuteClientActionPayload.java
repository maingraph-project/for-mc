package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExecuteClientActionPayload(String blueprintName, String nodeId, String actionType, String data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ExecuteClientActionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "execute_client_action"));
    
    public static final StreamCodec<FriendlyByteBuf, ExecuteClientActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ExecuteClientActionPayload::blueprintName,
            ByteBufCodecs.STRING_UTF8,
            ExecuteClientActionPayload::nodeId,
            ByteBufCodecs.STRING_UTF8,
            ExecuteClientActionPayload::actionType,
            ByteBufCodecs.STRING_UTF8,
            ExecuteClientActionPayload::data,
            ExecuteClientActionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
