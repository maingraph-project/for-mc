package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.GuiConnection;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import net.minecraft.network.chat.Component;

import java.util.*;

public class LayoutManager {
    private final BlueprintState state;

    public LayoutManager(BlueprintState state) {
        this.state = state;
    }

    public void autoLayout() {
        List<GuiNode> nodes = state.nodes;
        List<GuiConnection> connections = state.connections;
        
        if (nodes.isEmpty()) return;
        state.historyManager.pushHistory();

        // 1. 分类节点与预处理
        Map<GuiNode, Integer> layers = new HashMap<>();
        Set<GuiNode> remaining = new HashSet<>(nodes);
        
        // 初始层级：没有输入连接的节点
        List<GuiNode> currentLayerNodes = new ArrayList<>();
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

        // 迭代分配层级
        int maxLayer = 0;
        int safetyIter = 0;
        while (!remaining.isEmpty() && safetyIter++ < 1000) {
            List<GuiNode> nextLayer = new ArrayList<>();
            for (Iterator<GuiNode> it = remaining.iterator(); it.hasNext(); ) {
                GuiNode node = it.next();
                int maxParentLayer = -1;
                boolean allParentsAssigned = true;
                
                for (GuiConnection conn : connections) {
                    if (conn.to == node) {
                        if (layers.containsKey(conn.from)) {
                            maxParentLayer = Math.max(maxParentLayer, layers.get(conn.from));
                        } else {
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
            if (nextLayer.isEmpty() && !remaining.isEmpty()) {
                GuiNode force = remaining.iterator().next();
                layers.put(force, maxLayer + 1);
                nextLayer.add(force);
                remaining.remove(force);
                maxLayer++;
            }
        }

        // 3. 坐标计算
        float colW = 250;
        float rowH = 150;
        
        Map<Integer, List<GuiNode>> layerMap = new HashMap<>();
        for (GuiNode node : nodes) {
            int layer = layers.getOrDefault(node, 0);
            layerMap.computeIfAbsent(layer, k -> new ArrayList<>()).add(node);
        }

        for (int l = 1; l <= maxLayer; l++) {
            List<GuiNode> current = layerMap.get(l);
            if (current == null) continue;
            
            current.sort((a, b) -> {
                float avgYa = getAverageParentY(a, layers, connections);
                float avgYb = getAverageParentY(b, layers, connections);
                return Float.compare(avgYa, avgYb);
            });
        }

        for (int l = 0; l <= maxLayer; l++) {
            List<GuiNode> layerNodes = layerMap.get(l);
            if (layerNodes == null) continue;
            
            float totalHeight = (layerNodes.size() - 1) * rowH;
            for (int i = 0; i < layerNodes.size(); i++) {
                GuiNode node = layerNodes.get(i);
                node.x = l * colW;
                node.y = (i * rowH) - (totalHeight / 2f);
            }
        }

        state.markDirty();
        state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.layout_complete").getString());
    }

    private float getAverageParentY(GuiNode node, Map<GuiNode, Integer> layers, List<GuiConnection> connections) {
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
}
