package ltd.opens.mg.mc.network;

import ltd.opens.mg.mc.network.payloads.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MGMCNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        // Client -> Server
        registrar.playToServer(
            RequestBlueprintListPayload.TYPE,
            RequestBlueprintListPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleRequestList
        );
        registrar.playToServer(
            RequestBlueprintDataPayload.TYPE,
            RequestBlueprintDataPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleRequestData
        );
        registrar.playToServer(
            SaveBlueprintPayload.TYPE,
            SaveBlueprintPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleSave
        );
        registrar.playToServer(
            DeleteBlueprintPayload.TYPE,
            DeleteBlueprintPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleDelete
        );
        registrar.playToServer(
            RenameBlueprintPayload.TYPE,
            RenameBlueprintPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleRename
        );
        registrar.playToServer(
            DuplicateBlueprintPayload.TYPE,
            DuplicateBlueprintPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleDuplicate
        );
        registrar.playToServer(
            RequestMappingsPayload.TYPE,
            RequestMappingsPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleRequestMappings
        );
        registrar.playToServer(
            SaveMappingsPayload.TYPE,
            SaveMappingsPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleSaveMappings
        );
        registrar.playToServer(
            WorkbenchActionPayload.TYPE,
            WorkbenchActionPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Server::handleWorkbenchAction
        );

        // Server -> Client
        registrar.playToClient(
            ResponseBlueprintListPayload.TYPE,
            ResponseBlueprintListPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Client::handleResponseList
        );
        registrar.playToClient(
            ResponseBlueprintDataPayload.TYPE,
            ResponseBlueprintDataPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Client::handleResponseData
        );
        registrar.playToClient(
            SaveResultPayload.TYPE,
            SaveResultPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Client::handleSaveResult
        );
        registrar.playToClient(
            ResponseMappingsPayload.TYPE,
            ResponseMappingsPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Client::handleResponseMappings
        );
        registrar.playToClient(
            RuntimeErrorReportPayload.TYPE,
            RuntimeErrorReportPayload.STREAM_CODEC,
            BlueprintNetworkHandler.Client::handleRuntimeError
        );
    }
}
