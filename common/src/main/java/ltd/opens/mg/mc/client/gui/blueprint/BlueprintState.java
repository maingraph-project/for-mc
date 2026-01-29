package ltd.opens.mg.mc.client.gui.blueprint;

import ltd.opens.mg.mc.client.gui.blueprint.manager.*;
import ltd.opens.mg.mc.client.gui.blueprint.menu.*;
import ltd.opens.mg.mc.client.gui.components.*;
import ltd.opens.mg.mc.client.gui.components.GuiContextMenu;
import ltd.opens.mg.mc.client.gui.blueprint.manager.MarkerSearchManager;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.Util;
import ltd.opens.mg.mc.client.gui.screens.BlueprintScreen;
import java.util.ArrayList;
import java.util.List;

public class BlueprintState {
    public final Viewport viewport = new Viewport();
    
    // UI State
    public GuiNode focusedNode;
    public boolean isAnimatingView = false;
    public boolean isAnimatingLayout = false;

    public final List<GuiNode> nodes = new ArrayList<>();
    public final List<GuiConnection> connections = new ArrayList<>();

    public final HistoryManager historyManager = new HistoryManager(this);
    public final LayoutManager layoutManager = new LayoutManager(this);
    public final ViewManager viewManager = new ViewManager(this);
    
    // Selection state
    public final List<GuiNode> selectedNodes = new ArrayList<>();
    public boolean isBoxSelecting = false;
    public double boxSelectStartX, boxSelectStartY;
    public double boxSelectEndX, boxSelectEndY;

    // Clipboard for copy/paste
    public static String clipboardJson = null;

    public GuiNode draggingNode = null;
    public float dragOffsetX, dragOffsetY;
    public float startNodeX, startNodeY;

    public GuiNode connectionStartNode = null;
    public String connectionStartPort = null;
    public boolean isConnectionFromInput = false;

    public boolean isPanning = false;
    public double lastMouseX, lastMouseY;
    public double startMouseX, startMouseY; // 用于判断拖拽位移
    
    public boolean showNodeMenu = false;
    public double menuX, menuY;
    public final GuiContextMenu contextMenu = new GuiContextMenu();
    public GuiNode contextMenuNode = null;
    public BlueprintMenu menu = new BlueprintMenu();
    
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

    // Highlighting
    public GuiNode highlightedNode = null;
    public int highlightTimer = 0;

    // Quick Search & History
    public List<GuiNode> searchHistory = new ArrayList<>();
    public float searchConfirmProgress = 0f;
    public float buttonLongPressProgress = 0f;
    public String buttonLongPressTarget = null;
    public int lastHistorySelectedIndex = -1;
    public boolean isEnterDown = false;
    public boolean isMouseDown = false;
    public boolean isWDown = false;
    public boolean wTriggered = false; // Add this to prevent repeated triggers
    public float wPressProgress = 0f;
    public String wPressUrl = null;

    // Drag-to-create-node context
    public GuiNode pendingConnectionSourceNode = null;
    public String pendingConnectionSourcePort = null;
    public boolean pendingConnectionFromInput = false;
    public NodeDefinition.PortType pendingConnectionSourceType = null;

    public void showNotification(String message) {
        this.notificationMessage = message;
        this.notificationTimer = 60; // 3 seconds (at 20 ticks per second)
    }

    public void highlightNode(String nodeId) {
        for (GuiNode node : nodes) {
            if (node.id.equals(nodeId)) {
                this.highlightedNode = node;
                this.highlightTimer = 100; // 5 seconds
                this.viewManager.centerOnNode(node);
                break;
            }
        }
    }

    public void addToHistory(GuiNode node) {
        searchHistory.remove(node);
        searchHistory.add(0, node);
        if (searchHistory.size() > 5) {
            searchHistory.remove(searchHistory.size() - 1);
        }
    }

