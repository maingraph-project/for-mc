package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VectorNodes {

    public static void register() {
        // 1. vector_add
        NodeHelper.setup("vector_add", "node.mgmc.vector_add.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_add")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object bObj = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (aObj instanceof XYZ a && bObj instanceof XYZ b) {
                    return new XYZ(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
                }
                return XYZ.ZERO;
            });

        // 2. vector_sub
        NodeHelper.setup("vector_sub", "node.mgmc.vector_sub.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_sub")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object bObj = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (aObj instanceof XYZ a && bObj instanceof XYZ b) {
                    return new XYZ(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
                }
                return XYZ.ZERO;
            });

        // 3. vector_mul
        NodeHelper.setup("vector_mul", "node.mgmc.vector_mul.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_mul")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0f)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                if (aObj instanceof XYZ a) {
                    return new XYZ(a.x() * b, a.y() * b, a.z() * b);
                }
                return XYZ.ZERO;
            });

        // 4. vector_div
        NodeHelper.setup("vector_div", "node.mgmc.vector_div.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_div")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0f)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                if (aObj instanceof XYZ a && b != 0) {
                    return new XYZ(a.x() / b, a.y() / b, a.z() / b);
                }
                return XYZ.ZERO;
            });

        // 5. vector_normalize
        NodeHelper.setup("vector_normalize", "node.mgmc.vector_normalize.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_normalize")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                if (aObj instanceof XYZ a) {
                    Vec3 v = new Vec3(a.x(), a.y(), a.z()).normalize();
                    return new XYZ(v.x, v.y, v.z);
                }
                return XYZ.ZERO;
            });

        // 6. vector_length
        NodeHelper.setup("vector_length", "node.mgmc.vector_length.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_length")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                if (aObj instanceof XYZ a) {
                    return Math.sqrt(a.x() * a.x() + a.y() * a.y() + a.z() * a.z());
                }
                return 0.0;
            });

        // 7. vector_dot
        NodeHelper.setup("vector_dot", "node.mgmc.vector_dot.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_dot")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object bObj = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (aObj instanceof XYZ a && bObj instanceof XYZ b) {
                    return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
                }
                return 0.0;
            });

        // 8. vector_cross
        NodeHelper.setup("vector_cross", "node.mgmc.vector_cross.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_cross")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object bObj = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (aObj instanceof XYZ a && bObj instanceof XYZ b) {
                    return new XYZ(
                        a.y() * b.z() - a.z() * b.y(),
                        a.z() * b.x() - a.x() * b.z(),
                        a.x() * b.y() - a.y() * b.x()
                    );
                }
                return XYZ.ZERO;
            });

        // 9. vector_distance
        NodeHelper.setup("vector_distance", "node.mgmc.vector_distance.name")
            .category("node_category.mgmc.logic.vector")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/vector/vector_distance")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.RESULT, "node.mgmc.port.result", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                Object aObj = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object bObj = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (aObj instanceof XYZ a && bObj instanceof XYZ b) {
                    double dx = a.x() - b.x();
                    double dy = a.y() - b.y();
                    double dz = a.z() - b.z();
                    return Math.sqrt(dx * dx + dy * dy + dz * dz);
                }
                return 0.0;
            });
    }
}
