package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ResponseBlueprintDataPayload(String name, String data, long version) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResponseBlueprintDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "response_blueprint_data"));
    
    public static final StreamCodec<FriendlyByteBuf, ResponseBlueprintDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ResponseBlueprintDataPayload::name,
            ByteBufCodecs.stringUtf8(1048576),
            ResponseBlueprintDataPayload::data,
            ByteBufCodecs.VAR_LONG,
            ResponseBlueprintDataPayload::version,
            ResponseBlueprintDataPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
