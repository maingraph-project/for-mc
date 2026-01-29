package ltd.opens.mg.mc.network;

import com.google.gson.JsonObject;
import dev.architectury.networking.NetworkManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

public class BlueprintNetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static class Server {
        private static boolean hasPermission(ServerPlayer player) {
            if (player.level().getServer() == null) return false;
            return player.level().getServer().getProfilePermissions(player.getGameProfile()) >= 2;
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

        public static void handleRequestList(final RequestBlueprintListPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    MGMCNetwork.sendToPlayer(player, new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                }
            });
        }

        public static void handleRequestData(final RequestBlueprintDataPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    JsonObject bp = manager.getBlueprint((ServerLevel) player.level(), payload.name());
                    if (bp != null) {
                        long version = manager.getBlueprintVersion((ServerLevel) player.level(), payload.name());
                        MGMCNetwork.sendToPlayer(player, new ResponseBlueprintDataPayload(payload.name(), bp.toString(), version));
                    }
                }
            });
        }

        public static void handleSave(final SaveBlueprintPayload payload, final NetworkManager.PacketContext context) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                if (!hasPermission(player)) {
                    MGMCNetwork.sendToPlayer(player, new SaveResultPayload(false, "You do not have permission to save blueprints.", 0));
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
                    MGMCNetwork.sendToPlayer(player, new SaveResultPayload(result.success(), result.message(), result.newVersion()));
                });
            }
        }

        public static void handleDelete(final DeleteBlueprintPayload payload, final NetworkManager.PacketContext context) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.deleteBlueprintAsync((ServerLevel) player.level(), payload.name()).thenAccept(success -> {
                    if (success) {
                        MGMCNetwork.sendToPlayer(player, new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleRename(final RenameBlueprintPayload payload, final NetworkManager.PacketContext context) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.renameBlueprintAsync((ServerLevel) player.level(), payload.oldName(), payload.newName()).thenAccept(success -> {
                    if (success) {
                        MGMCNetwork.sendToPlayer(player, new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleDuplicate(final DuplicateBlueprintPayload payload, final NetworkManager.PacketContext context) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                if (!hasPermission(player)) return;
                var manager = MaingraphforMC.getServerManager();
                if (manager == null) return;
                manager.duplicateBlueprintAsync((ServerLevel) player.level(), payload.sourceName(), payload.targetName()).thenAccept(success -> {
                    if (success) {
                        MGMCNetwork.sendToPlayer(player, new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                    }
                });
            }
        }

        public static void handleRequestMappings(final RequestMappingsPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    MGMCNetwork.sendToPlayer(player, new ResponseMappingsPayload(manager.getRouter().getFullRoutingTable((ServerLevel) player.level())));
                }
            });
        }

        public static void handleSaveMappings(final SaveMappingsPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    manager.getRouter().updateAllMappings((ServerLevel) player.level(), payload.mappings());
                    // 广播更新？目前先简单回复
                    MGMCNetwork.sendToPlayer(player, new ResponseMappingsPayload(manager.getRouter().getFullRoutingTable((ServerLevel) player.level())));
                }
            });
        }

        public static void handleWorkbenchAction(final WorkbenchActionPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player && player.containerMenu instanceof ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu menu) {
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

        public static void handleRequestExport(final RequestExportPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    
                    JsonObject bp = manager.getBlueprint((ServerLevel) player.level(), payload.name());
                    if (bp != null) {
                        java.util.Map<String, java.util.Set<String>> relatedMappings = new java.util.HashMap<>();
                        var router = manager.getRouter();
                        router.getRoutingTable().forEach((id, blueprints) -> {
                            if (blueprints.contains(payload.name())) {
                                relatedMappings.put(id, java.util.Collections.singleton(payload.name()));
                            }
                        });
                        MGMCNetwork.sendToPlayer(player, new ResponseExportPayload(payload.name(), bp.toString(), relatedMappings));
                    }
                }
            });
        }

        public static void handleImport(final ImportBlueprintPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    var manager = MaingraphforMC.getServerManager();
                    if (manager == null) return;
                    
                    // 1. 保存蓝图逻辑
                    manager.saveBlueprintAsync((ServerLevel) player.level(), payload.name(), payload.data(), -1).thenAccept(result -> {
                        if (result.success()) {
                            // 2. 合并映射
                            var router = manager.getRouter();
                            java.util.Map<String, java.util.Set<String>> currentMappings = new java.util.HashMap<>(router.getFullRoutingTable((ServerLevel) player.level()));
                            
                            payload.mappings().forEach((id, blueprints) -> {
                                java.util.Set<String> set = currentMappings.computeIfAbsent(id, k -> new java.util.HashSet<>());
                                set.addAll(blueprints);
                            });
                            
                            router.updateAllMappings((ServerLevel) player.level(), currentMappings);
                            
                            // 3. 通知列表刷新
                            MGMCNetwork.sendToPlayer(player, new ResponseBlueprintListPayload(getBlueprintNames((ServerLevel) player.level(), true)));
                            MGMCNetwork.sendToPlayer(player, new ResponseMappingsPayload(router.getFullRoutingTable((ServerLevel) player.level())));
                        }
                    });
                }
            });
        }
    }

    public static class Client {
        public static void handleResponseList(final ResponseBlueprintListPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintSelectionScreen selectionScreen) {
                    selectionScreen.updateListFromServer(payload.blueprints());
                } else if (Minecraft.getInstance().screen instanceof BlueprintSelectionForMappingScreen mappingSelectionScreen) {
                    mappingSelectionScreen.updateListFromServer(payload.blueprints());
                } else if (Minecraft.getInstance().screen instanceof BlueprintWorkbenchScreen workbenchScreen) {
                    workbenchScreen.updateListFromServer(payload.blueprints());
                }
            });
        }

        public static void handleResponseData(final ResponseBlueprintDataPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.loadFromNetwork(payload.data(), payload.version());
                }
            });
        }

        public static void handleSaveResult(final SaveResultPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.onSaveResult(payload.success(), payload.message(), payload.newVersion());
                }
            });
        }

        public static void handleResponseMappings(final ResponseMappingsPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
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

        public static void handleResponseExport(final ResponseExportPayload payload, final NetworkManager.PacketContext context) {
            context.queue(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintSelectionScreen screen) {
                    screen.handleExportResponse(payload.name(), payload.data(), payload.relatedMappings());
                }
            });
        }

        public static void handleRuntimeError(RuntimeErrorReportPayload payload, NetworkManager.PacketContext context) {
            context.queue(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof ltd.opens.mg.mc.client.gui.screens.BlueprintScreen blueprintScreen) {
                    blueprintScreen.onRuntimeError(payload.blueprintName(), payload.nodeId(), payload.message());
                }
            });
        }
    }
}