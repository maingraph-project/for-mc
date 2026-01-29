package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ResponseExportPayload(String name, String data, Map<String, Set<String>> relatedMappings) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResponseExportPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "response_export"));

    public static final StreamCodec<FriendlyByteBuf, ResponseExportPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        ResponseExportPayload::name,
        ByteBufCodecs.STRING_UTF8,
        ResponseExportPayload::data,
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8)),
        ResponseExportPayload::relatedMappings,
        ResponseExportPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
