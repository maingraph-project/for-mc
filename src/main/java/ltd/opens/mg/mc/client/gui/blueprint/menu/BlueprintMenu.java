package ltd.opens.mg.mc.client.gui.blueprint.menu;


import ltd.opens.mg.mc.client.gui.blueprint.manager.*;


import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class BlueprintMenu {
    private String searchQuery = "";
    private List<BlueprintSearchManager.SearchResult> filteredResults = new ArrayList<>();
    private int selectedIndex = -1;
    private double scrollAmount = 0;
    private double subScrollAmount = 0;
    private int menuWidth = 150;
    private int subMenuWidth = 150;
    private int lastMenuContentY = 0;
    private int lastMenuHeight = 0;
    private String hoveredCategory = null;
    private String currentPath = BlueprintCategoryManager.ROOT_PATH;

    // Getters and Setters for state
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public List<BlueprintSearchManager.SearchResult> getFilteredResults() { return filteredResults; }
    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int selectedIndex) { this.selectedIndex = selectedIndex; }
    public double getScrollAmount() { return scrollAmount; }
    public void setScrollAmount(double scrollAmount) { this.scrollAmount = scrollAmount; }
    public double getSubScrollAmount() { return subScrollAmount; }
    public void setSubScrollAmount(double subScrollAmount) { this.subScrollAmount = subScrollAmount; }
    public int getMenuWidth() { return menuWidth; }
    public int getSubMenuWidth() { return subMenuWidth; }
    public int getLastMenuContentY() { return lastMenuContentY; }
    public int getLastMenuHeight() { return lastMenuHeight; }
    public String getHoveredCategory() { return hoveredCategory; }
    public String getCurrentPath() { return currentPath; }
    public void setCurrentPath(String currentPath) { this.currentPath = currentPath; }

    public void reset() {
        searchQuery = "";
        filteredResults.clear();
        selectedIndex = -1;
        scrollAmount = 0;
        subScrollAmount = 0;
        hoveredCategory = null;
        currentPath = BlueprintCategoryManager.ROOT_PATH;
    }

    public void updateSearch() {
        filteredResults = BlueprintSearchManager.performSearch(searchQuery);
        if (!filteredResults.isEmpty()) {
            selectedIndex = 0;
        } else {
            selectedIndex = -1;
        }
    }

    public NodeDefinition getSelectedNode() {
        if (selectedIndex >= 0 && selectedIndex < filteredResults.size()) {
            BlueprintSearchManager.SearchResult res = filteredResults.get(selectedIndex);
            if (!res.isCategory()) {
                return res.node;
            }
        }
        return null;
    }

    public void renderNodeContextMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY) {
        BlueprintMenuRenderer.renderNodeContextMenu(guiGraphics, font, mouseX, mouseY, menuX, menuY);
    }

    public void renderNodeMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        
        // 1. Layout Calculations
        calculateLayout(font, screenWidth);
        
        int width = menuWidth;
        if (x + width > screenWidth) x -= width;
        
        // 2. Render Search Box
        int searchHeight = 25;
        BlueprintMenuRenderer.renderSearchBox(guiGraphics, font, x, y, width, searchHeight, searchQuery, Component.translatable("gui.mgmc.blueprint_editor.search_hint"));

        int contentY = y + searchHeight + 2;
        int maxVisibleItems = 12;
        int itemHeight = 18;
        int pathBarHeight = (searchQuery.isEmpty()) ? 12 : 0;
        
        if (!searchQuery.isEmpty()) {
            renderSearchResults(guiGraphics, font, x, contentY, width, screenHeight, mouseX, mouseY, maxVisibleItems, itemHeight);
        } else {
            renderCategoryMode(guiGraphics, font, x, contentY, width, screenHeight, mouseX, mouseY, maxVisibleItems, itemHeight, pathBarHeight, screenWidth);
        }
    }

    private void calculateLayout(Font font, int screenWidth) {
        if (!searchQuery.isEmpty()) {
            int maxW = 180;
            for (BlueprintSearchManager.SearchResult res : filteredResults) {
                int itemW = res.isCategory() ? 
                    font.width(Component.translatable(res.category).getString()) + 40 :
                    font.width(Component.translatable(res.node.name()).getString()) + 100; // Simplified estimate
                maxW = Math.max(maxW, itemW);
            }
            menuWidth = Math.min(maxW, screenWidth / 2);
        } else {
            BlueprintCategoryManager.CategoryData data = BlueprintCategoryManager.getCategoryData(currentPath);
            int maxW = 180;
            if (!currentPath.equals(BlueprintCategoryManager.ROOT_PATH)) {
                maxW = Math.max(maxW, font.width("<- " + Component.translatable("gui.mgmc.blueprint_selection.back").getString()) + 30);
            }
            for (String sub : data.subCategories) {
                maxW = Math.max(maxW, font.width(Component.translatable(sub).getString()) + 40);
            }
            for (NodeDefinition def : data.directNodes) {
                maxW = Math.max(maxW, font.width(Component.translatable(def.name()).getString()) + 30);
            }
            menuWidth = Math.min(maxW, screenWidth / 3);
        }
    }

    private void renderSearchResults(GuiGraphics guiGraphics, Font font, int x, int contentY, int width, int screenHeight, int mouseX, int mouseY, int maxVisibleItems, int itemHeight) {
        int displayCount = Math.min(filteredResults.size(), maxVisibleItems);
        int height = displayCount * itemHeight + 6;
        
        if (contentY + height > screenHeight) contentY -= (height + 27); // 27 is search box + margin
        lastMenuContentY = contentY;
        lastMenuHeight = height;

        BlueprintMenuRenderer.renderBackground(guiGraphics, x, contentY, width, height);

        if (filteredResults.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.no_nodes_found"), x + 8, contentY + 8, 0xFF888888, false);
        } else {
            guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
            int totalHeight = filteredResults.size() * itemHeight;
            scrollAmount = Mth.clamp(scrollAmount, 0, Math.max(0, totalHeight - (displayCount * itemHeight)));
            
            for (int i = 0; i < filteredResults.size(); i++) {
                BlueprintSearchManager.SearchResult res = filteredResults.get(i);
                int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight < contentY || itemY > contentY + height) continue;
                
                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                if (hovered || i == selectedIndex) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                }
                
                if (res.isCategory()) {
                    String catName = Component.translatable(res.category).getString();
                    BlueprintMenuRenderer.renderHighlightedString(guiGraphics, font, catName, x + 8, itemY + 4, 0xFF88FFFF, searchQuery);
                    guiGraphics.drawString(font, ">", x + width - 15, itemY + 4, 0xFF888888, false);
                } else {
                    NodeDefinition def = res.node;
                    String name = Component.translatable(def.name()).getString();
                    BlueprintMenuRenderer.renderHighlightedString(guiGraphics, font, name, x + 8, itemY + 4, 0xFFFFFFFF, searchQuery);
                    
                    if (res.matchedType != null) {
                        guiGraphics.drawString(font, "[TYPE: " + res.matchedType + "]", x + 8 + font.width(name) + 4, itemY + 4, 0xFF55FF55, false);
                    }
                    String cat = Component.translatable(def.category()).getString();
                    BlueprintMenuRenderer.renderHighlightedString(guiGraphics, font, cat, x + width - font.width(cat) - 8, itemY + 4, 0xFF666666, searchQuery);
                }
            }
            guiGraphics.disableScissor();
            BlueprintMenuRenderer.renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);
        }
    }

    private void renderCategoryMode(GuiGraphics guiGraphics, Font font, int x, int contentY, int width, int screenHeight, int mouseX, int mouseY, int maxVisibleItems, int itemHeight, int pathBarHeight, int screenWidth) {
        BlueprintCategoryManager.CategoryData data = BlueprintCategoryManager.getCategoryData(currentPath);
        boolean hasBack = !currentPath.equals(BlueprintCategoryManager.ROOT_PATH);
        int totalItems = (hasBack ? 1 : 0) + data.subCategories.size() + data.directNodes.size();
        int displayCount = Math.min(totalItems, maxVisibleItems);
        int height = displayCount * itemHeight + 6;
        
        if (contentY + height + pathBarHeight > screenHeight) contentY -= (height + pathBarHeight + 27);
        
        BlueprintMenuRenderer.renderBackground(guiGraphics, x, contentY, width, height + pathBarHeight);

        if (pathBarHeight > 0) {
            String pathDisplay = getLocalizedPath(currentPath);
            guiGraphics.fill(x + 1, contentY + 1, x + width - 1, contentY + pathBarHeight, 0x33000000);
            guiGraphics.drawString(font, pathDisplay, x + 6, contentY + 2, 0xFF666666, false);
            contentY += pathBarHeight;
        }

        lastMenuContentY = contentY;
        lastMenuHeight = height;
        
        guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
        int totalHeight = totalItems * itemHeight;
        scrollAmount = Mth.clamp(scrollAmount, 0, Math.max(0, totalHeight - (displayCount * itemHeight)));

        String currentHoveredCatInMain = null;
        int currentIdx = 0;
        
        if (hasBack) {
            int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
            if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                }
                guiGraphics.drawString(font, "<- " + Component.translatable("gui.mgmc.blueprint_selection.back").getString(), x + 8, itemY + 4, 0xFFAAAAAA, false);
            }
            currentIdx++;
        }

        for (String subPath : data.subCategories) {
            int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
            if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                    currentHoveredCatInMain = subPath;
                }
                guiGraphics.drawString(font, Component.translatable(subPath), x + 8, itemY + 4, 0xFFFFFFFF, false);
                guiGraphics.drawString(font, ">", x + width - 15, itemY + 4, 0xFF888888, false);
            }
            currentIdx++;
        }

        for (NodeDefinition def : data.directNodes) {
            int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
            if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                }
                guiGraphics.drawString(font, Component.translatable(def.name()), x + 8, itemY + 4, 0xFFFFFFFF, false);
            }
            currentIdx++;
        }
        guiGraphics.disableScissor();
        BlueprintMenuRenderer.renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);

        updateSubmenu(mouseX, mouseY, x, width, contentY, height, screenWidth, screenHeight, font, itemHeight, maxVisibleItems, hasBack, data.subCategories, currentHoveredCatInMain);
        
        if (hoveredCategory != null) {
            renderSubmenu(guiGraphics, font, mouseX, mouseY, x, width, contentY, itemHeight, maxVisibleItems, screenWidth, screenHeight);
        }
    }

    private void updateSubmenu(int mouseX, int mouseY, int x, int width, int contentY, int height, int screenWidth, int screenHeight, Font font, int itemHeight, int maxVisibleItems, boolean hasBack, List<String> subCategories, String currentHoveredCatInMain) {
        if (currentHoveredCatInMain != null) {
            if (!currentHoveredCatInMain.equals(hoveredCategory)) {
                hoveredCategory = currentHoveredCatInMain;
                subScrollAmount = 0;
            }
        } else if (hoveredCategory != null) {
            boolean mouseInMainMenu = mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height;
            if (mouseInMainMenu) {
                hoveredCategory = null;
            } else {
                List<NodeDefinition> catNodes = BlueprintCategoryManager.getNodesInCategory(hoveredCategory);
                if (catNodes.isEmpty()) {
                    hoveredCategory = null;
                } else {
                    int maxSubW = 150;
                    for (NodeDefinition def : catNodes) maxSubW = Math.max(maxSubW, font.width(Component.translatable(def.name()).getString()) + 30);
                    subMenuWidth = Math.min(maxSubW, screenWidth / 3);
                    int subX = (x + width + subMenuWidth > screenWidth) ? x - subMenuWidth : x + width;
                    int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                    int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                    int subHeight = Math.min(catNodes.size(), maxVisibleItems) * itemHeight + 6;
                    if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                    if (subY < 0) subY = 5;
                    if (!(mouseX >= subX && mouseX <= subX + subMenuWidth && mouseY >= subY && mouseY <= subY + subHeight)) hoveredCategory = null;
                }
            }
        }
    }

    private void renderSubmenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, int x, int width, int contentY, int itemHeight, int maxVisibleItems, int screenWidth, int screenHeight) {
        List<NodeDefinition> catNodes = BlueprintCategoryManager.getNodesInCategory(hoveredCategory);
        if (catNodes.isEmpty()) return;

        int subX = (x + width + subMenuWidth > screenWidth) ? x - subMenuWidth : x + width;
        BlueprintCategoryManager.CategoryData currentData = BlueprintCategoryManager.getCategoryData(currentPath);
        int catItemIdx = (!currentPath.equals(BlueprintCategoryManager.ROOT_PATH) ? 1 : 0) + currentData.subCategories.indexOf(hoveredCategory);
        int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
        int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
        int subHeight = subDisplayCount * itemHeight + 6;
        
        if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
        if (subY < 0) subY = 5;

        BlueprintMenuRenderer.renderBackground(guiGraphics, subX, subY, subMenuWidth, subHeight);
        guiGraphics.enableScissor(subX, subY + 3, subX + subMenuWidth, subY + subHeight - 3);
        int subTotalHeight = catNodes.size() * itemHeight;
        subScrollAmount = Mth.clamp(subScrollAmount, 0, Math.max(0, subTotalHeight - (subDisplayCount * itemHeight)));

        for (int i = 0; i < catNodes.size(); i++) {
            NodeDefinition def = catNodes.get(i);
            int itemY = subY + 3 + i * itemHeight - (int)subScrollAmount;
            if (itemY + itemHeight >= subY && itemY <= subY + subHeight) {
                if (mouseX >= subX && mouseX <= subX + subMenuWidth && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    guiGraphics.fill(subX + 1, itemY, subX + subMenuWidth - 1, itemY + itemHeight, 0x44FFFFFF);
                }
                guiGraphics.drawString(font, Component.translatable(def.name()), subX + 8, itemY + 4, 0xFFFFFFFF, false);
            }
        }
        guiGraphics.disableScissor();
        BlueprintMenuRenderer.renderScrollbar(guiGraphics, subX + subMenuWidth - 4, subY + 3, 2, subHeight - 6, subScrollAmount, subTotalHeight);
    }

    private String getLocalizedPath(String path) {
        if (path.equals(BlueprintCategoryManager.ROOT_PATH)) return "/";
        String[] parts = path.split("\\.");
        StringBuilder fullPath = new StringBuilder();
        for (String part : parts) {
            if (part.equals("node_category") || part.equals("mgmc")) continue;
            fullPath.append("/").append(Component.translatable(path.substring(0, path.indexOf(part) + part.length())).getString());
        }
        return fullPath.length() == 0 ? "/" : fullPath.toString();
    }

    public void mouseScrolled(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight, double amount) {
        BlueprintMenuInputHandler.handleMouseScrolled(this, mouseX, mouseY, menuX, menuY, screenWidth, screenHeight, amount);
    }

    public boolean keyPressed(int key) {
        return BlueprintMenuInputHandler.handleKeyPressed(this, key);
    }

    public boolean charTyped(char codePoint) {
        return BlueprintMenuInputHandler.handleCharTyped(this, codePoint);
    }

    public NodeDefinition onClickNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        return BlueprintMenuInputHandler.handleOnClickNodeMenu(this, mouseX, mouseY, menuX, menuY, screenWidth, screenHeight);
    }

    public boolean isClickInsideNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        return BlueprintMenuInputHandler.handleIsClickInsideNodeMenu(this, mouseX, mouseY, menuX, menuY, screenWidth, screenHeight);
    }

    public enum ContextMenuResult { DELETE, BREAK_LINKS, NONE }

    public ContextMenuResult onClickContextMenu(double mouseX, double mouseY, double menuX, double menuY) {
        return BlueprintMenuInputHandler.handleOnClickContextMenu(mouseX, mouseY, menuX, menuY);
    }
}


