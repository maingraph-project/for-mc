# Node Refactoring Progress (精细进度对照表)

用于追踪所有节点从旧的 `NodeRegistrar` / `NodeLogicRegistry` 分散注册模式，重构为 `NodeHelper` 统一注册模式的进度。

## 1. 核心工具准备
- [x] `NodeHelper.java` 实现
    - [x] 基础 Builder 包装与链式调用架构
    - [x] 端口添加 API (`input`, `output`)
    - [x] 执行流快捷 API (`execIn`, `execOut`)
    - [x] 属性/标记系统 (`flag`) - 用于消除硬编码
    - [x] 统一注册逻辑 (`register`) - 对接元数据与逻辑中心

## 2. 节点重构列表

### 2.1 事件节点 (Event Nodes) -> `EventNodes.java`
- [ ] `on_mgrun` (蓝图运行)
- [ ] `on_player_move` (玩家移动)
- [ ] `on_break_block` (破坏方块)
- [ ] `on_place_block` (放置方块)
- [ ] `on_interact_block` (交互方块)
- [ ] `on_player_join` (玩家加入)
- [ ] `on_player_death` (玩家死亡)
- [ ] `on_player_respawn` (玩家重生)
- [ ] `on_player_hurt` (玩家受伤)
- [ ] `on_use_item` (使用物品)
- [ ] `on_player_attack` (玩家攻击)
- [ ] `on_entity_death` (实体死亡)
- [ ] `on_entity_hurt` (实体受伤)
- [ ] `on_entity_spawn` (实体生成)

### 2.2 动作与实体节点 (Action/Entity Nodes)
- [ ] `print_chat` (聊天输出) -> `ActionNodes.java`
- [ ] `run_command_as_player` (以玩家身份运行命令) -> `ActionNodes.java`
- [ ] `play_effect` (播放特效) -> `ActionNodes.java`
- [ ] `explosion` (爆炸) -> `ActionNodes.java`
- [ ] `get_entity_info` (获取实体信息) -> **独立类** `GetEntityInfoNode.java`

### 2.3 数学运算节点 (Math Nodes) -> `MathNodes.java`
- [ ] `add_float` (+)
- [ ] `sub_float` (-)
- [ ] `mul_float` (*)
- [ ] `div_float` (/)
- [ ] `mod_float` (%)
- [ ] `abs_float` (绝对值)
- [ ] `min_float` (最小值)
- [ ] `max_float` (最大值)
- [ ] `clamp_float` (区间限制)
- [ ] `round_float` (四舍五入)
- [ ] `floor_float` (向下取整)
- [ ] `ceil_float` (向上取整)
- [ ] `random_float` (随机浮点数)
- [ ] `random_int` (随机整数)
- [ ] `random_bool` (随机布尔)

### 2.4 逻辑比较节点 (Logic Nodes) -> `LogicNodes.java`
- [ ] `compare_eq` (==)
- [ ] `compare_neq` (!=)
- [ ] `compare_gt` (>)
- [ ] `compare_gte` (>=)
- [ ] `compare_lt` (<)
- [ ] `compare_lte` (<=)
- [ ] `logic_and` (与)
- [ ] `logic_or` (或)
- [ ] `logic_not` (非)
- [ ] `logic_xor` (异或)

### 2.5 变量与常量节点 (Variable Nodes) -> `VariableNodes.java`
- [ ] `float` (浮点数常量)
- [ ] `boolean` (布尔常量)
- [ ] `string` (字符串常量)
- [ ] `get_variable` (获取变量)
- [ ] `set_variable` (设置变量)

### 2.6 控制流节点 (Control Flow Nodes) -> `ControlFlowNodes.java`
- [ ] `branch` (分支/If)
- [ ] `switch` (切换器) -> **需修复硬编码**
- [ ] `for_loop` (循环)
- [ ] `break_loop` (中断)

### 2.7 字符串操作节点 (String Nodes) -> `StringNodes.java`
- [ ] `string_concat` (拼接)
- [ ] `string_combine` (组合)
- [ ] `string_length` (长度)
- [ ] `string_contains` (包含)
- [ ] `string_replace` (替换)
- [ ] `string_substring` (子串)
- [ ] `string_case` (大小写转换)
- [ ] `string_split` (分割)

### 2.8 列表操作节点 (List Nodes) -> `ListNodes.java`
- [ ] `get_list_item` (获取元素)
- [ ] `list_add` (添加元素)
- [ ] `list_remove` (移除元素)
- [ ] `list_length` (列表长度)
- [ ] `list_contains` (包含元素)
- [ ] `list_set_item` (设置元素)
- [ ] `list_join` (合并/转字符串)
- [ ] `random_list_item` (随机元素)

### 2.9 类型转换 (Conversion) -> `ConversionNodes.java`
- [ ] `cast` (类型转换) -> **需修复硬编码**

## 3. 清理工作
- [ ] 移除 `NodeRegistrar.java` 中的旧注册逻辑
- [ ] 更新 `NodeInitializer.java` 调用新注册入口
- [ ] 验证 `NodeDefinition` 是否正确加载 I18n 和 颜色
