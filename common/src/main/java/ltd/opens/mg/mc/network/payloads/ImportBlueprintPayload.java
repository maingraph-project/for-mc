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

public record ImportBlueprintPayload(String name, String data, Map<String, Set<String>> mappings) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ImportBlueprintPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "import_blueprint"));

    public static final StreamCodec<FriendlyByteBuf, ImportBlueprintPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        ImportBlueprintPayload::name,
        ByteBufCodecs.STRING_UTF8,
        ImportBlueprintPayload::data,
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8)),
        ImportBlueprintPayload::mappings,
        ImportBlueprintPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
