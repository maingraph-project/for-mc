package ltd.opens.mg.mc.client.gui.blueprint;

import ltd.opens.mg.mc.client.gui.blueprint.menu.*;
import ltd.opens.mg.mc.client.gui.components.*;
import ltd.opens.mg.mc.client.gui.blueprint.manager.MarkerSearchManager;
import net.minecraft.client.gui.components.EditBox;
import java.util.ArrayList;
import java.util.List;

public class BlueprintState {
    public float panX = 0;
    public float panY = 0;
    public float zoom = 1.0f;

    public final List<GuiNode> nodes = new ArrayList<>();
    public final List<GuiConnection> connections = new ArrayList<>();
    
    // Selection state
    public final List<GuiNode> selectedNodes = new ArrayList<>();
    public boolean isBoxSelecting = false;
    public double boxSelectStartX, boxSelectStartY;
    public double boxSelectEndX, boxSelectEndY;

    // Clipboard for copy/paste
    public static String clipboardJson = null;

    public GuiNode draggingNode = null;
    public float dragOffsetX, dragOffsetY;

    public GuiNode connectionStartNode = null;
    public String connectionStartPort = null;
    public boolean isConnectionFromInput = false;

    public boolean isPanning = false;
    public double lastMouseX, lastMouseY;
    public double startMouseX, startMouseY; // 用于判断拖拽位移
    
    public boolean showNodeMenu = false;
    public double menuX, menuY;
    public boolean showNodeContextMenu = false;
    public GuiNode contextMenuNode = null;
    public final BlueprintMenu menu = new BlueprintMenu();

    public GuiNode focusedNode = null;
    public String focusedPort = null;
    public int cursorTick = 0;
    public boolean isDirty = false;
    public String notificationMessage = null;
    public int notificationTimer = 0;
    public boolean readOnly = false;
    public boolean showMinimap = true;
    public boolean showQuickSearch = false;
    public EditBox quickSearchEditBox = null;
    public final List<GuiNode> quickSearchMatches = new ArrayList<>();
    public int quickSearchSelectedIndex = -1;
    public int quickSearchScrollOffset = 0;
    public static final int MAX_QUICK_SEARCH_VISIBLE = 8;
    public long version = 0;

    // Marker Editing
    public GuiNode editingMarkerNode = null;
    public EditBox markerEditBox = null;

    // View Animation
    public boolean isAnimatingView = false;
    public float targetPanX = 0;
    public float targetPanY = 0;
    public float targetZoom = 1.0f;
    private static final float PAN_SMOOTHING = 0.2f;
    private static final float ZOOM_SMOOTHING = 0.15f;

    public GuiNode highlightedNode = null;
    public int highlightTimer = 0;

    // Search History
    public List<GuiNode> searchHistory = new ArrayList<>();
    public float searchConfirmProgress = 0f; // 0 to 1
    public int lastHistorySelectedIndex = -1;
    public boolean isEnterDown = false;
    public boolean isMouseDown = false;

    public void addToHistory(GuiNode node) {
        searchHistory.remove(node);
        searchHistory.add(0, node);
        if (searchHistory.size() > 5) {
            searchHistory.remove(searchHistory.size() - 1);
        }
    }

