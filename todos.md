# 节点注册系统改进建议 (详细执行计划)

## 第一阶段：基础设施与规范 (重构基础)

- [x] **提升线程安全性** 
     - **原因**：当前的 `HashMap` 在多线程环境下（如 NeoForge 并行加载或异步脚本解析）会导致死循环或数据丢失。 
     - [x] **操作**：将 `NodeRegistry.java` 和 `NodeLogicRegistry.java` 的底层 `HashMap` 替换为 `ConcurrentHashMap`。 
     - [x] **细节**：确保在并发初始化时，`register` 和 `get` 操作是线程安全的。
- [x] **引入命名空间机制**
     - **原因**：没有命名空间的 ID（如 `add_float`）极易与第三方插件冲突。确立 `modid:id` 契约是开放 API 的前提。
     - [x] **操作**：增加逻辑，自动补全节点 ID ，格式：{获取到的modid，调用注册的（不是mgmc）}:{节点id}
     - [x] **细节**：防止不同模组间的节点 ID 冲突，确立全局唯一的节点契约。
- [ ] **增加重复注册校验**
    - **原因**：静默覆盖已注册节点会导致极难排查的 Bug。显式报错能让开发者在第一时间发现 ID 冲突。
    - [ ] **操作**：在 `NodeRegistry.register` 方法头部增加 `if (REGISTRY.containsKey(id)) throw new IllegalStateException(...)`。
    - [ ] **细节**：在开发阶段尽早发现 ID 冲突，避免由于覆盖导致的逻辑混乱。

## 第二阶段：初始化流程重构 (控制力与解耦)

- [ ] **引入事件驱动的节点注册机制**
    - **原因**：目前的 `NodeInitializer` 采用硬编码的手动调用方式，扩展性差且不支持第三方模组。通过事件总线可以实现自动发现和真正的模块化解耦。
    - [ ] **操作**：定义 `MGMCNodeRegistrationEvent`（继承自 `net.neoforged.bus.api.Event`），并在 `NodeInitializer` 中发布该事件。
    - [ ] **操作**：重构现有节点类（如 `MathNodes`）使用 `@SubscribeEvent` 监听该事件进行注册。
    - [ ] **细节**：彻底摆脱对 `NodeInitializer` 的手动修改依赖，实现“插件式”节点注册。
- [ ] **消除静态初始化风险**
    - **原因**：Java 静态块的加载时机不可控。在静态块中注册会导致类加载顺序错误，甚至在 NeoForge 准备好之前就尝试访问游戏资源。
    - [ ] **操作**：移除 `NodeRegistry.java` 中的 `static {}` 块及其中调用的 `NodeInitializer.init()`。
    - [ ] **操作**：在模组主类（如 `MGMC.java`）的构造函数或 `FMLCommonSetupEvent` 中显式调用初始化方法。
    - [ ] **细节**：夺回初始化控制权，确保注册流程在可控的生命周期内执行。
- [ ] **逻辑与元数据解耦**
    - **原因**：目前的定义与逻辑强耦合。实现解耦后，纯客户端环境（如编辑器）可以只加载节点外观而不加载复杂的服务器逻辑。
    - [ ] **操作**：在 `NodeHelper` 中增加 `registerMetadataOnly()` 方法，仅调用 `NodeRegistry.register` 而不涉及 `NodeLogicRegistry`。
    - [ ] **细节**：支持“纯 UI”环境，允许在没有游戏逻辑类的情况下加载节点外观定义。
- [ ] **引入常量池管理端口 ID**
    - **原因**：大量硬编码字符串（如 `"exec"`, `"value"`）散落在代码各处，一旦拼错就会导致运行时逻辑静默失败。
    - [ ] **操作**：新建 `ltd.opens.mg.mc.core.blueprint.NodePorts` 类，定义如 `EXEC = "exec"`, `VALUE = "value"`, `A = "a"`, `B = "b"` 等常量。
    - [ ] **操作**：替换 `MathNodes.java` 和 `ControlFlowNodes.java` 中所有的硬编码字符串为上述常量。
    - [ ] **细节**：通过编译器检查拼写错误，提高重构安全性。

## 第三阶段：定义语法优化 (消除槽点与设计缺陷)

- [ ] **减少注册时的样板代码 (Boilerplate)**
    - **原因**：目前注册一个简单的加法节点需要 10 多行重复代码。过度冗余的代码降低了开发效率且掩盖了核心逻辑。
    - [ ] **操作**：在 `NodeHelper` 中新增 `registerMathOp(BiFunction<Double, Double, Double> op)` 等高阶封装方法。
    - [ ] **细节**：重构 `MathNodes.java`，将原先的冗长逻辑简化为 1-2 行，自动处理类型转换，兼容样板代码写法
- [ ] **规范化颜色与分类定义**
    - **原因**：颜色值硬编码在各个类中，且极其不统一。不仅端口颜色混乱，**不同分类的节点颜色（如动作类、逻辑类、变量类）也散落在各处**。这导致 UI 风格混乱，且难以进行全局主题调整。
    - [ ] **操作**：新建 `ltd.opens.mg.mc.core.blueprint.NodeThemes` 类，定义一套标准的调色板（Palette）。
    - [ ] **操作**：统一分类颜色：定义 `COLOR_NODE_ACTION`, `COLOR_NODE_EVENT`, `COLOR_NODE_LOGIC`, `COLOR_NODE_VARIABLE` 等标准色。
    - [ ] **操作**：统一端口颜色：定义 `COLOR_PORT_EXEC`, `COLOR_PORT_STRING`, `COLOR_PORT_FLOAT`, `COLOR_PORT_BOOLEAN` 等标准色。
    - [ ] **细节**：在所有节点类（如 `ActionNodes`, `LogicNodes`）中引用这些常量。实现“一处修改，全局生效”，确保同分类节点和同类型端口的颜色在视觉上高度一致。
- [ ] **修复执行期状态污染**
    - **原因**：直接在 `JsonObject`（节点实例）上存临时变量（如 `_index`）会导致蓝图文件被修改，且在并发或递归执行时数据会相互干扰。
    - [ ] **操作**：在 `NodeContext.java` 中增加 `Map<String, Object> runtimeData` 或类似作用域。
    - [ ] **操作**：重构 `ControlFlowNodes.java` 中的 `loop` 节点，将 `_index` 存储在 `NodeContext` 而非修改 `node` 对象的属性。
    - [ ] **细节**：彻底杜绝状态污染，确保脚本执行的纯净性和线程安全。

## 第四阶段：外部接入 (API 开放)

- [ ] **实现注册表冻结机制**
    - **原因**：允许在游戏运行时动态修改节点定义会带来不可预测的崩溃和安全隐患。
    - [ ] **操作**：在 `NodeRegistry` 中增加 `boolean frozen` 标志位。
    - [ ] **操作**：在所有初始化结束后调用 `freeze()`，使 `REGISTRY` 变为只读视图。
    - [ ] **细节**：确保运行时环境的稳定性，防止非法修改。
