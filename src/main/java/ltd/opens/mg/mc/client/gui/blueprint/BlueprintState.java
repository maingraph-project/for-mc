package ltd.opens.mg.mc.client.gui.blueprint;

import ltd.opens.mg.mc.client.gui.blueprint.menu.*;
import ltd.opens.mg.mc.client.gui.components.*;
import java.util.ArrayList;
import java.util.List;

public class BlueprintState {
    public float panX = 0;
    public float panY = 0;
    public float zoom = 1.0f;

    public final List<GuiNode> nodes = new ArrayList<>();
    public final List<GuiConnection> connections = new ArrayList<>();
    
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
    public long version = 0;

    public void showNotification(String message) {
        this.notificationMessage = message;
        this.notificationTimer = 60; // 3 seconds at 20fps, but Minecraft render is faster, let's use ticks or simple counter
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void autoLayout() {
        if (nodes.isEmpty()) return;

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
