package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class WorldNodes {

    public static void register() {
        // 1. play_sound
        NodeHelper.setup("play_sound", "node.mgmc.play_sound.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/play_sound")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.SOUND, "node.mgmc.port.sound", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:entity.experience_orb.pickup")
            .input(NodePorts.VOLUME, "node.mgmc.port.volume", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0f)
            .input(NodePorts.PITCH, "node.mgmc.port.pitch", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0f)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object xyzObj = NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                String soundId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.SOUND, ctx), ctx);
                float volume = (float)TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.VOLUME, ctx));
                float pitch = (float)TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.PITCH, ctx));
                
                if (xyzObj instanceof XYZ xyz && ctx.level != null) {
                    ResourceLocation rl = ResourceLocation.parse(soundId);
                    SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
                    if (soundEvent != null) {
                        ctx.level.playSound(null, new BlockPos((int)xyz.x(), (int)xyz.y(), (int)xyz.z()), soundEvent, SoundSource.MASTER, volume, pitch);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // 2. get_time
        NodeHelper.setup("get_time", "node.mgmc.get_time.name")
            .category("node_category.mgmc.variable.world")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/world/get_time")
            .output(NodePorts.TIME, "node.mgmc.port.time", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT)
            .registerValue((node, portId, ctx) -> {
                if (ctx.level != null) {
                    return ctx.level.getDayTime();
                }
                return 0L;
            });

        // 3. set_time
        NodeHelper.setup("set_time", "node.mgmc.set_time.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/set_time")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.TIME, "node.mgmc.port.time", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 0)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                long time = TypeConverter.toLong(NodeLogicRegistry.evaluateInput(node, NodePorts.TIME, ctx));
                if (ctx.level instanceof ServerLevel serverLevel) {
                    serverLevel.setDayTime(time);
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // 4. get_weather
        NodeHelper.setup("get_weather", "node.mgmc.get_weather.name")
            .category("node_category.mgmc.variable.world")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/world/get_weather")
            .output(NodePorts.WEATHER, "node.mgmc.port.weather", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                if (ctx.level != null) {
                    if (ctx.level.isThundering()) return "thunder";
                    if (ctx.level.isRaining()) return "rain";
                }
                return "clear";
            });

        // 5. set_weather
        NodeHelper.setup("set_weather", "node.mgmc.set_weather.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/set_weather")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.WEATHER, "node.mgmc.port.weather", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "clear")
            .input(NodePorts.DURATION, "node.mgmc.port.duration", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 6000)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                String weather = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.WEATHER, ctx), ctx);
                int duration = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.DURATION, ctx));
                
                if (ctx.level instanceof ServerLevel serverLevel) {
                    if ("clear".equalsIgnoreCase(weather)) {
                        serverLevel.setWeatherParameters(duration, 0, false, false);
                    } else if ("rain".equalsIgnoreCase(weather)) {
                         serverLevel.setWeatherParameters(0, duration, true, false);
                    } else if ("thunder".equalsIgnoreCase(weather)) {
                         serverLevel.setWeatherParameters(0, duration, true, true);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
    }
}
