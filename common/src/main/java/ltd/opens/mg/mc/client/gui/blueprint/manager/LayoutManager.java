package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.GuiConnection;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import ltd.opens.mg.mc.client.gui.components.GuiRegion;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.network.chat.Component;

import java.util.*;

public class LayoutManager {
    private final BlueprintState state;

    public LayoutManager(BlueprintState state) {
        this.state = state;
    }

    public void autoLayout() {
        if (state.nodes.isEmpty()) return;
        state.historyManager.pushHistory();

        // 记录动画开始前的状态
        for (GuiNode node : state.nodes) {
            node.targetX = node.x;
            node.targetY = node.y;
            node.isAnimatingPos = true;
        }
        state.isAnimatingLayout = true;

        // Prepare for layout
        List<GuiNode> layoutNodes = new ArrayList<>();
        List<GuiConnection> layoutConnections = new ArrayList<>();

        // Map regions to proxy nodes
        Map<GuiRegion, GuiNode> regionProxies = new HashMap<>();
        Map<GuiNode, GuiRegion> nodeToRegion = new HashMap<>();
        Map<GuiNode, GuiNode> nodeToProxy = new HashMap<>();

        // 1. Identify nodes in regions
        if (!state.regions.isEmpty()) {
            for (GuiRegion region : state.regions) {
                NodeDefinition dummyDef = new NodeDefinition.Builder("region_proxy_" + region.hashCode(), "Region Group").build();
                GuiNode proxy = new GuiNode(dummyDef, region.x, region.y);
                proxy.width = region.width;
                proxy.height = region.height;
                // Preserve original position for delta calculation later
                proxy.targetX = region.x;
                proxy.targetY = region.y;
                
                regionProxies.put(region, proxy);
                layoutNodes.add(proxy);
            }

            for (GuiNode node : state.nodes) {
                GuiRegion container = null;
                float cx = node.x + node.width / 2;
                float cy = node.y + node.height / 2;
                
                // Find containing region (top-most visual order, which is last in list usually, but here any matches)
                // Reverse order to match click detection priority
                for (int i = state.regions.size() - 1; i >= 0; i--) {
                    GuiRegion r = state.regions.get(i);
                    if (cx >= r.x && cx <= r.x + r.width && cy >= r.y && cy <= r.y + r.height) {
                        container = r;
                        break;
                    }
                }

                if (container != null) {
                    nodeToRegion.put(node, container);
                    nodeToProxy.put(node, regionProxies.get(container));
                } else {
                    layoutNodes.add(node); // Independent node
                }
            }

            // 2. Build connections for layout
            for (GuiConnection conn : state.connections) {
                GuiNode from = conn.from;
                GuiNode to = conn.to;
                
                GuiNode sourceLayoutNode = nodeToProxy.getOrDefault(from, from);
                GuiNode targetLayoutNode = nodeToProxy.getOrDefault(to, to);

                if (sourceLayoutNode != targetLayoutNode) {
                    // Avoid duplicate connections? 
                    // Sugiyama can handle multiple edges, but might be better to distinct them?
                    // For now, let's just add them.
                    layoutConnections.add(new GuiConnection(sourceLayoutNode, "out", targetLayoutNode, "in"));
                }
            }
        } else {
            layoutNodes.addAll(state.nodes);
            layoutConnections.addAll(state.connections);
        }

        // Execute Layout Algorithm
        performSugiyamaLayout(layoutNodes, layoutConnections);

        // Apply results back to regions and contained nodes
        if (!state.regions.isEmpty()) {
            for (Map.Entry<GuiRegion, GuiNode> entry : regionProxies.entrySet()) {
                GuiRegion region = entry.getKey();
                GuiNode proxy = entry.getValue();

                float dx = proxy.targetX - region.x;
                float dy = proxy.targetY - region.y;

                if (dx != 0 || dy != 0) {
                    region.x = proxy.targetX;
                    region.y = proxy.targetY;
                    
                    // Move contained nodes
                    for (Map.Entry<GuiNode, GuiRegion> mapping : nodeToRegion.entrySet()) {
                        if (mapping.getValue() == region) {
                            GuiNode node = mapping.getKey();
                            node.targetX += dx;
                            node.targetY += dy;
                        }
                    }
                }
            }
        }

        state.markDirty();
        state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.layout_complete").getString());
    }

    public void autoLayoutRegion(GuiRegion region) {
        if (state.nodes.isEmpty()) return;

        List<GuiNode> nodesInRegion = new ArrayList<>();
        // Identify nodes in region
        for (GuiNode node : state.nodes) {
            float cx = node.x + node.width / 2;
            float cy = node.y + node.height / 2;
            if (cx >= region.x && cx <= region.x + region.width && cy >= region.y && cy <= region.y + region.height) {
                nodesInRegion.add(node);
            }
        }

        if (nodesInRegion.isEmpty()) return;

        state.historyManager.pushHistory();

        // Record pre-animation state
        for (GuiNode node : nodesInRegion) {
            node.targetX = node.x;
            node.targetY = node.y;
            node.isAnimatingPos = true;
        }
        state.isAnimatingLayout = true;

        // Find internal connections
        List<GuiConnection> internalConnections = new ArrayList<>();
        for (GuiConnection conn : state.connections) {
            if (nodesInRegion.contains(conn.from) && nodesInRegion.contains(conn.to)) {
                internalConnections.add(conn);
            }
        }

        // Run Sugiyama on these nodes
        performSugiyamaLayout(nodesInRegion, internalConnections);

        // performSugiyamaLayout sets targetX/targetY centered around (0,0) roughly
        // We need to move them to the region

        // Calculate bounding box of the new layout
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (GuiNode node : nodesInRegion) {
            minX = Math.min(minX, node.targetX);
            minY = Math.min(minY, node.targetY);
            maxX = Math.max(maxX, node.targetX + node.width);
            maxY = Math.max(maxY, node.targetY + node.height);
        }

        float layoutWidth = maxX - minX;
        float layoutHeight = maxY - minY;

        // Resize region to fit comfortably if it's too small
        float padding = 30;
        float headerHeight = 30;

        float requiredWidth = layoutWidth + padding * 2;
        float requiredHeight = layoutHeight + padding * 2 + headerHeight;

        if (region.width < requiredWidth) region.width = requiredWidth;
        if (region.height < requiredHeight) region.height = requiredHeight;

        // Desired position of minX, minY relative to region
        // Center the layout in the region if region is larger
        float availableWidth = region.width - padding * 2;
        float availableHeight = region.height - headerHeight - padding * 2;
        
        float startX = region.x + padding + (availableWidth - layoutWidth) / 2;
        float startY = region.y + headerHeight + padding + (availableHeight - layoutHeight) / 2;

        float offsetX = startX - minX;
        float offsetY = startY - minY;

        for (GuiNode node : nodesInRegion) {
            node.targetX += offsetX;
            node.targetY += offsetY;
        }

        state.markDirty();
    }

    private void performSugiyamaLayout(List<GuiNode> nodes, List<GuiConnection> connections) {
        if (nodes.isEmpty()) return;

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
