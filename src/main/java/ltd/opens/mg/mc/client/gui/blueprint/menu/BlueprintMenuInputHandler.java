package ltd.opens.mg.mc.client.gui.blueprint.menu;

import ltd.opens.mg.mc.client.gui.blueprint.manager.*;


import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import java.util.List;

public class BlueprintMenuInputHandler {

    public static void handleMouseScrolled(BlueprintMenu menu, double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight, double amount) {
        int x = (int) menuX;
        int width = menu.getMenuWidth();
        if (x + width > screenWidth) x -= width;

        if (menu.getHoveredCategory() != null) {
            List<NodeDefinition> catNodes = BlueprintCategoryManager.getNodesInCategory(menu.getHoveredCategory());
            if (!catNodes.isEmpty()) {
                int subX = (x + width + menu.getSubMenuWidth() > screenWidth) ? x - menu.getSubMenuWidth() : x + width;
                BlueprintCategoryManager.CategoryData data = BlueprintCategoryManager.getCategoryData(menu.getCurrentPath());
                int catItemIdx = (!menu.getCurrentPath().equals(BlueprintCategoryManager.ROOT_PATH) ? 1 : 0) + data.subCategories.indexOf(menu.getHoveredCategory());
                int subY = menu.getLastMenuContentY() + 3 + catItemIdx * 18 - (int)menu.getScrollAmount();
                int subHeight = Math.min(catNodes.size(), 12) * 18 + 6;
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;

                if (mouseX >= subX && mouseX <= subX + menu.getSubMenuWidth() && mouseY >= subY && mouseY <= subY + subHeight) {
                    menu.setSubScrollAmount(menu.getSubScrollAmount() - (amount * 15));
                    return;
                }
            }
        }
        menu.setScrollAmount(menu.getScrollAmount() - (amount * 15));
    }

    public static boolean handleKeyPressed(BlueprintMenu menu, int key) {
        if (key == 259) { // Backspace
            String query = menu.getSearchQuery();
            if (!query.isEmpty()) {
                menu.setSearchQuery(query.substring(0, query.length() - 1));
                menu.updateSearch();
                menu.setScrollAmount(0);
                return true;
            }
        } else if (key == 257) { // Enter
            if (!menu.getFilteredResults().isEmpty() && !menu.getSearchQuery().isEmpty()) {
                BlueprintSearchManager.SearchResult res = menu.getFilteredResults().get(menu.getSelectedIndex());
                if (res.isCategory()) {
                    menu.setCurrentPath(res.category);
                    menu.setSearchQuery("");
                    menu.setScrollAmount(0);
                }
                return true;
            }
        } else if (key == 265) { // Up
            menu.setSelectedIndex(Math.max(0, menu.getSelectedIndex() - 1));
            menu.setScrollAmount(Math.min(menu.getScrollAmount(), menu.getSelectedIndex() * 18));
            return true;
        } else if (key == 264) { // Down
            menu.setSelectedIndex(Math.min(menu.getFilteredResults().size() - 1, menu.getSelectedIndex() + 1));
            menu.setScrollAmount(Math.max(menu.getScrollAmount(), (menu.getSelectedIndex() - 11) * 18));
            return true;
        }
        return false;
    }

    public static boolean handleCharTyped(BlueprintMenu menu, char codePoint) {
        menu.setSearchQuery(menu.getSearchQuery() + codePoint);
        menu.updateSearch();
        menu.setScrollAmount(0);
        return true;
    }

