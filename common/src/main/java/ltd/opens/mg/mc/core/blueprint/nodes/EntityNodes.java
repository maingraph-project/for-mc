package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import ltd.opens.mg.mc.MaingraphforMC;

public class EntityNodes {

    public static void register() {
        // 1. spawn_entity
        NodeHelper.setup("spawn_entity", "node.mgmc.spawn_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/entity/spawn_entity")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.ENTITY_UUID, "node.mgmc.port.entity_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:zombie")
            .input(NodePorts.NBT, "node.mgmc.port.nbt", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "{}")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .register(new ltd.opens.mg.mc.core.blueprint.engine.NodeHandler() {
                @Override
                public void execute(com.google.gson.JsonObject node, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {
                    Object xyzObj = NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                    String entityId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY_UUID, ctx), ctx); // Reuse ENTITY_UUID as ID input
                    String nbtStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NBT, ctx), ctx);
                    
                    Entity spawnedEntity = null;
                    if (xyzObj instanceof XYZ xyz && ctx.level instanceof ServerLevel serverLevel) {
                        try {
                            ResourceLocation rl = ResourceLocation.parse(entityId);
                            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
                            CompoundTag tag = null;
                            if (nbtStr != null && !nbtStr.isEmpty() && !nbtStr.equals("{}")) {
                                tag = TagParser.parseTag(nbtStr);
                            }
                            
                            spawnedEntity = type.create(serverLevel);
                            if (spawnedEntity != null) {
                                spawnedEntity.setPos(xyz.x(), xyz.y(), xyz.z());
                                if (tag != null) {
                                    CompoundTag entityTag = new CompoundTag();
                                    spawnedEntity.save(entityTag);
                                    entityTag.merge(tag);
                                    spawnedEntity.load(entityTag);
                                }
                                serverLevel.addFreshEntity(spawnedEntity);
                            }
                        } catch (Exception e) {
                            MaingraphforMC.LOGGER.error("Error spawning entity: " + entityId, e);
                        }
                    }
                    
                    // Store spawned entity for output
                    ctx.setRuntimeData(node.get("id").getAsString(), NodePorts.ENTITY, spawnedEntity);
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                }

                @Override
                public Object getValue(com.google.gson.JsonObject node, String portId, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {
                    return ctx.getRuntimeData(node.get("id").getAsString(), NodePorts.ENTITY, null);
                }
            });

        // 2. get_equipment
        NodeHelper.setup("get_equipment", "node.mgmc.get_equipment.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_equipment")
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.EQUIPMENT_SLOT, "node.mgmc.port.slot", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "mainhand")
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT)
            .output(NodePorts.NBT, "node.mgmc.port.nbt", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                String slotName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.EQUIPMENT_SLOT, ctx), ctx);
                
                if (entityObj instanceof LivingEntity entity) {
                    EquipmentSlot slot = EquipmentSlot.byName(slotName);
                    if (slot != null) {
                        ItemStack stack = entity.getItemBySlot(slot);
                        if (portId.equals(NodePorts.ITEM_ID)) return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                        if (portId.equals(NodePorts.COUNT)) return stack.getCount();
                        if (portId.equals(NodePorts.NBT)) {
                             CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                             return data != null ? data.getUnsafe().toString() : "{}";
                        }
                    }
                }
                
                if (portId.equals(NodePorts.ITEM_ID)) return "minecraft:air";
                if (portId.equals(NodePorts.COUNT)) return 0;
                return "{}";
            });

        // 3. set_equipment
        NodeHelper.setup("set_equipment", "node.mgmc.set_equipment.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/entity/set_equipment")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.EQUIPMENT_SLOT, "node.mgmc.port.slot", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "mainhand")
            .input(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:stone")
            .input(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 1)
            .input(NodePorts.NBT, "node.mgmc.port.nbt", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "{}")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                String slotName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.EQUIPMENT_SLOT, ctx), ctx);
                String itemId = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM_ID, ctx), ctx);
                int count = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.COUNT, ctx));
                String nbtStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NBT, ctx), ctx);
                
                if (entityObj instanceof LivingEntity entity) {
                    try {
                        EquipmentSlot slot = EquipmentSlot.byName(slotName);
                        ResourceLocation rl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(rl);
                        
                        if (slot != null && item != Items.AIR) {
                            ItemStack stack = new ItemStack(item, count);
                            if (nbtStr != null && !nbtStr.isEmpty() && !nbtStr.equals("{}")) {
                                try {
                                    CompoundTag tag = TagParser.parseTag(nbtStr);
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                } catch (Exception e) {
                                    MaingraphforMC.LOGGER.error("Error parsing NBT in set_equipment: " + node.get("id"), e);
                                }
                            }
                            entity.setItemSlot(slot, stack);
                        }
                    } catch (Exception e) {
                        MaingraphforMC.LOGGER.error("Error in set_equipment: " + node.get("id"), e);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
            
        // 4. ride_entity
        NodeHelper.setup("ride_entity", "node.mgmc.ride_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/entity/ride_entity")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.MOUNT, "node.mgmc.port.mount", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.RIDER, "node.mgmc.port.rider", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object mountObj = NodeLogicRegistry.evaluateInput(node, NodePorts.MOUNT, ctx);
                Object riderObj = NodeLogicRegistry.evaluateInput(node, NodePorts.RIDER, ctx);
                
                if (mountObj instanceof Entity mount && riderObj instanceof Entity rider) {
                    rider.startRiding(mount, true);
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
            
        // 5. dismount_entity
        NodeHelper.setup("dismount_entity", "node.mgmc.dismount_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/action/entity/dismount_entity")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                if (entityObj instanceof Entity entity) {
                    entity.stopRiding();
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // 6. get_look_vector
        NodeHelper.setup("get_look_vector", "node.mgmc.get_look_vector.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_look_vector")
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                if (entityObj instanceof Entity entity) {
                    net.minecraft.world.phys.Vec3 vec = entity.getLookAngle();
                    return new XYZ(vec.x, vec.y, vec.z);
                }
                return XYZ.ZERO;
            });

        // 7. get_eye_position
        NodeHelper.setup("get_eye_position", "node.mgmc.get_eye_position.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_eye_position")
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                if (entityObj instanceof Entity entity) {
                    net.minecraft.world.phys.Vec3 vec = entity.getEyePosition();
                    return new XYZ(vec.x, vec.y, vec.z);
                }
                return XYZ.ZERO;
            });
    }
}
