package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.math.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.logic.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.event.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.string.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.entity.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.collection.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.variable.*;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.world.*;

import java.util.HashMap;
import java.util.Map;

public class NodeLogicRegistry {
    private static final Map<String, NodeHandler> HANDLERS = new HashMap<>();

    public static void register(String typeId, NodeHandler handler) {
        HANDLERS.put(typeId, handler);
    }

    public static NodeHandler get(String typeId) {
        return HANDLERS.get(typeId);
    }

    public static Object evaluateInput(JsonObject node, String pinId, NodeContext ctx) {
        if (!node.has("inputs")) return null;
        JsonObject inputs = node.getAsJsonObject("inputs");
        if (!inputs.has(pinId)) return null;

        JsonObject input = inputs.getAsJsonObject(pinId);
        String type = input.get("type").getAsString();

        if (type.equals("value")) {
            return input.has("value") ? input.get("value").getAsString() : null;
        } else if (type.equals("link")) {
            String sourceId = input.get("nodeId").getAsString();
            String sourceSocket = input.get("socket").getAsString();
            JsonObject sourceNode = ctx.nodesMap.get(sourceId);
            if (sourceNode != null) {
                return evaluateOutput(sourceNode, sourceSocket, ctx);
            }
        }
        return null;
    }

    public static Object evaluateOutput(JsonObject node, String pinId, NodeContext ctx) {
        String type = node.has("type") ? node.get("type").getAsString() : null;
        if (type == null) return null;

        NodeHandler handler = get(type);
        if (handler != null) {
            return handler.getValue(node, pinId, ctx);
        }
        return null;
    }

    public static void triggerExec(JsonObject node, String pinId, NodeContext ctx) {
        if (!node.has("outputs")) return;
        JsonObject outputs = node.getAsJsonObject("outputs");
        if (!outputs.has(pinId)) return;

        JsonArray targets = outputs.getAsJsonArray(pinId);
        for (JsonElement t : targets) {
            JsonObject target = t.getAsJsonObject();
            String targetId = target.get("nodeId").getAsString();
            JsonObject targetNode = ctx.nodesMap.get(targetId);
            if (targetNode != null) {
                String type = targetNode.has("type") ? targetNode.get("type").getAsString() : null;
                if (type != null) {
                    NodeHandler handler = get(type);
                    if (handler != null) {
                        ctx.lastTriggeredPin = target.has("socket") ? target.get("socket").getAsString() : "exec";
                        handler.execute(targetNode, ctx);
                    }
                }
            }
        }
    }

    static {
        // Events
        register("on_mgrun", new OnMgRunHandler());
        register("on_player_move", new OnPlayerMoveHandler());
        register("on_break_block", new OnBreakBlockHandler());
        register("on_place_block", new OnPlaceBlockHandler());
        register("on_interact_block", new OnInteractBlockHandler());
        register("on_player_join", new OnPlayerJoinHandler());
        register("on_player_death", new OnPlayerDeathHandler());
        register("on_player_respawn", new OnPlayerRespawnHandler());
        register("on_player_hurt", new OnPlayerHurtHandler());
        register("on_use_item", new OnUseItemHandler());
        register("on_player_attack", new OnPlayerAttackHandler());
        register("on_entity_death", new OnEntityDeathHandler());
        register("on_entity_hurt", new OnEntityHurtHandler());
        register("on_entity_spawn", new OnEntitySpawnHandler());

        // Functions
        register("print_chat", new PrintChatHandler());
        register("run_command_as_player", new RunCommandAsPlayerHandler());
        register("print_string", new PrintStringHandler());
        register("get_list_item", new GetListItemHandler());
        register("list_add", new ListAddHandler());
        register("list_remove", new ListRemoveHandler());
        register("list_length", new ListLengthHandler());
        register("list_contains", new ListContainsHandler());
        register("list_set_item", new ListSetItemHandler());
        register("list_join", new ListJoinHandler());
        register("string_split", new StringSplitHandler());
        register("string_concat", new StringConcatHandler());
        register("string_combine", new StringCombineHandler());
        register("string_length", new StringLengthHandler());
        register("string_contains", new StringContainsHandler());
        register("string_replace", new StringReplaceHandler());
        register("string_substring", new StringSubstringHandler());
        register("string_case", new StringCaseHandler());
        register("get_entity_info", new GetEntityInfoHandler());
        register("play_effect", new PlayEffectHandler());
        register("explosion", new ExplosionHandler());

        // Logic
        register("branch", new BranchHandler());
        register("cast", new CastHandler());
        register("switch", new SwitchHandler());
        register("for_loop", new ForLoopHandler());
        register("break_loop", new BreakLoopHandler());

        // Data
        register("player_health", new PlayerHealthHandler());
        register("add_float", new AddFloatHandler());
        register("sub_float", new SubFloatHandler());
        register("mul_float", new MulFloatHandler());
        register("div_float", new DivFloatHandler());
        register("mod_float", new ModFloatHandler());
        register("abs_float", new AbsFloatHandler());
        register("min_float", new MinFloatHandler());
        register("max_float", new MaxFloatHandler());
        register("clamp_float", new ClampFloatHandler());
        register("round_float", new RoundFloatHandler());
        register("floor_float", new FloorFloatHandler());
        register("ceil_float", new CeilFloatHandler());
        register("random_float", new RandomFloatHandler());
        register("random_int", new RandomIntHandler());
        register("random_bool", new RandomBoolHandler());
        register("random_list_item", new RandomListItemHandler());
        
        // Comparison
        register("compare_eq", new CompareFloatHandler("eq"));
        register("compare_neq", new CompareFloatHandler("neq"));
        register("compare_gt", new CompareFloatHandler("gt"));
        register("compare_gte", new CompareFloatHandler("gte"));
        register("compare_lt", new CompareFloatHandler("lt"));
        register("compare_lte", new CompareFloatHandler("lte"));

        // Logic Gates
        register("logic_and", new LogicGateHandler("and"));
        register("logic_or", new LogicGateHandler("or"));
        register("logic_not", new LogicGateHandler("not"));
        register("logic_xor", new LogicGateHandler("xor"));

        register("string", new StringHandler());
        register("float", new FloatHandler());
        register("boolean", new BooleanHandler());
        register("get_variable", new GetVariableHandler());
        register("set_variable", new SetVariableHandler());
    }
}
