package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class InventoryNodes {

    public static void register() {
        // give_item
        NodeHelper.setup("give_item", "node.mgmc.give_item.name")
            .category("node_category.mgmc.action.inventory")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/inventory/give_item")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.PLAYER, "node.mgmc.port.player", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .input(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 1)
            .input(NodePorts.NBT, "node.mgmc.port.nbt", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "{}")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.PLAYER, ctx);
                String itemId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM_ID, ctx));
                int count = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.COUNT, ctx));
                String nbtStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NBT, ctx));

                if (entityObj instanceof ServerPlayer player) {
                    try {
                        ResourceLocation rl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(rl);
                        if (item != Items.AIR) {
                            ItemStack stack = new ItemStack(item, count);
                            if (nbtStr != null && !nbtStr.isEmpty() && !nbtStr.equals("{}")) {
                                try {
                                    CompoundTag tag = TagParser.parseTag(nbtStr);
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                } catch (Exception e) {
                                    MaingraphforMC.LOGGER.error("Error parsing NBT in give_item node: " + node.get("id"), e);
                                }
                            }
                            player.getInventory().add(stack);
                        }
                    } catch (Exception e) {
                        MaingraphforMC.LOGGER.error("Error in give_item node: " + node.get("id"), e);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // clear_item
        NodeHelper.setup("clear_item", "node.mgmc.clear_item.name")
            .category("node_category.mgmc.action.inventory")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/inventory/clear_item")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.PLAYER, "node.mgmc.port.player", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .input(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 1)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.PLAYER, ctx);
                String itemId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM_ID, ctx));
                int count = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.COUNT, ctx));

                if (entityObj instanceof ServerPlayer player) {
                     try {
                        ResourceLocation rl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(rl);
                        if (item != Items.AIR) {
                             if (count <= 0) {
                                 player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == item, -1, player.inventoryMenu.getCraftSlots());
                             } else {
                                 player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == item, count, player.inventoryMenu.getCraftSlots());
                             }
                             // Sync inventory
                             player.containerMenu.broadcastChanges();
                             player.inventoryMenu.broadcastChanges();
                        }
                    } catch (Exception e) {
                        MaingraphforMC.LOGGER.error("Error in clear_item node: " + node.get("id"), e);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // get_item_count
        NodeHelper.setup("get_item_count", "node.mgmc.get_item_count.name")
            .category("node_category.mgmc.data.inventory")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/data/inventory/get_item_count")
            .input(NodePorts.PLAYER, "node.mgmc.port.player", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .output(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT)
            .registerValue((node, port, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.PLAYER, ctx);
                String itemId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM_ID, ctx));
                
                if (entityObj instanceof ServerPlayer player) {
                     try {
                        ResourceLocation rl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(rl);
                        if (item != Items.AIR) {
                            return player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == item, 0, player.inventoryMenu.getCraftSlots());
                        }
                    } catch (Exception e) {
                        MaingraphforMC.LOGGER.error("Error in get_item_count node: " + node.get("id"), e);
                    }
                }
                return 0;
            });

        // has_item
        NodeHelper.setup("has_item", "node.mgmc.has_item.name")
            .category("node_category.mgmc.data.inventory")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/data/inventory/has_item")
            .input(NodePorts.PLAYER, "node.mgmc.port.player", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, port, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.PLAYER, ctx);
                String itemId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM_ID, ctx));
                
                if (entityObj instanceof ServerPlayer player) {
                     try {
                        ResourceLocation rl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(rl);
                        if (item != Items.AIR) {
                            return player.getInventory().contains(new ItemStack(item));
                        }
                    } catch (Exception e) {
                        MaingraphforMC.LOGGER.error("Error in has_item node: " + node.get("id"), e);
                    }
                }
                return false;
            });
    }
}