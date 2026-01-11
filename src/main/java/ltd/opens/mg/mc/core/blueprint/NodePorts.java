package ltd.opens.mg.mc.core.blueprint;

/**
 * 节点端口 ID 常量池，用于规范化硬编码字符串，减少拼写错误。
 */
public class NodePorts {
    // 通用执行端口
    public static final String EXEC = "exec";
    public static final String EXEC_IN = "exec";
    public static final String EXEC_OUT = "exec";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String THEN = "then";
    public static final String COMPLETED = "completed";
    public static final String START = "start";
    public static final String END = "end";
    public static final String BREAK = "break";
    public static final String LOOP_BODY = "loop_body";
    public static final String DEFAULT = "default";
    public static final String CONTROL = "control";

    // 通用数据端口
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String RESULT = "result";
    public static final String VALUE = "value";
    public static final String CONDITION = "condition";
    
    // 数学/逻辑运算
    public static final String A = "a";
    public static final String B = "b";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String CHANCE = "chance";
    
    // 列表/数组
    public static final String LIST = "list";
    public static final String INDEX = "index";
    public static final String ITEM = "item";
    public static final String SIZE = "size";
    public static final String LENGTH = "length";
    public static final String DELIMITER = "delimiter";
    public static final String STRING = "string";
    public static final String SUBSTRING = "substring";
    public static final String OLD = "old";
    public static final String NEW = "new";
    public static final String MODE = "mode";
    
    // 实体/玩家相关
    public static final String ENTITY = "entity";
    public static final String ENTITY_UUID = "entity_uuid";
    public static final String PLAYER = "player";
    public static final String TARGET = "target";
    public static final String POSITION = "position";
    public static final String XYZ = "xyz";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";
    public static final String NAME = "name";
    public static final String UUID = "uuid";
    public static final String MESSAGE = "message";
    public static final String COMMAND = "command";
    public static final String EFFECT = "effect";
    public static final String RADIUS = "radius";
    public static final String TO_TYPE = "to_type";
    public static final String PARAMETERS = "parameters";
    public static final String TRIGGER_UUID = "trigger_uuid";
    public static final String TRIGGER_NAME = "trigger_name";
    public static final String TRIGGER_X = "trigger_x";
    public static final String TRIGGER_Y = "trigger_y";
    public static final String TRIGGER_Z = "trigger_z";
    public static final String SPEED = "speed";
    public static final String BLOCK_ID = "block_id";
    public static final String DAMAGE_AMOUNT = "damage_amount";
    public static final String HEAL_AMOUNT = "heal_amount";
    public static final String COMMENT = "comment";
    public static final String ATTACKER_UUID = "attacker_uuid";
    public static final String VICTIM_UUID = "victim_uuid";
    public static final String ATTACKER_ENTITY = "attacker_entity";
    public static final String VICTIM_ENTITY = "victim_entity";
    public static final String TRIGGER_ENTITY = "trigger_entity";
    public static final String TRIGGER_EXTRA_ENTITY = "trigger_extra_entity";
    public static final String ITEM_ID = "item_id";
    public static final String DURATION = "duration";
    public static final String AMPLIFIER = "amplifier";
    public static final String SHOW_PARTICLES = "show_particles";
    public static final String SECONDS = "seconds";
    public static final String VELOCITY = "velocity";
    
    // 实体属性
    public static final String TYPE = "type";
    public static final String REGISTRY_NAME = "registry_name";
    public static final String HEALTH = "health";
    public static final String MAX_HEALTH = "max_health";
    public static final String IS_LIVING = "is_living";
    public static final String IS_PLAYER = "is_player";
    public static final String IS_ONLINE = "is_online";
    public static final String PERMISSION_LEVEL = "permission_level";
    public static final String POS_X = "pos_x";
    public static final String POS_Y = "pos_y";
    public static final String POS_Z = "pos_z";
}
