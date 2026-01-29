package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SaveResultPayload(boolean success, String message, long newVersion) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SaveResultPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "save_result"));
    
    public static final StreamCodec<FriendlyByteBuf, SaveResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SaveResultPayload::success,
            ByteBufCodecs.STRING_UTF8,
            SaveResultPayload::message,
            ByteBufCodecs.VAR_LONG,
            SaveResultPayload::newVersion,
            SaveResultPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
