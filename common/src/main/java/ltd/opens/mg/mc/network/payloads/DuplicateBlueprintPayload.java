package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DuplicateBlueprintPayload(String sourceName, String targetName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DuplicateBlueprintPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "duplicate_blueprint"));
    
    public static final StreamCodec<FriendlyByteBuf, DuplicateBlueprintPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DuplicateBlueprintPayload::sourceName,
            ByteBufCodecs.STRING_UTF8,
            DuplicateBlueprintPayload::targetName,
            DuplicateBlueprintPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
