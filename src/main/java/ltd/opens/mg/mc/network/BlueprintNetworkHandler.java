package ltd.opens.mg.mc.network;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionForMappingScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintMappingScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintWorkbenchScreen;
import ltd.opens.mg.mc.network.payloads.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

public class BlueprintNetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static class Server {
        private static boolean hasPermission(ServerPlayer player) {
            if (player.level().getServer() == null) return false;
            return player.level().getServer().getProfilePermissions(new NameAndId(player.getUUID(), player.getGameProfile().name())).level().id() >= 2;
        }

        private static java.util.List<String> getBlueprintNames(ServerLevel level, boolean force) {
            var manager = MaingraphforMC.getServerManager();
            if (manager == null) return java.util.Collections.emptyList();
            
            java.util.Set<String> blueprintNames = new java.util.HashSet<>();
            
            // 1. 优先从存档目录加载
            try (var stream = java.nio.file.Files.list(manager.getBlueprintsDir(level))) {
                stream.filter(p -> p.toString().endsWith(".json"))
                        .map(p -> p.getFileName().toString())
                        .forEach(blueprintNames::add);
            } catch (Exception e) {
                LOGGER.error("Failed to list local blueprints", e);
            }

            // 2. 如果不是专服/联机模式，合并全局目录蓝图（不重复添加）
            if (!ltd.opens.mg.mc.core.blueprint.BlueprintManager.isMultiplayer(level)) {
                try (var stream = java.nio.file.Files.list(ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir())) {
                    stream.filter(p -> p.toString().endsWith(".json"))
                            .map(p -> p.getFileName().toString())
                            .forEach(blueprintNames::add);
                } catch (Exception e) {
                    LOGGER.error("Failed to list global blueprints", e);
                }
            }

            java.util.List<String> result = new java.util.ArrayList<>(blueprintNames);
            java.util.Collections.sort(result);
            return result;
        }

        public static void handleRequestList(final RequestBlueprintListPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    context.reply(new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                }
            });
        }

        public static void handleRequestData(final RequestBlueprintDataPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    JsonObject bp = manager.getBlueprint((ServerLevel) player.level(), payload.name());
                    if (bp != null) {
                        long version = manager.getBlueprintVersion((ServerLevel) player.level(), payload.name());
                        context.reply(new ResponseBlueprintDataPayload(payload.name(), bp.toString(), version));
                    }
                }
            });
        }

        public static void handleSave(final SaveBlueprintPayload payload, final IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                if (!hasPermission(player)) {
                    context.reply(new SaveResultPayload(false, "You do not have permission to save blueprints.", 0));
                    return;
                }
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.saveBlueprintAsync(
                        (ServerLevel) player.level(),
                        payload.name(),
                        payload.data(),
                        payload.expectedVersion()
                ).thenAccept(result -> {
                    context.reply(new SaveResultPayload(result.success(), result.message(), result.newVersion()));
                });
            }
        }

        public static void handleDelete(final DeleteBlueprintPayload payload, final IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.deleteBlueprintAsync((ServerLevel) player.level(), payload.name()).thenAccept(success -> {
                    if (success) {
                        context.reply(new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleRename(final RenameBlueprintPayload payload, final IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.renameBlueprintAsync((ServerLevel) player.level(), payload.oldName(), payload.newName()).thenAccept(success -> {
                    if (success) {
                        context.reply(new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleDuplicate(final DuplicateBlueprintPayload payload, final IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.duplicateBlueprintAsync((ServerLevel) player.level(), payload.sourceName(), payload.targetName()).thenAccept(success -> {
                    if (success) {
                        context.reply(new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleRequestMappings(final RequestMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    context.reply(new ResponseMappingsPayload(manager.getRouter().getFullRoutingTable((ServerLevel) player.level())));
                }
            });
        }

        public static void handleSaveMappings(final SaveMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    manager.getRouter().updateAllMappings((ServerLevel) player.level(), payload.mappings());
                    // 广播更新？目前先简单回复
                    context.reply(new ResponseMappingsPayload(manager.getRouter().getFullRoutingTable((ServerLevel) player.level())));
                }
            });
        }

        public static void handleWorkbenchAction(final WorkbenchActionPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu menu) {
                    net.minecraft.world.item.ItemStack stack = menu.getTargetItem();
                    if (stack.isEmpty()) return;

                    java.util.List<String> scripts = new java.util.ArrayList<>(stack.getOrDefault(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), java.util.Collections.emptyList()));
                    
                    if (payload.action() == WorkbenchActionPayload.Action.BIND) {
                        if (!scripts.contains(payload.blueprintPath())) {
                            scripts.add(payload.blueprintPath());
                        }
                    } else if (payload.action() == WorkbenchActionPayload.Action.UNBIND) {
                        scripts.remove(payload.blueprintPath());
                    }

                    if (scripts.isEmpty()) {
                        stack.remove(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get());
                    } else {
                        stack.set(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), scripts);
                    }
                    
                    menu.slotsChanged(null); // 通知槽位刷新
                }
            });
        }
    }

    public static class Client {
        public static void handleResponseList(final ResponseBlueprintListPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintSelectionScreen selectionScreen) {
                    selectionScreen.updateListFromServer(payload.blueprints());
                } else if (Minecraft.getInstance().screen instanceof BlueprintSelectionForMappingScreen mappingSelectionScreen) {
                    mappingSelectionScreen.updateListFromServer(payload.blueprints());
                } else if (Minecraft.getInstance().screen instanceof BlueprintWorkbenchScreen workbenchScreen) {
                    workbenchScreen.updateListFromServer(payload.blueprints());
                }
            });
        }

        public static void handleResponseData(final ResponseBlueprintDataPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.loadFromNetwork(payload.data(), payload.version());
                }
            });
        }

        public static void handleSaveResult(final SaveResultPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.onSaveResult(payload.success(), payload.message(), payload.newVersion());
                }
            });
        }

        public static void handleResponseMappings(final ResponseMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                // 更新客户端内存中的路由表
                var router = MaingraphforMC.getClientRouter();
                if (router != null) {
                    router.clientUpdateMappings(payload.mappings());
                }
                
                if (Minecraft.getInstance().screen instanceof BlueprintMappingScreen screen) {
                    screen.updateMappingsFromServer(payload.mappings());
                }
            });
        }

        public static void handleRuntimeError(RuntimeErrorReportPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof ltd.opens.mg.mc.client.gui.screens.BlueprintScreen blueprintScreen) {
                    blueprintScreen.onRuntimeError(payload.blueprintName(), payload.nodeId(), payload.message());
                }
            });
        }
    }
}
