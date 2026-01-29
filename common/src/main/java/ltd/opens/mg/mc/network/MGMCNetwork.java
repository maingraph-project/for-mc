package ltd.opens.mg.mc.network;

import dev.architectury.networking.NetworkManager;
import ltd.opens.mg.mc.network.payloads.*;
import net.minecraft.server.level.ServerPlayer;

public class MGMCNetwork {

    public static void init() {
        // Client -> Server
        NetworkManager.registerReceiver(NetworkManager.c2s(), RequestBlueprintListPayload.TYPE, RequestBlueprintListPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleRequestList);
        NetworkManager.registerReceiver(NetworkManager.c2s(), RequestBlueprintDataPayload.TYPE, RequestBlueprintDataPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleRequestData);
        NetworkManager.registerReceiver(NetworkManager.c2s(), SaveBlueprintPayload.TYPE, SaveBlueprintPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleSave);
        NetworkManager.registerReceiver(NetworkManager.c2s(), DeleteBlueprintPayload.TYPE, DeleteBlueprintPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleDelete);
        NetworkManager.registerReceiver(NetworkManager.c2s(), RenameBlueprintPayload.TYPE, RenameBlueprintPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleRename);
        NetworkManager.registerReceiver(NetworkManager.c2s(), DuplicateBlueprintPayload.TYPE, DuplicateBlueprintPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleDuplicate);
        NetworkManager.registerReceiver(NetworkManager.c2s(), RequestMappingsPayload.TYPE, RequestMappingsPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleRequestMappings);
        NetworkManager.registerReceiver(NetworkManager.c2s(), SaveMappingsPayload.TYPE, SaveMappingsPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleSaveMappings);
        NetworkManager.registerReceiver(NetworkManager.c2s(), WorkbenchActionPayload.TYPE, WorkbenchActionPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleWorkbenchAction);
        NetworkManager.registerReceiver(NetworkManager.c2s(), RequestExportPayload.TYPE, RequestExportPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleRequestExport);
        NetworkManager.registerReceiver(NetworkManager.c2s(), ImportBlueprintPayload.TYPE, ImportBlueprintPayload.STREAM_CODEC, BlueprintNetworkHandler.Server::handleImport);

        // Server -> Client
        NetworkManager.registerReceiver(NetworkManager.s2c(), ResponseBlueprintListPayload.TYPE, ResponseBlueprintListPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleResponseList);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ResponseBlueprintDataPayload.TYPE, ResponseBlueprintDataPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleResponseData);
        NetworkManager.registerReceiver(NetworkManager.s2c(), SaveResultPayload.TYPE, SaveResultPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleSaveResult);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ResponseMappingsPayload.TYPE, ResponseMappingsPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleResponseMappings);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ResponseExportPayload.TYPE, ResponseExportPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleResponseExport);
        NetworkManager.registerReceiver(NetworkManager.s2c(), RuntimeErrorReportPayload.TYPE, RuntimeErrorReportPayload.STREAM_CODEC, BlueprintNetworkHandler.Client::handleRuntimeError);
    }

    public static void sendToPlayer(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        NetworkManager.sendToPlayer(player, payload);
    }

    public static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        NetworkManager.sendToServer(payload);
    }
}
