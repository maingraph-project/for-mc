package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO;
import ltd.opens.mg.mc.client.gui.components.GuiConnection;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class HistoryManager {
    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 50;
    
    private final List<GuiNode> nodes;
    private final List<GuiConnection> connections;
    private final BlueprintState state;

    public HistoryManager(BlueprintState state) {
        this.state = state;
        this.nodes = state.nodes;
        this.connections = state.connections;
    }

    public void pushHistory() {
        String currentState = BlueprintIO.serialize(nodes, connections);
        pushHistory(currentState);
    }

    public void pushHistory(String stateJson) {
        if (stateJson != null) {
            if (!undoStack.isEmpty() && undoStack.peek().equals(stateJson)) return;
            
            undoStack.push(stateJson);
            if (undoStack.size() > MAX_HISTORY) {
                undoStack.removeLast();
            }
            redoStack.clear();
        }
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        
        String currentState = BlueprintIO.serialize(nodes, connections);
        if (currentState != null) {
            redoStack.push(currentState);
        }
        
        String previousState = undoStack.pop();
        applyState(previousState);
        state.showNotification(Component.translatable("gui.mgmc.notification.undo").getString());
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        
        String currentState = BlueprintIO.serialize(nodes, connections);
        if (currentState != null) {
            undoStack.push(currentState);
        }
        
        String nextState = redoStack.pop();
        applyState(nextState);
        state.showNotification(Component.translatable("gui.mgmc.notification.redo").getString());
    }

    private void applyState(String json) {
        state.selectedNodes.clear();
        BlueprintIO.loadFromString(json, nodes, connections, true);
        state.markDirty();
    }
}
