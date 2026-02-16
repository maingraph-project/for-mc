package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HistoryManager {
    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 50;
    
    private final BlueprintState state;

    public HistoryManager(BlueprintState state) {
        this.state = state;
    }

    public void pushHistory() {
        String currentState = BlueprintIO.serialize(state.nodes, state.connections, state.regions);
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
        
        String currentState = BlueprintIO.serialize(state.nodes, state.connections, state.regions);
        
        // Skip identical states at the top of the stack
        while (!undoStack.isEmpty() && undoStack.peek().equals(currentState)) {
            undoStack.pop();
        }

        if (undoStack.isEmpty()) return;

        if (currentState != null) {
            redoStack.push(currentState);
        }
        
        String previousState = undoStack.pop();
        applyState(previousState);
        state.showNotification(Component.translatable("gui.mgmc.notification.undo").getString());
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        
        String currentState = BlueprintIO.serialize(state.nodes, state.connections, state.regions);
        
        // Skip identical states at the top of the stack
        while (!redoStack.isEmpty() && redoStack.peek().equals(currentState)) {
            redoStack.pop();
        }

        if (redoStack.isEmpty()) return;

        if (currentState != null) {
            undoStack.push(currentState);
        }
        
        String nextState = redoStack.pop();
        applyState(nextState);
        state.showNotification(Component.translatable("gui.mgmc.notification.redo").getString());
    }

    private void applyState(String json) {
        state.selectedNodes.clear();
        state.draggingNode = null;
        state.connectionStartNode = null;
        state.contextMenuNode = null;
        state.focusedNode = null;
        state.editingMarkerNode = null;
        state.highlightedNode = null;
        
        state.isBoxSelecting = false;
        state.isPanning = false;
        state.showNodeMenu = false;
        state.contextMenu.hide();
        state.showQuickSearch = false;

        List<String> historyIds = new ArrayList<>();
        for (GuiNode node : state.searchHistory) {
            historyIds.add(node.id);
        }
        
        BlueprintIO.loadFromString(json, state.nodes, state.connections, state.regions, true);

        state.searchHistory.clear();
        for (String id : historyIds) {
            for (GuiNode node : state.nodes) {
                if (node.id.equals(id)) {
                    state.searchHistory.add(node);
                    break;
                }
            }
        }
        
        state.markDirty();
    }
}
