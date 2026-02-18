package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;

public class TrigNodes {

    public static void register() {
        // sin
        NodeHelper.setup("sin", "node.mgmc.sin.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/sin")
            .registerUnaryMathOp(Math::sin);

        // cos
        NodeHelper.setup("cos", "node.mgmc.cos.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/cos")
            .registerUnaryMathOp(Math::cos);

        // tan
        NodeHelper.setup("tan", "node.mgmc.tan.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/tan")
            .registerUnaryMathOp(Math::tan);

        // asin
        NodeHelper.setup("asin", "node.mgmc.asin.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/asin")
            .registerUnaryMathOp(Math::asin);

        // acos
        NodeHelper.setup("acos", "node.mgmc.acos.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/acos")
            .registerUnaryMathOp(Math::acos);

        // atan
        NodeHelper.setup("atan", "node.mgmc.atan.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/atan")
            .registerUnaryMathOp(Math::atan);

        // atan2
        NodeHelper.setup("atan2", "node.mgmc.atan2.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/atan2")
            .registerMathOp(Math::atan2);

        // to_radians
        NodeHelper.setup("to_radians", "node.mgmc.to_radians.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/to_radians")
            .registerUnaryMathOp(Math::toRadians);

        // to_degrees
        NodeHelper.setup("to_degrees", "node.mgmc.to_degrees.name")
            .category("node_category.mgmc.logic.trig")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/trig/to_degrees")
            .registerUnaryMathOp(Math::toDegrees);
    }
}
