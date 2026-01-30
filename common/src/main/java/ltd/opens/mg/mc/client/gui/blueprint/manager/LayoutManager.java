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

        // 记录动画开始前的状态
        for (GuiNode node : nodes) {
            node.targetX = node.x;
            node.targetY = node.y;
            node.isAnimatingPos = true;
        }
        state.isAnimatingLayout = true;

        // 1. 分层 (Sugiyama 算法第一步：拓扑排序分配层级)
        Map<GuiNode, Integer> nodeToLayer = new HashMap<>();
        Set<GuiNode> remaining = new HashSet<>(nodes);
        
        int maxLayer = 0;
        int safetyIter = 0;
        
        // 找出初始层级：没有输入连接的节点（或者输入都在循环中的节点）
        for (GuiNode node : nodes) {
            boolean hasInputs = false;
            for (GuiConnection conn : connections) {
                if (conn.to == node && conn.from != node) { // 忽略自连接
                    hasInputs = true;
                    break;
                }
            }
            if (!hasInputs) {
                nodeToLayer.put(node, 0);
                remaining.remove(node);
            }
        }

        // 迭代分配层级
        while (!remaining.isEmpty() && safetyIter++ < 1000) {
            boolean progress = false;
            List<GuiNode> nextToAssign = new ArrayList<>();
            
            for (GuiNode node : remaining) {
                int maxParentLayer = -1;
                boolean allParentsAssigned = true;
                
                for (GuiConnection conn : connections) {
                    if (conn.to == node && conn.from != node) {
                        if (nodeToLayer.containsKey(conn.from)) {
                            maxParentLayer = Math.max(maxParentLayer, nodeToLayer.get(conn.from));
                        } else {
                            allParentsAssigned = false;
                            break;
                        }
                    }
                }
                
                if (allParentsAssigned && maxParentLayer != -1) {
                    nextToAssign.add(node);
                    progress = true;
                }
            }
            
            for (GuiNode node : nextToAssign) {
                int layer = 0;
                for (GuiConnection conn : connections) {
                    if (conn.to == node && nodeToLayer.containsKey(conn.from)) {
                        layer = Math.max(layer, nodeToLayer.get(conn.from) + 1);
                    }
                }
                nodeToLayer.put(node, layer);
                maxLayer = Math.max(maxLayer, layer);
                remaining.remove(node);
            }
            
            if (!progress && !remaining.isEmpty()) {
                // 存在循环或孤立岛屿，强制分配一个
                GuiNode force = remaining.iterator().next();
                int layer = 0;
                // 尽量找一个已经分配了父节点的
                for (GuiConnection conn : connections) {
                    if (conn.to == force && nodeToLayer.containsKey(conn.from)) {
                        layer = Math.max(layer, nodeToLayer.get(conn.from) + 1);
                    }
                }
                nodeToLayer.put(force, layer);
                maxLayer = Math.max(maxLayer, layer);
                remaining.remove(force);
            }
        }

        // 2. 将节点按层级归类
        Map<Integer, List<GuiNode>> layerMap = new TreeMap<>();
        for (GuiNode node : nodes) {
            int layer = nodeToLayer.getOrDefault(node, 0);
            layerMap.computeIfAbsent(layer, k -> new ArrayList<>()).add(node);
        }

        // 3. 层内排序 (重心启发式：减少连线交叉)
        for (int l = 1; l <= maxLayer; l++) {
            List<GuiNode> currentLayer = layerMap.get(l);
            if (currentLayer == null) continue;
            
            currentLayer.sort((a, b) -> {
                float avgYa = getAverageParentY(a, connections);
                float avgYb = getAverageParentY(b, connections);
                if (avgYa == avgYb) {
                    // 如果重心相同，按标题排序保持稳定性
                    return a.title.compareTo(b.title);
                }
                return Float.compare(avgYa, avgYb);
            });
        }

        // 4. 坐标计算 (动态间距)
        float horizontalPadding = 80;  // 列与列之间的间距
        float verticalPadding = 25;    // 行与行之间的最小间距
        
        // 计算每列的最大宽度
        Map<Integer, Float> columnWidths = new HashMap<>();
        for (int l = 0; l <= maxLayer; l++) {
            List<GuiNode> layerNodes = layerMap.get(l);
            if (layerNodes == null) continue;
            float maxWidth = 0;
            for (GuiNode node : layerNodes) {
                maxWidth = Math.max(maxWidth, node.width);
            }
            columnWidths.put(l, maxWidth);
        }

        // 计算每一层的总高度并分配位置
        float currentX = 0;
        for (int l = 0; l <= maxLayer; l++) {
            List<GuiNode> layerNodes = layerMap.get(l);
            if (layerNodes == null) continue;
            
            float colWidth = columnWidths.get(l);
            float totalLayerHeight = 0;
            for (GuiNode node : layerNodes) {
                totalLayerHeight += node.height + verticalPadding;
            }
            totalLayerHeight -= verticalPadding; // 减去最后一个多余的间距

            float currentY = -totalLayerHeight / 2f; // 垂直居中
            
            for (GuiNode node : layerNodes) {
                node.targetX = currentX;
                node.targetY = currentY;
                currentY += node.height + verticalPadding;
            }
            
            currentX += colWidth + horizontalPadding;
        }

        state.markDirty();
        state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.layout_complete").getString());
    }

    public void tick() {
        if (!state.isAnimatingLayout) return;

        boolean anyAnimating = false;
        float smoothing = 0.2f;

        for (GuiNode node : state.nodes) {
            if (node.isAnimatingPos) {
                float dx = node.targetX - node.x;
                float dy = node.targetY - node.y;

                if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
                    node.x = node.targetX;
                    node.y = node.targetY;
                    node.isAnimatingPos = false;
                } else {
                    node.x += dx * smoothing;
                    node.y += dy * smoothing;
                    anyAnimating = true;
                }
            }
        }

        if (!anyAnimating) {
            state.isAnimatingLayout = false;
        }
    }

    private float getAverageParentY(GuiNode node, List<GuiConnection> connections) {
        float sumY = 0;
        int count = 0;
        for (GuiConnection conn : connections) {
            if (conn.to == node) {
                // 使用父节点当前的 Y 坐标（如果是从左往右整理，父节点 Y 已经确定）
                sumY += conn.from.y;
                count++;
            }
        }
        return count == 0 ? 0 : sumY / count;
    }
}
