package ltd.opens.mg.mc;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

@Mod(MaingraphforMC.MODID)
public class MaingraphforMC {
    public static final String MODID = "mgmc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static BlueprintManager serverManager;
    private static GlobalVariableManager globalVariableManager;
    private static EntityVariableManager entityVariableManager;
    private static BlueprintRouter clientRouter;
    private final IEventBus modEventBus;

    public MaingraphforMC(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        
        ltd.opens.mg.mc.core.registry.MGMCRegistries.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ltd.opens.mg.mc.network.MGMCNetwork::register);
        
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ltd.opens.mg.mc.core.blueprint.engine.TickScheduler.class);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            clientRouter = new BlueprintRouter();
        }

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 在 CommonSetup 阶段初始化节点，确保所有 Mod 均已实例化，方便跨 Mod 扩展
        ltd.opens.mg.mc.core.blueprint.NodeInitializer.init(this.modEventBus);
        
        ltd.opens.mg.mc.core.blueprint.EventDispatcher.init();
        LOGGER.info("Maingraph for MC initialized.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        serverManager = new BlueprintManager();
        serverManager.getRouter().load(event.getServer().overworld());
        
        globalVariableManager = new GlobalVariableManager();
        globalVariableManager.load(event.getServer().overworld());
        
        entityVariableManager = new EntityVariableManager();
        entityVariableManager.load(event.getServer().overworld());
        
        LOGGER.info("MGMC: Blueprint manager, global and entity variables initialized for world.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
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

    private boolean hasPermission(CommandSourceStack s) {
        if (s.getServer() != null && s.getEntity() instanceof ServerPlayer player) {
            return s.getServer().getProfilePermissions(new net.minecraft.server.players.NameAndId(player.getUUID(), player.getGameProfile().name())).level().id() >= 2;
        }
        return true;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mgmc")
            .requires(this::hasPermission)
            .then(Commands.literal("workbench")
                .executes(context -> {
                    if (context.getSource().getPlayer() != null) {
                        net.minecraft.world.entity.player.Player player = context.getSource().getPlayer();
                        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (id, inv, p) -> new ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu(id, inv),
                            Component.translatable("gui.mgmc.workbench.title")
                        ));
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
                        var logs = manager.getLogs();
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

        event.getDispatcher().register(Commands.literal("mgrun")
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
                                    // 统一使用带命名空间的事件 ID 或在 Engine 中处理
                                    BlueprintEngine.execute(level, blueprint, "mgmc:on_mgrun", eventName, args, triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
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
                                BlueprintEngine.execute(level, blueprint, "mgmc:on_mgrun", eventName, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
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
