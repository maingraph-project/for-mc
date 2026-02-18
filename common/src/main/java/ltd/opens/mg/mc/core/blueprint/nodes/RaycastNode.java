package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RaycastNode {

    public static void register() {
        NodeHelper.setup("raycast", "node.mgmc.raycast.name")
            .category("node_category.mgmc.variable.world")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/world/raycast")
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.DIRECTION, "node.mgmc.port.direction", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.MAX_DISTANCE, "node.mgmc.port.max_distance", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, "100.0")
            .input(NodePorts.STOP_ON_BLOCK, "node.mgmc.port.stop_on_block", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, "true")
            .input(NodePorts.STOP_ON_ENTITY, "node.mgmc.port.stop_on_entity", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, "true")
            .output(NodePorts.HIT_TYPE, "node.mgmc.port.hit_type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.HIT_POS, "node.mgmc.port.hit_pos", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.HIT_ENTITY, "node.mgmc.port.hit_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.HIT_BLOCK_POS, "node.mgmc.port.hit_block_pos", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.POINTS, "node.mgmc.port.points", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .registerValue((node, pinId, ctx) -> {
                if (ctx.level == null) return null;

                // Inputs
                XYZ origin = (XYZ) NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx);
                if (origin == null) origin = XYZ.ZERO;
                
                XYZ direction = (XYZ) NodeLogicRegistry.evaluateInput(node, NodePorts.DIRECTION, ctx);
                if (direction == null) direction = new XYZ(0, 1, 0); // Default UP
                
                double maxDist = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MAX_DISTANCE, ctx));
                if (maxDist <= 0) maxDist = 100.0;
                
                // Use default true if not provided or parsing fails, but check if user explicitly set to false?
                // TypeConverter.toBoolean returns false for null/empty.
                // But the input definition has default "true".
                // If the user's blueprint has "value": false, evaluateInput returns false.
                // If the user's blueprint does NOT have the input, evaluateInput uses default "true".
                // The problem is the user's blueprint explicitly has false.
                // But to be user friendly, if BOTH are false, maybe we should enable both or block?
                // Or maybe the user really wants to scan nothing? Unlikely.
                // Let's change the logic: if both are false, treat as both true (fallback)?
                // Or just assume user made a mistake.
                
                boolean stopOnBlock = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.STOP_ON_BLOCK, ctx));
                boolean stopOnEntity = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.STOP_ON_ENTITY, ctx));

                // Auto-fix: If both are false, enable both by default to avoid confusion
                if (!stopOnBlock && !stopOnEntity) {
                    stopOnBlock = true;
                    stopOnEntity = true;
                }

                // Calculation
                Vec3 start = new Vec3(origin.x(), origin.y(), origin.z());
                Vec3 dir = new Vec3(direction.x(), direction.y(), direction.z()).normalize();
                Vec3 end = start.add(dir.scale(maxDist));
                
                HitResult finalHit = null;
                double currentDist = maxDist;
                
                // 1. Block Raycast
                if (stopOnBlock) {
                    BlockHitResult blockHit = ctx.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ctx.triggerEntity));
                    if (blockHit.getType() != HitResult.Type.MISS) {
                        finalHit = blockHit;
                        currentDist = start.distanceTo(blockHit.getLocation());
                        end = blockHit.getLocation(); // Shorten ray for entity check
                    }
                }
                
                // 2. Entity Raycast
                if (stopOnEntity) {
                    AABB area = new AABB(start, end).inflate(1.0);
                    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                        ctx.level,
                        ctx.triggerEntity,
                        start,
                        end,
                        area,
                        e -> !e.isSpectator() && e.isPickable() && (ctx.triggerEntity == null || !e.is(ctx.triggerEntity))
                    );
                    
                    if (entityHit != null) {
                        double d = start.distanceTo(entityHit.getLocation());
                        if (d < currentDist) {
                            finalHit = entityHit;
                            end = entityHit.getLocation();
                        }
                    }
                }

                // If no hit, we effectively missed or hit nothing within range
                if (finalHit == null) {
                    // Create a MISS result at max distance
                    // finalHit = BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
                }

                // Output Logic
                if (NodePorts.HIT_TYPE.equals(pinId)) {
                    if (finalHit == null || finalHit.getType() == HitResult.Type.MISS) return "MISS";
                    if (finalHit.getType() == HitResult.Type.BLOCK) return "BLOCK";
                    if (finalHit.getType() == HitResult.Type.ENTITY) return "ENTITY";
                    return "MISS";
                }
                
                if (NodePorts.HIT_POS.equals(pinId)) {
                     Vec3 pos = (finalHit != null) ? finalHit.getLocation() : end;
                     return new XYZ(pos.x, pos.y, pos.z);
                }
                
                if (NodePorts.HIT_ENTITY.equals(pinId)) {
                    if (finalHit instanceof EntityHitResult ehr) {
                        return ehr.getEntity();
                    }
                    return null;
                }
                
                if (NodePorts.HIT_BLOCK_POS.equals(pinId)) {
                    if (finalHit != null) {
                        BlockPos p = BlockPos.containing(finalHit.getLocation());
                        return new XYZ(p.getX(), p.getY(), p.getZ());
                    }
                    return XYZ.ZERO;
                }
                
                if (NodePorts.POINTS.equals(pinId)) {
                    List<XYZ> points = new ArrayList<>();
                    double step = 0.5;
                    double totalDist = start.distanceTo(end);
                    int steps = (int) (totalDist / step);
                    
                    for (int i = 0; i <= steps; i++) {
                        Vec3 p = start.add(dir.scale(i * step));
                        points.add(new XYZ(p.x, p.y, p.z));
                    }
                    // Ensure end point is included
                    points.add(new XYZ(end.x, end.y, end.z));
                    return points;
                }

                return null;
            });
    }
}