    public void tick(int screenWidth, int screenHeight) {
        cursorTick++;
        if (highlightTimer > 0) highlightTimer--;
        
        // Confirm progress logic (Long press Enter or Mouse for history)
        if (showQuickSearch && quickSearchEditBox != null && quickSearchEditBox.getValue().isEmpty()) {
            if ((isEnterDown || isMouseDown) && quickSearchSelectedIndex >= 0 && quickSearchSelectedIndex < searchHistory.size()) {
                searchConfirmProgress += 0.05f; // Fill in 20 ticks (1 second)
                if (searchConfirmProgress >= 1.0f) {
                    searchConfirmProgress = 0f;
                    isEnterDown = false;
                    isMouseDown = false;
                    jumpToNode(searchHistory.get(quickSearchSelectedIndex), screenWidth, screenHeight);
                    showQuickSearch = false;
                }
            } else {
                searchConfirmProgress *= 0.8f; // Fast decay when not holding
                if (searchConfirmProgress < 0.01f) searchConfirmProgress = 0f;
            }
        } else {
            searchConfirmProgress = 0f;
        }

        if (isAnimatingView) {
            float dx = targetPanX - panX;
            float dy = targetPanY - panY;
            float dz = targetZoom - zoom;
            
            if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f && Math.abs(dz) < 0.005f) {
                panX = targetPanX;
                panY = targetPanY;
                zoom = targetZoom;
                isAnimatingView = false;
            } else {
                panX += dx * PAN_SMOOTHING;
                panY += dy * PAN_SMOOTHING;
                zoom += dz * ZOOM_SMOOTHING;
            }
        }
    }

    public void jumpToNode(GuiNode node, int screenWidth, int screenHeight) {
        targetZoom = 1.0f; // Focus zoom level
        targetPanX = screenWidth / 2f - (node.x + node.width / 2f) * targetZoom;
        targetPanY = screenHeight / 2f - (node.y + node.height / 2f) * targetZoom;
        isAnimatingView = true;
        highlightedNode = node;
        highlightTimer = 40; // 2 seconds at 20 ticks
        addToHistory(node);
    }

    public void updateQuickSearchMatches() {
        quickSearchMatches.clear();
        if (quickSearchEditBox == null) return;
        String query = quickSearchEditBox.getValue();
        if (query.isEmpty()) {
            quickSearchSelectedIndex = searchHistory.isEmpty() ? -1 : 0;
            quickSearchScrollOffset = 0;
            searchConfirmProgress = 0f;
            return;
        }

        quickSearchMatches.addAll(MarkerSearchManager.performSearch(nodes, query));
        
        quickSearchScrollOffset = 0;
        if (!quickSearchMatches.isEmpty()) {
            quickSearchSelectedIndex = 0;
        } else {
            quickSearchSelectedIndex = -1;
        }
    }

    // Undo/Redo history
    private final java.util.Deque<String> undoStack = new java.util.ArrayDeque<>();
    private final java.util.Deque<String> redoStack = new java.util.ArrayDeque<>();
    private static final int MAX_HISTORY = 50;
    public String historyPendingState = null;

    public void pushHistory() {
        String currentState = ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO.serialize(nodes, connections);
        pushHistory(currentState);
    }

    public void pushHistory(String stateJson) {
        if (stateJson != null) {
            // Only push if different from last (optional optimization)
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
        
        String currentState = ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO.serialize(nodes, connections);
        if (currentState != null) {
            redoStack.push(currentState);
        }
        
        String previousState = undoStack.pop();
        applyState(previousState);
        showNotification(net.minecraft.network.chat.Component.translatable("gui.mgmc.notification.undo").getString());
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        
        String currentState = ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO.serialize(nodes, connections);
        if (currentState != null) {
            undoStack.push(currentState);
        }
        
        String nextState = redoStack.pop();
        applyState(nextState);
        showNotification(net.minecraft.network.chat.Component.translatable("gui.mgmc.notification.redo").getString());
    }

    private void applyState(String json) {
        selectedNodes.clear();
        ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO.loadFromString(json, nodes, connections, true);
        markDirty();
    }

    public void showNotification(String message) {
        this.notificationMessage = message;
        this.notificationTimer = 60; // 3 seconds at 20fps, but Minecraft render is faster, let's use ticks or simple counter
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void autoLayout() {
        if (nodes.isEmpty()) return;
        pushHistory();

        // 1. 分类节点与预处理
        java.util.List<GuiNode> execNodes = new java.util.ArrayList<>();
        java.util.List<GuiNode> dataNodes = new java.util.ArrayList<>();
        for (GuiNode node : nodes) {
            boolean isExec = false;
            for (GuiNode.NodePort p : node.inputs) if (p.type == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC) isExec = true;
            for (GuiNode.NodePort p : node.outputs) if (p.type == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC) isExec = true;
            if (isExec) execNodes.add(node);
            else dataNodes.add(node);
        }

        // 2. 层级分配 (Rank Assignment)
        java.util.Map<GuiNode, Integer> layers = new java.util.HashMap<>();
        java.util.Set<GuiNode> remaining = new java.util.HashSet<>(nodes);
        
        // 初始层级：没有输入连接的节点
        java.util.List<GuiNode> currentLayerNodes = new java.util.ArrayList<>();
        for (GuiNode node : nodes) {
            boolean hasInputs = false;
            for (GuiConnection conn : connections) {
                if (conn.to == node) {
                    hasInputs = true;
                    break;
                }
            }
            if (!hasInputs) {
                layers.put(node, 0);
                currentLayerNodes.add(node);
                remaining.remove(node);
            }
        }

        // 迭代分配层级 (简单拓扑排序变体)
        int maxLayer = 0;
        int safetyIter = 0;
        while (!remaining.isEmpty() && safetyIter++ < 1000) {
            java.util.List<GuiNode> nextLayer = new java.util.ArrayList<>();
            for (java.util.Iterator<GuiNode> it = remaining.iterator(); it.hasNext(); ) {
                GuiNode node = it.next();
                int maxParentLayer = -1;
                boolean allParentsAssigned = true;
                
                for (GuiConnection conn : connections) {
                    if (conn.to == node) {
                        if (layers.containsKey(conn.from)) {
                            maxParentLayer = Math.max(maxParentLayer, layers.get(conn.from));
                        } else {
                            // 检查是否是循环引用，如果是，先不管它
                            // 这里简单处理：如果父节点还没分配层级，暂不处理该节点
                            allParentsAssigned = false;
                            break;
                        }
                    }
                }
                
                if (allParentsAssigned && maxParentLayer != -1) {
                    layers.put(node, maxParentLayer + 1);
                    nextLayer.add(node);
                    it.remove();
                    maxLayer = Math.max(maxLayer, maxParentLayer + 1);
                }
            }
            // 处理循环引用：如果这一轮没有新节点加入，强行取一个
            if (nextLayer.isEmpty() && !remaining.isEmpty()) {
                GuiNode force = remaining.iterator().next();
                layers.put(force, maxLayer + 1);
                nextLayer.add(force);
                remaining.remove(force);
                maxLayer++;
            }
        }

        // 3. 坐标计算 (Sugiyama Step 3 & 4)
        float colW = 250;
        float rowH = 150;
        
        java.util.Map<Integer, java.util.List<GuiNode>> layerMap = new java.util.HashMap<>();
        for (GuiNode node : nodes) {
            int layer = layers.getOrDefault(node, 0);
            layerMap.computeIfAbsent(layer, k -> new java.util.ArrayList<>()).add(node);
        }

        // 排序每一层以减少交叉 (Barycenter Heuristic)
        for (int l = 1; l <= maxLayer; l++) {
            java.util.List<GuiNode> current = layerMap.get(l);
            if (current == null) continue;
            
            current.sort((a, b) -> {
                float avgYa = getAverageParentY(a, layers);
                float avgYb = getAverageParentY(b, layers);
                return Float.compare(avgYa, avgYb);
            });
        }

        // 最终定位
        for (int l = 0; l <= maxLayer; l++) {
            java.util.List<GuiNode> layerNodes = layerMap.get(l);
            if (layerNodes == null) continue;
            
            float totalHeight = (layerNodes.size() - 1) * rowH;
            for (int i = 0; i < layerNodes.size(); i++) {
                GuiNode node = layerNodes.get(i);
                node.x = l * colW;
                node.y = (i * rowH) - (totalHeight / 2f); // 垂直居中对齐
            }
        }

        markDirty();
        showNotification(net.minecraft.network.chat.Component.translatable("gui.mgmc.blueprint_editor.layout_complete").getString());
    }

    private float getAverageParentY(GuiNode node, java.util.Map<GuiNode, Integer> layers) {
        float sumY = 0;
        int count = 0;
        for (GuiConnection conn : connections) {
            if (conn.to == node && layers.containsKey(conn.from)) {
                sumY += conn.from.y;
                count++;
            }
        }
        return count == 0 ? 0 : sumY / count;
    }

    public void resetView() {
        panX = 0;
        panY = 0;
        zoom = 1.0f;
    }
}
