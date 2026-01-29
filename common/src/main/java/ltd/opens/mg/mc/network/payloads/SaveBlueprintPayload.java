package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SaveBlueprintPayload(String name, String data, long expectedVersion) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SaveBlueprintPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "save_blueprint"));
    
    public static final StreamCodec<FriendlyByteBuf, SaveBlueprintPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SaveBlueprintPayload::name,
            ByteBufCodecs.stringUtf8(1048576),
            SaveBlueprintPayload::data,
            ByteBufCodecs.VAR_LONG,
            SaveBlueprintPayload::expectedVersion,
            SaveBlueprintPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
