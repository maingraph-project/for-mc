package ltd.opens.mg.mc;

import net.neoforged.neoforge.common.ModConfigSpec;

// Config class for the mod.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<Integer> MAX_RECURSION_DEPTH_VAL = BUILDER
            .comment("Max recursion depth to prevent server crash from infinite loops")
            .define("max_recursion_depth", 10);

    private static final ModConfigSpec.ConfigValue<Integer> MAX_NODE_EXECUTIONS_VAL = BUILDER
            .comment("Max node executions per blueprint run to prevent lag from large loops")
            .define("max_node_executions", 5000);

    private static final ModConfigSpec.ConfigValue<Boolean> ALLOW_SERVER_RUN_COMMAND_NODE_VAL = BUILDER
            .comment("Whether to allow saving blueprints containing 'Run Command as Server' nodes. Disabling this improves security.")
            .define("allow_server_run_command_node", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int getMaxRecursionDepth() {
        return MAX_RECURSION_DEPTH_VAL.get();
    }

    public static int getMaxNodeExecutions() {
        return MAX_NODE_EXECUTIONS_VAL.get();
    }

    public static boolean isServerRunCommandNodeAllowed() {
        return ALLOW_SERVER_RUN_COMMAND_NODE_VAL.get();
    }
}
