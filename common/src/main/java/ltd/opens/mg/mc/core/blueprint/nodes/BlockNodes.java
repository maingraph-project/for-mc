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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockNodes {

    public static void register() {
        // 1. get_block (获取方块)
        NodeHelper.setup("get_block", "node.mgmc.get_block.name")
            .category("node_category.mgmc.variable.world")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/world/get_block")
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                Object xyzObj = NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                if (xyzObj instanceof XYZ xyz && ctx.level != null) {
                    BlockPos pos = new BlockPos((int)xyz.x(), (int)xyz.y(), (int)xyz.z());
                    BlockState state = ctx.level.getBlockState(pos);
                    return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                }
                return "minecraft:air";
            });

        // 2. set_block (设置方块)
        NodeHelper.setup("set_block", "node.mgmc.set_block.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/set_block")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object xyzObj = NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                String blockId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.BLOCK_ID, ctx));
                
                if (xyzObj instanceof XYZ xyz && ctx.level != null) {
                    BlockPos pos = new BlockPos((int)xyz.x(), (int)xyz.y(), (int)xyz.z());
                    ResourceLocation rl = ResourceLocation.parse(blockId);
                    Block block = BuiltInRegistries.BLOCK.get(rl);
                    if (block != Blocks.AIR || blockId.equals("minecraft:air")) {
                         ctx.level.setBlock(pos, block.defaultBlockState(), 3);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // 3. break_block (破坏方块)
        NodeHelper.setup("break_block", "node.mgmc.break_block.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/break_block")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.DROP, "node.mgmc.port.drop", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object xyzObj = NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                boolean drop = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.DROP, ctx));
                
                if (xyzObj instanceof XYZ xyz && ctx.level != null) {
                    BlockPos pos = new BlockPos((int)xyz.x(), (int)xyz.y(), (int)xyz.z());
                    ctx.level.destroyBlock(pos, drop);
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
            
        // 4. fill_area (填充区域)
        NodeHelper.setup("fill_area", "node.mgmc.fill_area.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/world/fill_area")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.MIN, "node.mgmc.port.min_pos", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.MAX, "node.mgmc.port.max_pos", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object minObj = NodeLogicRegistry.evaluateInput(node, NodePorts.MIN, ctx);
                Object maxObj = NodeLogicRegistry.evaluateInput(node, NodePorts.MAX, ctx);
                String blockId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.BLOCK_ID, ctx));
                
                if (minObj instanceof XYZ min && maxObj instanceof XYZ max && ctx.level != null) {
                    ResourceLocation rl = ResourceLocation.parse(blockId);
                    Block block = BuiltInRegistries.BLOCK.get(rl);
                    if (block != Blocks.AIR || blockId.equals("minecraft:air")) {
                        BlockState state = block.defaultBlockState();
                        int startX = Math.min((int)min.x(), (int)max.x());
                        int startY = Math.min((int)min.y(), (int)max.y());
                        int startZ = Math.min((int)min.z(), (int)max.z());
                        int endX = Math.max((int)min.x(), (int)max.x());
                        int endY = Math.max((int)min.y(), (int)max.y());
                        int endZ = Math.max((int)min.z(), (int)max.z());
                        
                        // No limit
                        for (int x = startX; x <= endX; x++) {
                            for (int y = startY; y <= endY; y++) {
                                for (int z = startZ; z <= endZ; z++) {
                                    ctx.level.setBlock(new BlockPos(x, y, z), state, 2);
                                }
                            }
                        }
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
    }
}
