package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RuntimeErrorReportPayload(String blueprintName, String nodeId, String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RuntimeErrorReportPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "runtime_error_report"));
    
    public static final StreamCodec<FriendlyByteBuf, RuntimeErrorReportPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RuntimeErrorReportPayload::blueprintName,
            ByteBufCodecs.STRING_UTF8,
            RuntimeErrorReportPayload::nodeId,
            ByteBufCodecs.STRING_UTF8,
            RuntimeErrorReportPayload::message,
            RuntimeErrorReportPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