    public static NodeDefinition handleOnClickNodeMenu(BlueprintMenu menu, double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int width = menu.getMenuWidth();
        if (x + width > screenWidth) x -= width;

        if (!menu.getSearchQuery().isEmpty()) {
            List<BlueprintSearchManager.SearchResult> filteredResults = menu.getFilteredResults();
            for (int i = 0; i < filteredResults.size(); i++) {
                BlueprintSearchManager.SearchResult res = filteredResults.get(i);
                int itemY = menu.getLastMenuContentY() + 3 + i * 18 - (int)menu.getScrollAmount();
                if (itemY + 18 < menu.getLastMenuContentY() || itemY > menu.getLastMenuContentY() + menu.getLastMenuHeight()) continue;
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 18) {
                    if (res.isCategory()) {
                        menu.setCurrentPath(res.category);
                        menu.setSearchQuery("");
                        menu.setScrollAmount(0);
                        return null;
                    }
                    return res.node;
                }
            }
        } else {
            BlueprintCategoryManager.CategoryData data = BlueprintCategoryManager.getCategoryData(menu.getCurrentPath());
            boolean hasBack = !menu.getCurrentPath().equals(BlueprintCategoryManager.ROOT_PATH);
            int currentIdx = 0;
            if (hasBack) {
                int itemY = menu.getLastMenuContentY() + 3 + currentIdx * 18 - (int)menu.getScrollAmount();
                if (itemY + 18 >= menu.getLastMenuContentY() && itemY <= menu.getLastMenuContentY() + menu.getLastMenuHeight()) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 18) {
                        int lastDot = menu.getCurrentPath().lastIndexOf('.');
                        if (lastDot != -1) menu.setCurrentPath(menu.getCurrentPath().substring(0, lastDot));
                        else menu.setCurrentPath(BlueprintCategoryManager.ROOT_PATH);
                        menu.setScrollAmount(0);
                        return null;
                    }
                }
                currentIdx++;
            }
            for (String subPath : data.subCategories) {
                int itemY = menu.getLastMenuContentY() + 3 + currentIdx * 18 - (int)menu.getScrollAmount();
                if (itemY + 18 >= menu.getLastMenuContentY() && itemY <= menu.getLastMenuContentY() + menu.getLastMenuHeight()) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 18) {
                        menu.setCurrentPath(subPath);
                        menu.setScrollAmount(0);
                        return null;
                    }
                }
                currentIdx++;
            }
            for (NodeDefinition def : data.directNodes) {
                int itemY = menu.getLastMenuContentY() + 3 + currentIdx * 18 - (int)menu.getScrollAmount();
                if (itemY + 18 >= menu.getLastMenuContentY() && itemY <= menu.getLastMenuContentY() + menu.getLastMenuHeight()) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 18) return def;
                }
                currentIdx++;
            }
            if (menu.getHoveredCategory() != null) {
                List<NodeDefinition> catNodes = BlueprintCategoryManager.getNodesInCategory(menu.getHoveredCategory());
                int subX = (x + width + menu.getSubMenuWidth() > screenWidth) ? x - menu.getSubMenuWidth() : x + width;
                int catItemIdx = (hasBack ? 1 : 0) + data.subCategories.indexOf(menu.getHoveredCategory());
                int subY = menu.getLastMenuContentY() + 3 + catItemIdx * 18 - (int)menu.getScrollAmount();
                int subHeight = Math.min(catNodes.size(), 12) * 18 + 6;
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;
                for (int i = 0; i < catNodes.size(); i++) {
                    int itemY = subY + 3 + i * 18 - (int)menu.getSubScrollAmount();
                    if (itemY + 18 >= subY && itemY <= subY + subHeight) {
                        if (mouseX >= subX && mouseX <= subX + menu.getSubMenuWidth() && mouseY >= itemY && mouseY <= itemY + 18) return catNodes.get(i);
                    }
                }
            }
        }
        return null;
    }

    public static boolean handleIsClickInsideNodeMenu(BlueprintMenu menu, double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int width = menu.getMenuWidth();
        if (x + width > screenWidth) x -= width;
        if (mouseX >= x && mouseX <= x + width && mouseY >= menuY && mouseY <= menuY + 25) return true;
        if (mouseX >= x && mouseX <= x + width && mouseY >= menu.getLastMenuContentY() && mouseY <= menu.getLastMenuContentY() + menu.getLastMenuHeight()) return true;
        if (menu.getHoveredCategory() != null) {
            List<NodeDefinition> catNodes = BlueprintCategoryManager.getNodesInCategory(menu.getHoveredCategory());
            if (!catNodes.isEmpty()) {
                int subX = (x + width + menu.getSubMenuWidth() > screenWidth) ? x - menu.getSubMenuWidth() : x + width;
                BlueprintCategoryManager.CategoryData data = BlueprintCategoryManager.getCategoryData(menu.getCurrentPath());
                int catItemIdx = (!menu.getCurrentPath().equals(BlueprintCategoryManager.ROOT_PATH) ? 1 : 0) + data.subCategories.indexOf(menu.getHoveredCategory());
                int subY = menu.getLastMenuContentY() + 3 + catItemIdx * 18 - (int)menu.getScrollAmount();
                int subHeight = Math.min(catNodes.size(), 12) * 18 + 6;
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;
                if (mouseX >= subX && mouseX <= subX + menu.getSubMenuWidth() && mouseY >= subY && mouseY <= subY + subHeight) return true;
            }
        }
        return false;
    }

    public static BlueprintMenu.ContextMenuResult handleOnClickContextMenu(double mouseX, double mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23) return BlueprintMenu.ContextMenuResult.DELETE;
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43) return BlueprintMenu.ContextMenuResult.BREAK_LINKS;
        return BlueprintMenu.ContextMenuResult.NONE;
    }
}


