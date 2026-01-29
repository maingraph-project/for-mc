package ltd.opens.mg.mc.network.payloads;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WorkbenchActionPayload(Action action, String blueprintPath) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorkbenchActionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaingraphforMC.MODID, "workbench_action"));

    public enum Action {
        BIND,
        UNBIND
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchActionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT.map(i -> Action.values()[i], Action::ordinal),
        WorkbenchActionPayload::action,
        ByteBufCodecs.STRING_UTF8,
        WorkbenchActionPayload::blueprintPath,
        WorkbenchActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
