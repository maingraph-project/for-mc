package ltd.opens.mg.mc;

import dev.architectury.platform.Platform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static int maxRecursionDepth = 10;
    private static int maxNodeExecutions = 5000;
    private static boolean allowServerRunCommandNode = true;

    public static void init() {
        Path configDir = Platform.getConfigFolder();
        Path configFile = configDir.resolve("mgmc.properties");
        
        Properties props = new Properties();
        if (Files.exists(configFile)) {
            try (var reader = Files.newBufferedReader(configFile)) {
                props.load(reader);
            } catch (IOException e) {
                MaingraphforMC.LOGGER.error("Failed to load config", e);
            }
        }
        
        try {
            maxRecursionDepth = Integer.parseInt(props.getProperty("max_recursion_depth", "10"));
        } catch (NumberFormatException e) {
            maxRecursionDepth = 10;
        }
        
        try {
            maxNodeExecutions = Integer.parseInt(props.getProperty("max_node_executions", "5000"));
        } catch (NumberFormatException e) {
            maxNodeExecutions = 5000;
        }
        
        allowServerRunCommandNode = Boolean.parseBoolean(props.getProperty("allow_server_run_command_node", "true"));
        
        // Save to ensure defaults exist
        props.setProperty("max_recursion_depth", String.valueOf(maxRecursionDepth));
        props.setProperty("max_node_executions", String.valueOf(maxNodeExecutions));
        props.setProperty("allow_server_run_command_node", String.valueOf(allowServerRunCommandNode));
        
        try (var writer = Files.newBufferedWriter(configFile)) {
            props.store(writer, "Maingraph for MC Config");
        } catch (IOException e) {
            MaingraphforMC.LOGGER.error("Failed to save config", e);
        }
    }

    public static int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    public static int getMaxNodeExecutions() {
        return maxNodeExecutions;
    }

    public static boolean isServerRunCommandNodeAllowed() {
        return allowServerRunCommandNode;
    }
}
