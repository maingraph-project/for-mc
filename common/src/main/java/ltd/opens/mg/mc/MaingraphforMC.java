package ltd.opens.mg.mc;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import ltd.opens.mg.mc.core.blueprint.BlueprintManager;
import ltd.opens.mg.mc.core.blueprint.EntityVariableManager;
import ltd.opens.mg.mc.core.blueprint.GlobalVariableManager;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.Env;

public class MaingraphforMC {
    public static final String MODID = "mgmc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static BlueprintManager serverManager;
    private static GlobalVariableManager globalVariableManager;
    private static EntityVariableManager entityVariableManager;
    private static BlueprintRouter clientRouter;

    public static void init() {
        Config.init();
        ltd.opens.mg.mc.core.registry.MGMCRegistries.register();
        
        // Initialize Network
        ltd.opens.mg.mc.network.MGMCNetwork.init();
        
        // Common Setup
        ltd.opens.mg.mc.core.blueprint.NodeInitializer.init();
        ltd.opens.mg.mc.core.blueprint.EventDispatcher.init();
        ltd.opens.mg.mc.core.blueprint.events.CommonEvents.init();
        
        // Events
        LifecycleEvent.SERVER_STARTING.register(MaingraphforMC::onServerStarting);
        LifecycleEvent.SERVER_STOPPING.register(MaingraphforMC::onServerStopping);
        CommandRegistrationEvent.EVENT.register(MaingraphforMC::onRegisterCommands);
        
        TickEvent.SERVER_POST.register(server -> {
            ltd.opens.mg.mc.core.blueprint.engine.TickScheduler.tick();
        });

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            clientRouter = new BlueprintRouter();
            MaingraphforMCClient.init();
        });

        LOGGER.info("Maingraph for MC initialized.");
    }

    public static void onServerStarting(MinecraftServer server) {
        serverManager = new BlueprintManager();
        serverManager.getRouter().load(server.overworld());
        
        globalVariableManager = new GlobalVariableManager();
        globalVariableManager.load(server.overworld());
        
        entityVariableManager = new EntityVariableManager();
        entityVariableManager.load(server.overworld());
        
        LOGGER.info("MGMC: Blueprint manager, global and entity variables initialized for world.");
    }

    public static void onServerStopping(MinecraftServer server) {
        if (serverManager != null) {
            serverManager.clearCaches();
        }
        if (globalVariableManager != null) {
            globalVariableManager.save();
            globalVariableManager.clear();
        }
        if (entityVariableManager != null) {
            entityVariableManager.save();
            entityVariableManager.clear();
        }
        ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine.clearCaches();
        ltd.opens.mg.mc.core.blueprint.EventDispatcher.clear();
        
        serverManager = null;
        globalVariableManager = null;
        entityVariableManager = null;
        LOGGER.info("MGMC: Blueprint manager and global caches cleared.");
    }

    public static BlueprintManager getServerManager() {
        return serverManager;
    }

    public static GlobalVariableManager getGlobalVariableManager() {
        return globalVariableManager;
    }

    public static EntityVariableManager getEntityVariableManager() {
        return entityVariableManager;
    }

    public static BlueprintRouter getClientRouter() {
        return clientRouter;
    }

    private static boolean hasPermission(CommandSourceStack s) {
        if (s.getServer() != null && s.getEntity() instanceof ServerPlayer player) {
            return s.getServer().getProfilePermissions(player.getGameProfile()) >= 2;
        }
        return true;
    }

    public static void onRegisterCommands(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher, net.minecraft.commands.CommandBuildContext registry, net.minecraft.commands.Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("mgmc")
            .requires(MaingraphforMC::hasPermission)
            .then(Commands.literal("workbench")
                .executes(context -> {
                    if (context.getSource().getPlayer() != null) {
                        ServerPlayer player = (ServerPlayer) context.getSource().getPlayer();
                        // Open Menu using Architectury MenuRegistry if needed, or standard way if it works
                        // For MenuType<BlueprintWorkbenchMenu>, we need to open it.
                        // In Architectury, MenuRegistry.openMenu is preferred for ExtendedMenu
                        dev.architectury.registry.menu.MenuRegistry.openMenu(player, 
                            new net.minecraft.world.SimpleMenuProvider(
                                (id, inv, p) -> new ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu(id, inv),
                                Component.translatable("gui.mgmc.workbench.title")
                            )
                        );
                    }
                    return 1;
                })
            )
            .then(Commands.literal("bind")
                .then(Commands.argument("blueprint", StringArgumentType.string())
                    .executes(context -> {
                        if (context.getSource().getPlayer() != null) {
                            net.minecraft.world.entity.player.Player player = context.getSource().getPlayer();
                            net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                            if (stack.isEmpty()) {
                                context.getSource().sendFailure(Component.translatable("command.mgmc.workbench.no_item"));
                                return 0;
                            }
                            String path = StringArgumentType.getString(context, "blueprint");
                            java.util.List<String> scripts = new java.util.ArrayList<>(stack.getOrDefault(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), java.util.Collections.emptyList()));
                            if (!scripts.contains(path)) {
                                scripts.add(path);
                                stack.set(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), scripts);
                                context.getSource().sendSuccess(() -> Component.translatable("command.mgmc.workbench.bind.success", path), true);
                            } else {
                                context.getSource().sendFailure(Component.translatable("command.mgmc.workbench.already_bound"));
                            }
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("unbind")
                .then(Commands.argument("blueprint", StringArgumentType.string())
                    .executes(context -> {
                        if (context.getSource().getPlayer() != null) {
                            net.minecraft.world.entity.player.Player player = context.getSource().getPlayer();
                            net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                            if (stack.isEmpty()) return 0;
                            String path = StringArgumentType.getString(context, "blueprint");
                            java.util.List<String> scripts = new java.util.ArrayList<>(stack.getOrDefault(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), java.util.Collections.emptyList()));
                            if (scripts.remove(path)) {
                                if (scripts.isEmpty()) {
                                    stack.remove(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get());
                                } else {
                                    stack.set(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get(), scripts);
                                }
                                context.getSource().sendSuccess(() -> Component.translatable("command.mgmc.workbench.unbind.success", path), true);
                            }
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("list")
                .executes(context -> {
                    BlueprintManager manager = getServerManager();
                    if (manager != null) {
                        java.util.List<String> names = manager.getRouter().getRoutingTable().values().stream()
                            .flatMap(java.util.Set::stream)
                            .distinct()
                            .sorted()
                            .toList();
                        
                        if (names.isEmpty()) {
                            context.getSource().sendSuccess(() -> Component.translatable("command.mgmc.list.empty"), false);
                        } else {
                            context.getSource().sendSuccess(() -> Component.translatable("command.mgmc.list.header", names.size()), false);
                            for (String name : names) {
                                context.getSource().sendSuccess(() -> Component.translatable("command.mgmc.list.item", name), false);
                            }
                        }
                    }
                    return 1;
                })
            )
            .then(Commands.literal("log")
                .executes(context -> {
                    BlueprintManager manager = getServerManager();
                    if (manager != null) {
                        java.util.List<BlueprintManager.LogEntry> logs = manager.getLogs();
                        if (logs.isEmpty()) {
                            context.getSource().sendSuccess(() -> Component.literal("§7[MGMC] No logs available."), false);
                        } else {
                            context.getSource().sendSuccess(() -> Component.literal("§6--- MGMC Runtime Logs (Last " + logs.size() + ") ---"), false);
                            for (var log : logs) {
                                String color = log.level().equals("ERROR") ? "§c" : "§f";
                                String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(log.timestamp()));
                                context.getSource().sendSuccess(() -> Component.literal(
                                    String.format("§8[%s] %s[%s] §7(%s) §f%s", 
                                        time, color, log.level(), log.blueprintName(), log.message())
                                ), false);
                            }
                        }
                    }
                    return 1;
                })
            )
        );

        dispatcher.register(Commands.literal("mgrun")
            .requires(s -> true)
            .then(Commands.argument("blueprint", StringArgumentType.word())
                .then(Commands.argument("event", StringArgumentType.word())
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(context -> {
                            if (serverManager == null) return 0;
                            String blueprintName = StringArgumentType.getString(context, "blueprint");
                            String eventName = StringArgumentType.getString(context, "event");
                            String argsStr = StringArgumentType.getString(context, "args");
                            String[] args = argsStr.split("\\s+");
                            
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();
                            String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                            String triggerName = source.getTextName();
                            var pos = source.getPosition();
                            
                            try {
                                JsonObject blueprint = serverManager.getBlueprint(level, blueprintName);
                                if (blueprint != null) {
                                    BlueprintEngine.execute(level, blueprint, blueprintName, "mgmc:on_mgrun", eventName, args, triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
                                } else {
                                    context.getSource().sendFailure(Component.translatable("command.mgmc.mgrun.blueprint_not_found", blueprintName));
                                }
                            } catch (Exception e) {
                                context.getSource().sendFailure(Component.translatable("command.mgmc.mgrun.failed", e.getMessage()));
                            }
                            return 1;
                        })
                    )
                    .executes(context -> {
                        if (serverManager == null) return 0;
                        String blueprintName = StringArgumentType.getString(context, "blueprint");
                        String eventName = StringArgumentType.getString(context, "event");
                        CommandSourceStack source = context.getSource();
                        ServerLevel level = source.getLevel();
                        String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                        String triggerName = source.getTextName();
                        var pos = source.getPosition();
                        try {
                            JsonObject blueprint = serverManager.getBlueprint(level, blueprintName);
                            if (blueprint != null) {
                                BlueprintEngine.execute(level, blueprint, blueprintName, "mgmc:on_mgrun", eventName, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
                            } else {
                                context.getSource().sendFailure(Component.translatable("command.mgmc.mgrun.blueprint_not_found", blueprintName));
                            }
                        } catch (Exception e) {
                            context.getSource().sendFailure(Component.translatable("command.mgmc.mgrun.failed", e.getMessage()));
                        }
                        return 1;
                    })
                )
            )
        );
    }
}
