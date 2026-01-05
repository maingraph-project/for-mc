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
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.commands.CommandSourceStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Mod(MaingraphforMC.MODID)
public class MaingraphforMC {
    public static final String MODID = "mgmc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaingraphforMC(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ltd.opens.mg.mc.network.MGMCNetwork::register);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new BlueprintServerHandler());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Maingraph for MC initialized.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mgrun")
            .requires(s -> true)
            .then(Commands.argument("blueprint", StringArgumentType.word())
                .then(Commands.argument("event", StringArgumentType.word())
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(context -> {
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
                                JsonObject blueprint = BlueprintServerHandler.getBlueprint(level, blueprintName);
                                if (blueprint != null) {
                                    BlueprintEngine.execute(level, blueprint, "on_mgrun", eventName, args, triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
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
                        String blueprintName = StringArgumentType.getString(context, "blueprint");
                        String eventName = StringArgumentType.getString(context, "event");
                        CommandSourceStack source = context.getSource();
                        ServerLevel level = source.getLevel();
                        String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                        String triggerName = source.getTextName();
                        var pos = source.getPosition();
                        try {
                            JsonObject blueprint = BlueprintServerHandler.getBlueprint(level, blueprintName);
                            if (blueprint != null) {
                                BlueprintEngine.execute(level, blueprint, "on_mgrun", eventName, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z, 0.0);
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

    public static class BlueprintServerHandler {
        private final java.util.Map<java.util.UUID, Double[]> lastPositions = new java.util.HashMap<>();
        
        private static class CachedBlueprint {
            JsonObject json;
            long lastModified;
            long version;
            CachedBlueprint(JsonObject json, long lastModified, long version) {
                this.json = json;
                this.lastModified = lastModified;
                this.version = version;
            }
        }
        
        private static final java.util.Map<String, CachedBlueprint> blueprintCache = new java.util.HashMap<>();
        private static long lastCacheRefresh = 0;
        private static final long CACHE_REFRESH_INTERVAL = 1000; // 1 second

        public static Path getBlueprintsDir(ServerLevel level) {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("mgmc_blueprints");
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (Exception e) {}
            }
            return dir;
        }

        public static JsonObject getBlueprint(ServerLevel level, String name) {
            try {
                if (!name.endsWith(".json")) name += ".json";
                Path dataFile = getBlueprintsDir(level).resolve(name);
                if (Files.exists(dataFile)) {
                    long lastModified = Files.getLastModifiedTime(dataFile).toMillis();
                    CachedBlueprint cached = blueprintCache.get(name);
                    if (cached == null || lastModified > cached.lastModified) {
                        String json = Files.readString(dataFile);
                        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                        long version = 0;
                    if (obj.has("_version") && obj.get("_version").isJsonPrimitive() && obj.get("_version").getAsJsonPrimitive().isNumber()) {
                        version = obj.get("_version").getAsLong();
                    }
                        cached = new CachedBlueprint(obj, lastModified, version);
                        blueprintCache.put(name, cached);
                    }
                    return cached.json;
                }
            } catch (Exception e) {}
            return null;
        }

        public static long getBlueprintVersion(ServerLevel level, String name) {
            if (!name.endsWith(".json")) name += ".json";
            getBlueprint(level, name); // Ensure it's in cache
            CachedBlueprint cached = blueprintCache.get(name);
            return cached != null ? cached.version : -1;
        }

        public static SaveResult saveBlueprint(ServerLevel level, String name, String data, long expectedVersion) {
            try {
                if (!name.endsWith(".json")) name += ".json";
                Path dataFile = getBlueprintsDir(level).resolve(name);
                
                long currentVersion = getBlueprintVersion(level, name);
                
                // Race condition check: if file exists and version doesn't match
                if (currentVersion != -1 && expectedVersion != -1 && currentVersion != expectedVersion) {
                    return new SaveResult(false, "Race condition detected: blueprint has been modified by another user.", currentVersion);
                }

                JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
                long newVersion = (currentVersion == -1 ? 0 : currentVersion) + 1;
                obj.addProperty("_version", newVersion);
                
                Files.writeString(dataFile, obj.toString());
                
                // Update cache
                blueprintCache.put(name, new CachedBlueprint(obj, System.currentTimeMillis(), newVersion));
                lastAllBlueprintsRefresh = 0; // Force refresh of all blueprints list
                
                return new SaveResult(true, "Saved successfully.", newVersion);
            } catch (Exception e) {
                return new SaveResult(false, "Failed to save: " + e.getMessage(), -1);
            }
        }

        public record SaveResult(boolean success, String message, long newVersion) {}

        public static void deleteBlueprint(ServerLevel level, String name) {
            try {
                if (!name.endsWith(".json")) name += ".json";
                Path dataFile = getBlueprintsDir(level).resolve(name);
                Files.deleteIfExists(dataFile);
                blueprintCache.remove(name);
                lastAllBlueprintsRefresh = 0;
            } catch (Exception e) {}
        }

        public static void renameBlueprint(ServerLevel level, String oldName, String newName) {
            try {
                if (!oldName.endsWith(".json")) oldName += ".json";
                if (!newName.endsWith(".json")) newName += ".json";
                Path oldFile = getBlueprintsDir(level).resolve(oldName);
                Path newFile = getBlueprintsDir(level).resolve(newName);
                if (Files.exists(oldFile)) {
                    Files.move(oldFile, newFile);
                    blueprintCache.remove(oldName);
                    lastAllBlueprintsRefresh = 0;
                }
            } catch (Exception e) {}
        }

        private static java.util.List<JsonObject> allBlueprintsCache = new java.util.ArrayList<>();
        private static long lastAllBlueprintsRefresh = 0;

        public static java.util.Collection<JsonObject> getAllBlueprints(ServerLevel level) {
            long now = System.currentTimeMillis();
            if (now - lastAllBlueprintsRefresh < CACHE_REFRESH_INTERVAL) {
                return allBlueprintsCache;
            }

            java.util.List<JsonObject> all = new java.util.ArrayList<>();
            try {
                Path dir = getBlueprintsDir(level);
                try (var stream = Files.list(dir)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        String name = p.getFileName().toString();
                        JsonObject bp = getBlueprint(level, name);
                        if (bp != null) all.add(bp);
                    });
                }
                allBlueprintsCache = all;
                lastAllBlueprintsRefresh = now;
            } catch (Exception e) {}
            return all;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            if (server.getTickCount() % 5 != 0) return;

            for (var player : server.getPlayerList().getPlayers()) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                java.util.UUID uuid = player.getUUID();

                Double[] lastPos = lastPositions.get(uuid);
                if (lastPos != null) {
                    double dx = x - lastPos[0];
                    double dy = y - lastPos[1];
                    double dz = z - lastPos[2];
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq > 0.25) {
                        try {
                            double speed = Math.sqrt(distSq) / 5.0; // Distance over 5 ticks
                            ServerLevel level = (ServerLevel) player.level();
                            for (JsonObject blueprint : getAllBlueprints(level)) {
                                BlueprintEngine.execute(level, blueprint, "on_player_move", "", new String[0], 
                                    uuid.toString(), player.getName().getString(), x, y, z, speed);
                            }
                            lastPositions.put(uuid, new Double[]{x, y, z});
                        } catch (Exception e) {}
                    }
                } else {
                    lastPositions.put(uuid, new Double[]{x, y, z});
                }
            }

            if (server.getTickCount() % 100 == 0) {
                lastPositions.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
            }
        }

        @SubscribeEvent
        public void onBlockBreak(BlockEvent.BreakEvent event) {
            if (event.getLevel() instanceof ServerLevel level) {
                String blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString();
                String uuid = event.getPlayer().getUUID().toString();
                String name = event.getPlayer().getName().getString();
                var pos = event.getPos();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_break_block", "", new String[0], 
                        uuid, name, pos.getX(), pos.getY(), pos.getZ(), 0.0, blockId, "", 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Player player) {
                String blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString();
                String uuid = player.getUUID().toString();
                String name = player.getName().getString();
                var pos = event.getPos();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_place_block", "", new String[0], 
                        uuid, name, pos.getX(), pos.getY(), pos.getZ(), 0.0, blockId, "", 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
            if (event.getLevel() instanceof ServerLevel level) {
                String blockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(event.getPos()).getBlock()).toString();
                String uuid = event.getEntity().getUUID().toString();
                String name = event.getEntity().getName().getString();
                var pos = event.getPos();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_interact_block", "", new String[0], 
                        uuid, name, pos.getX(), pos.getY(), pos.getZ(), 0.0, blockId, "", 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                String uuid = event.getEntity().getUUID().toString();
                String name = event.getEntity().getName().getString();
                var pos = event.getEntity().position();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_player_join", "", new String[0], 
                        uuid, name, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                String victimUuid = event.getEntity().getUUID().toString();
                String victimName = event.getEntity().getName().getString();
                String attackerUuid = event.getSource().getEntity() != null ? event.getSource().getEntity().getUUID().toString() : "";
                var pos = event.getEntity().position();

                if (event.getEntity() instanceof Player) {
                    for (JsonObject blueprint : getAllBlueprints(level)) {
                        BlueprintEngine.execute(level, blueprint, "on_player_death", "", new String[0], 
                            victimUuid, victimName, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, attackerUuid);
                    }
                }

                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_entity_death", "", new String[0], 
                        victimUuid, victimName, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, attackerUuid);
                }
            }
        }

        @SubscribeEvent
        public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                String uuid = event.getEntity().getUUID().toString();
                String name = event.getEntity().getName().getString();
                var pos = event.getEntity().position();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_player_respawn", "", new String[0], 
                        uuid, name, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onLivingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                String victimUuid = event.getEntity().getUUID().toString();
                String victimName = event.getEntity().getName().getString();
                String attackerUuid = event.getSource().getEntity() != null ? event.getSource().getEntity().getUUID().toString() : "";
                double amount = event.getAmount();
                var pos = event.getEntity().position();

                if (event.getEntity() instanceof Player) {
                    for (JsonObject blueprint : getAllBlueprints(level)) {
                        BlueprintEngine.execute(level, blueprint, "on_player_hurt", "", new String[0], 
                            victimUuid, victimName, pos.x, pos.y, pos.z, 0.0, "", "", amount, attackerUuid);
                    }
                }

                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_entity_hurt", "", new String[0], 
                        victimUuid, victimName, pos.x, pos.y, pos.z, 0.0, "", "", amount, attackerUuid);
                }
            }
        }

        @SubscribeEvent
        public void onUseItem(PlayerInteractEvent.RightClickItem event) {
            if (event.getLevel() instanceof ServerLevel level) {
                String itemId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString();
                String uuid = event.getEntity().getUUID().toString();
                String name = event.getEntity().getName().getString();
                var pos = event.getEntity().position();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_use_item", "", new String[0], 
                        uuid, name, pos.x, pos.y, pos.z, 0.0, "", itemId, 0.0, "");
                }
            }
        }

        @SubscribeEvent
        public void onPlayerAttack(AttackEntityEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                String attackerUuid = event.getEntity().getUUID().toString();
                String attackerName = event.getEntity().getName().getString();
                String victimUuid = event.getTarget().getUUID().toString();
                var pos = event.getEntity().position();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_player_attack", "", new String[0], 
                        attackerUuid, attackerName, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, victimUuid);
                }
            }
        }

        @SubscribeEvent
        public void onEntitySpawn(EntityJoinLevelEvent event) {
            if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof LivingEntity) {
                String uuid = event.getEntity().getUUID().toString();
                String name = event.getEntity().getName().getString();
                var pos = event.getEntity().position();
                for (JsonObject blueprint : getAllBlueprints(level)) {
                    BlueprintEngine.execute(level, blueprint, "on_entity_spawn", "", new String[0], 
                        uuid, name, pos.x, pos.y, pos.z, 0.0, "", "", 0.0, "");
                }
            }
        }
    }
}