    public void tick(int screenWidth, int screenHeight, int mouseX, int mouseY, boolean isMouseDown) {
        cursorTick++;
        if (highlightTimer > 0) highlightTimer--;
        if (notificationTimer > 0) notificationTimer--;
        viewManager.tick();
        layoutManager.tick();

        // W Long Press Logic
        if (isWDown && selectedNodes.size() == 1 && !showQuickSearch && editingMarkerNode == null) {
            if (wTriggered) {
                // If already triggered, keep progress at 0 or 1, and don't do anything
                wPressProgress = 0f;
            } else {
                // Find first selected node with a web_url
                String url = null;
                for (GuiNode node : selectedNodes) {
                    if (node.definition != null && node.definition.properties().containsKey("web_url")) {
                        url = (String) node.definition.properties().get("web_url");
                        break;
                    }
                }

                if (url != null) {
                    wPressUrl = url;
                    wPressProgress += 0.04f; // 25 ticks = 1.25s
                    if (wPressProgress >= 1.0f) {
                        System.out.println("MGMC: Long press W completed for URL: " + url);
                        wPressProgress = 0f;
                        wTriggered = true; // Mark as triggered
                        openWebpage(url);
                    }
                } else {
                    if (wPressProgress > 0) {
                        System.out.println("MGMC: W down but no web_url found in selected nodes");
                    }
                    wPressProgress *= 0.8f;
                    if (wPressProgress < 0.01f) {
                        wPressProgress = 0f;
                        wPressUrl = null;
                    }
                }
            }
        } else {
            if (!isWDown) {
                wTriggered = false; // Reset when key is released
            }
            wPressProgress *= 0.8f;
            if (wPressProgress < 0.01f) {
                wPressProgress = 0f;
                wPressUrl = null;
            }
        }

        // Confirm progress logic (Long press Enter or Mouse for history)
        if (showQuickSearch && quickSearchEditBox != null && quickSearchEditBox.getValue().isEmpty()) {
            if ((isEnterDown || isMouseDown) && quickSearchSelectedIndex >= 0 && quickSearchSelectedIndex < searchHistory.size()) {
                searchConfirmProgress += 0.05f; // Fill in 20 ticks (1 second)
                if (searchConfirmProgress >= 1.0f) {
                    searchConfirmProgress = 0f;
                    isEnterDown = false;
                    this.isMouseDown = false;
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

        // Button long press logic
        if (buttonLongPressTarget != null) {
            if (isMouseDown) {
                buttonLongPressProgress += 0.05f; // 20 ticks = 1s
                if (buttonLongPressProgress >= 1.0f) {
                    if ("reset_view".equals(buttonLongPressTarget)) {
                        resetView();
                    } else if ("auto_layout".equals(buttonLongPressTarget)) {
                        autoLayout();
                    }
                    buttonLongPressProgress = 0f;
                    buttonLongPressTarget = null;
                }
            } else {
                buttonLongPressProgress = 0f;
                buttonLongPressTarget = null;
            }
        } else {
            buttonLongPressProgress = 0f;
        }
    }

    public void jumpToNode(GuiNode node, int screenWidth, int screenHeight) {
        viewManager.jumpToNode(node, screenWidth, screenHeight);
    }

    private void openWebpage(String url) {
        if (url == null || url.isEmpty()) return;
        
        System.out.println("MGMC: Attempting to open webpage: " + url);
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§a[MGMC] §7 Try open URL: §n" + url), false);
        }

        // 先尝试最直接的方式
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                return;
            }
        } catch (Exception e) {
            System.err.println("MGMC: Desktop browse failed, falling back to Util: " + e.getMessage());
        }

        // 保底方案
        Util.getPlatform().openUri(url);
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
    public String historyPendingState = null;

    public void pushHistory() {
        historyManager.pushHistory();
    }

    public void pushHistory(String stateJson) {
        historyManager.pushHistory(stateJson);
    }

    public void undo() {
        historyManager.undo();
    }

    public void redo() {
        historyManager.redo();
    }

    public void autoLayout() {
        layoutManager.autoLayout();
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void resetView() {
        viewManager.resetView();
    }
}
