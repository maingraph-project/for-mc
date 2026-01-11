package ltd.opens.mg.mc.core.blueprint;

/**
 * 节点系统视觉主题常量池，用于统一节点分类和端口类型的颜色。
 * 实现“一处修改，全局生效”，确保视觉风格高度一致。
 */
public class NodeThemes {
    // --- 节点分类颜色 ---
    public static final int COLOR_NODE_EVENT = 0xFF880000;    // 深红 - 事件
    public static final int COLOR_NODE_ACTION = 0xFFCC3333;   // 浅红 - 动作
    public static final int COLOR_NODE_LOGIC = 0xFF4444FF;    // 蓝色 - 逻辑
    public static final int COLOR_NODE_MATH = 0xFF008800;     // 绿色 - 数学
    public static final int COLOR_NODE_VARIABLE = 0xFFFFAA00; // 橙色 - 变量/常量
    public static final int COLOR_NODE_CONVERSION = 0xFF666666; // 灰色 - 转换
    public static final int COLOR_NODE_ENTITY = 0xFF33CCCC;   // 青色 - 实体属性
    public static final int COLOR_NODE_LIST = 0xFF00AAAA;     // 深青 - 列表操作
    public static final int COLOR_NODE_STRING = 0xFFCCAA00;   // 暗金 - 字符串操作
    public static final int COLOR_NODE_CONTROL = 0xFF4444FF;  // 蓝色 - 控制流 (与逻辑一致)
    public static final int COLOR_NODE_COMMENT = 0xFF448844;  // 灰绿 - 标记/注释

    // --- 端口类型颜色 ---
    public static final int COLOR_PORT_EXEC = 0xFFFFFFFF;     // 白色 - 执行流
    public static final int COLOR_PORT_STRING = 0xFFFFCC00;   // 金色 - 字符串
    public static final int COLOR_PORT_FLOAT = 0xFF55FF55;    // 亮绿 - 数值
    public static final int COLOR_PORT_BOOLEAN = 0xFF920101;  // 深红 - 布尔
    public static final int COLOR_PORT_LIST = 0xFF00FFFF;     // 青蓝 - 列表
    public static final int COLOR_PORT_UUID = 0xFFCC00FF;     // 紫色 - UUID
    public static final int COLOR_PORT_XYZ = 0xFF55FFCC;      // 蓝绿 - XYZ
    public static final int COLOR_PORT_ENTITY = 0xFF55FFFF;   // 亮青 - 实体
    public static final int COLOR_PORT_OBJECT = 0xFFFFFFFF;   // 白色 - 通用对象
    public static final int COLOR_PORT_ANY = 0xFFAAAAAA;      // 浅灰 - 任意类型
}
