
StartupEvents.init(event => {
    const findClass = (className) => {
        try { return Java.type(className); } catch (e) {
            try { return Java.loadClass(className); } catch (e2) {
                return java.lang.Class.forName(className, true, event.class.classLoader);
            }
        }
    };

    const NodeHelper = findClass('ltd.opens.mg.mc.core.blueprint.NodeHelper');
    const NodePorts = findClass('ltd.opens.mg.mc.core.blueprint.NodePorts');
    const PortType = findClass('ltd.opens.mg.mc.core.blueprint.NodeDefinition$PortType');
    const NodeThemes = findClass('ltd.opens.mg.mc.core.blueprint.NodeThemes');
    const NodeLogicRegistry = findClass('ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry');
    const TypeConverter = findClass('ltd.opens.mg.mc.core.blueprint.engine.TypeConverter');
    const EntityType = findClass('net.minecraft.world.entity.EntityType');

    console.log('正在通过 KubeJS 注册 MGMC 自定义节点...');

    // --- 1. 注册一个动作节点：给玩家发送雷劈效果 ---
    // 使用 setup("kubejs", "id", "name") 来指定命名空间为 kubejs
    NodeHelper.setup("kubejs", "lightning", "KubeJS: 召唤闪电")
        .category("node_category.mgmc.action.player") // 设置分类
        .color(0xFFFF00) // 黄色
        .execIn()  // 添加执行输入
        .execOut() // 添加执行输出
        .input(NodePorts.ENTITY, "目标玩家", PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
        .registerExec((node, ctx) => {
            // 获取输入的实体
            let entity = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
            
            // 如果实体存在 (entity 是原始 Java 对象)
            if (entity) {
                // KubeJS 会将 Java 的 getLevel() 包装成 level 属性
                // 报错提示 level 是 object 而不是 function，所以我们直接用属性访问
                let level = entity.level; 
                
                // 使用 EntityType.LIGHTNING_BOLT 获取闪电类型
                let lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning) {
                    lightning.moveTo(entity.position());
                    level.addFreshEntity(lightning);
                    console.log('KubeJS 节点：已对 ' + entity.getName().getString() + ' 释放闪电');
                }
            }

            // 触发后续执行流
            NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
        });

    // --- 2. 注册一个计算节点：字符串拼接 ---
    NodeHelper.setup("kubejs", "concat", "KubeJS: 字符串拼接")
        .category("node_category.mgmc.logic.math")
        .color(0x00FF00) // 绿色
        .input("prefix", "前缀", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "[KJS] ")
        .input("content", "内容", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
        .output(NodePorts.RESULT, "结果", PortType.STRING, NodeThemes.COLOR_PORT_STRING)
        .registerValue((node, portId, ctx) => {
            // 获取输入并转换类型
            let prefix = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "prefix", ctx));
            let content = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "content", ctx));
            
            return prefix + content;
        });

    console.log('MGMC KubeJS 节点注册完成！');
});