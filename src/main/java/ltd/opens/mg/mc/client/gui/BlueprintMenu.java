package ltd.opens.mg.mc.client.gui;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintMenu {
    private String searchQuery = "";
    private List<SearchResult> filteredResults = new ArrayList<>();
    private int selectedIndex = -1;
    private double scrollAmount = 0;
    private double subScrollAmount = 0;
    private int menuWidth = 150;
    private int subMenuWidth = 150;
    private int lastMenuContentY = 0;
    private int lastMenuHeight = 0;
    private String hoveredCategory = null;
    private String currentPath = "node_category.mgmc";

    private static class SearchResult {
        final NodeDefinition node;
        final String category;
        String matchedPort = null;
        String matchedType = null;
        int score = 0;
        SearchResult(NodeDefinition node) { this.node = node; this.category = null; }
        SearchResult(String category) { this.category = category; this.node = null; }
        boolean isNode() { return node != null; }
        boolean isCategory() { return category != null; }
    }

    public void renderNodeContextMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        int height = 46;
        
        // Shadow/Glow
        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x44000000);
        // Background
        guiGraphics.fill(x, y, x + width, y + height, 0xF01E1E1E);
        // Border
        guiGraphics.renderOutline(x, y, width, height, 0xFF444444);
        
        // Delete Node
        boolean hoverDelete = mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23;
        if (hoverDelete) guiGraphics.fill(x + 1, y + 3, x + width - 1, y + 23, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.delete"), x + 8, y + 8, 0xFFFFFFFF, false);
        
        // Break Links
        boolean hoverBreak = mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43;
        if (hoverBreak) guiGraphics.fill(x + 1, y + 23, x + width - 1, y + 43, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.break_links"), x + 8, y + 28, 0xFFFFFFFF, false);
    }

    public void renderNodeMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        
        // 1. Calculate Menu Width
        if (!searchQuery.isEmpty()) {
            updateSearch();
            int maxW = 180; // Minimum width
            for (SearchResult res : filteredResults) {
                int itemW = 0;
                if (res.isCategory()) {
                    itemW = font.width(Component.translatable(res.category).getString()) + 40;
                } else {
                    int nameW = font.width(Component.translatable(res.node.name()).getString());
                    int typeW = 0;
                    if (res.matchedType != null) typeW = font.width("[TYPE: " + res.matchedType + "]") + 10;
                    else if (res.matchedPort != null) typeW = font.width("[" + res.matchedPort + "]") + 10;
                    int catW = font.width(Component.translatable(res.node.category()).getString());
                    itemW = 8 + nameW + typeW + catW + 20;
                }
                maxW = Math.max(maxW, itemW);
            }
            menuWidth = Math.min(maxW, screenWidth / 2);
        } else {
            // Category Mode Width Calculation
            List<String> subCategories = new ArrayList<>();
            List<NodeDefinition> directNodes = new ArrayList<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                if (def.category().equals(currentPath)) directNodes.add(def);
                else if (def.category().startsWith(currentPath + ".")) {
                    String sub = def.category().substring(currentPath.length() + 1);
                    int dot = sub.indexOf('.');
                    String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                    String fullSubPath = currentPath + "." + immediateSub;
                    if (!subCategories.contains(fullSubPath)) subCategories.add(fullSubPath);
                }
            }
            int maxW = 180;
            if (!currentPath.equals("node_category.mgmc")) {
                maxW = Math.max(maxW, font.width("<- " + Component.translatable("gui.mgmc.blueprint_selection.back").getString()) + 30);
            }
            for (String sub : subCategories) {
                maxW = Math.max(maxW, font.width(Component.translatable(sub).getString()) + 40);
            }
            for (NodeDefinition def : directNodes) {
                maxW = Math.max(maxW, font.width(Component.translatable(def.name()).getString()) + 30);
            }
            menuWidth = Math.min(maxW, screenWidth / 3);
        }

        int width = menuWidth;
        
        // Handle screen boundaries
        if (x + width > screenWidth) x -= width;
        
        // 1. Render Search Box
        int searchHeight = 25;
        guiGraphics.fill(x, y, x + width, y + searchHeight, 0xF0121212);
        guiGraphics.renderOutline(x, y, width, searchHeight, 0xFF555555);
        
        Component searchHint = Component.translatable("gui.mgmc.blueprint_editor.search_hint");
        String displaySearch = searchQuery.isEmpty() ? searchHint.getString() : searchQuery;
        int searchColor = searchQuery.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;
        guiGraphics.drawString(font, displaySearch + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : ""), x + 8, y + (searchHeight - 9) / 2, searchColor, false);

        int contentY = y + searchHeight + 2;
        int maxVisibleItems = 12;
        int itemHeight = 18;
        int pathBarHeight = (searchQuery.isEmpty()) ? 12 : 0;
        
        if (!searchQuery.isEmpty()) {
            // --- Search Results Mode ---
            int displayCount = Math.min(filteredResults.size(), maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            
            if (contentY + height > screenHeight) contentY = y - height;
            lastMenuContentY = contentY;
            lastMenuHeight = height;

            guiGraphics.fill(x, contentY, x + width, contentY + height, 0xF01E1E1E);
            guiGraphics.renderOutline(x, contentY, width, height, 0xFF444444);

            if (filteredResults.isEmpty()) {
                guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.no_nodes_found"), x + 8, contentY + 8, 0xFF888888, false);
            } else {
                guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
                int totalHeight = filteredResults.size() * itemHeight;
                int maxScroll = Math.max(0, totalHeight - (displayCount * itemHeight));
                scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
                
                for (int i = 0; i < filteredResults.size(); i++) {
                    SearchResult res = filteredResults.get(i);
                    int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                    
                    // Optimization: only render visible items
                    if (itemY + itemHeight < contentY || itemY > contentY + height) continue;
                    
                    boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                    
                    if (hovered || i == selectedIndex) {
                        guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                    }
                    
                    if (res.isCategory()) {
                        String catName = Component.translatable(res.category).getString();
                        renderHighlightedString(guiGraphics, font, catName, x + 8, itemY + 4, 0xFF88FFFF, searchQuery);
                        guiGraphics.drawString(font, ">", x + width - 15, itemY + 4, 0xFF888888, false);
                    } else {
                        NodeDefinition def = res.node;
                        String name = Component.translatable(def.name()).getString();
                        String cat = Component.translatable(def.category()).getString();
                        renderHighlightedString(guiGraphics, font, name, x + 8, itemY + 4, 0xFFFFFFFF, searchQuery);
                        
                        if (res.matchedType != null) {
                            String typeInfo = "[TYPE: " + res.matchedType + "]";
                            int nameW = font.width(name);
                            guiGraphics.drawString(font, typeInfo, x + 8 + nameW + 4, itemY + 4, 0xFF55FF55, false);
                        } else if (res.matchedPort != null) {
                            String portInfo = "[" + res.matchedPort + "]";
                            int nameW = font.width(name);
                            guiGraphics.drawString(font, portInfo, x + 8 + nameW + 4, itemY + 4, 0xFFFFAA00, false);
                        }

                        int catW = font.width(cat);
                        renderHighlightedString(guiGraphics, font, cat, x + width - catW - 8, itemY + 4, 0xFF666666, searchQuery);
                    }
                }
                guiGraphics.disableScissor();
                
                // Scrollbar
                if (totalHeight > height - 6) {
                    renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);
                }
            }
        } else {
            // --- Category Mode ---
            // Re-use lists calculated for width
            List<String> subCategories = new ArrayList<>();
            List<NodeDefinition> directNodes = new ArrayList<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                if (def.category().equals(currentPath)) directNodes.add(def);
                else if (def.category().startsWith(currentPath + ".")) {
                    String sub = def.category().substring(currentPath.length() + 1);
                    int dot = sub.indexOf('.');
                    String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                    String fullSubPath = currentPath + "." + immediateSub;
                    if (!subCategories.contains(fullSubPath)) subCategories.add(fullSubPath);
                }
            }
            subCategories.sort((a, b) -> Component.translatable(a).getString().compareTo(Component.translatable(b).getString()));
            directNodes.sort((a, b) -> Component.translatable(a.name()).getString().compareTo(Component.translatable(b.name()).getString()));

            boolean hasBack = !currentPath.equals("node_category.mgmc");
            int totalItems = (hasBack ? 1 : 0) + subCategories.size() + directNodes.size();
            int displayCount = Math.min(totalItems, maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            
            if (contentY + height + pathBarHeight > screenHeight) contentY = y - height - pathBarHeight;
            
            // Render Background
            guiGraphics.fill(x, contentY, x + width, contentY + height + pathBarHeight, 0xF01E1E1E);
            guiGraphics.renderOutline(x, contentY, width, height + pathBarHeight, 0xFF444444);

            // Render Path Bar (Unobtrusive)
            if (pathBarHeight > 0) {
                String pathDisplay = "/";
                if (!currentPath.equals("node_category.mgmc")) {
                    String[] parts = currentPath.split("\\.");
                    StringBuilder fullPath = new StringBuilder();
                    for (String part : parts) {
                        if (part.equals("node_category") || part.equals("mgmc")) continue;
                        fullPath.append("/").append(Component.translatable(currentPath.substring(0, currentPath.indexOf(part) + part.length())).getString());
                    }
                    pathDisplay = fullPath.length() == 0 ? "/" : fullPath.toString();
                }

                // Draw a very subtle background for path
                guiGraphics.fill(x + 1, contentY + 1, x + width - 1, contentY + pathBarHeight, 0x33000000);
                guiGraphics.drawString(font, pathDisplay, x + 6, contentY + 2, 0xFF666666, false);
                
                contentY += pathBarHeight;
            }

            lastMenuContentY = contentY;
            lastMenuHeight = height;
            
            guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
            int totalHeight = totalItems * itemHeight;
            int maxScroll = Math.max(0, totalHeight - (displayCount * itemHeight));
            scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);

            String currentHoveredCatInMain = null;
            int currentIdx = 0;
            // Render Back
            if (hasBack) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                    if (hovered) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                }
                    guiGraphics.drawString(font, "<- " + Component.translatable("gui.mgmc.blueprint_selection.back").getString(), x + 8, itemY + 4, 0xFFAAAAAA, false);
                }
                currentIdx++;
            }

            // Render Sub-categories
            for (String subPath : subCategories) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                    if (hovered) {
                        guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                        currentHoveredCatInMain = subPath;
                    }
                    guiGraphics.drawString(font, Component.translatable(subPath), x + 8, itemY + 4, 0xFFFFFFFF, false);
                    guiGraphics.drawString(font, ">", x + width - 15, itemY + 4, 0xFF888888, false);
                }
                currentIdx++;
            }

            // Render Direct Nodes
            for (NodeDefinition def : directNodes) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                    if (hovered) {
                        guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                    }
                    guiGraphics.drawString(font, Component.translatable(def.name()), x + 8, itemY + 4, 0xFFFFFFFF, false);
                }
                currentIdx++;
            }
            guiGraphics.disableScissor();
            
            if (totalHeight > height - 6) {
                renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);
            }

            // --- Submenu Logic ---
            if (currentHoveredCatInMain != null) {
                if (!currentHoveredCatInMain.equals(hoveredCategory)) {
                    hoveredCategory = currentHoveredCatInMain;
                    subScrollAmount = 0;
                }
            } else {
                // If not hovering a category in main menu, check if we should keep the current submenu
                if (hoveredCategory != null) {
                    // Check if mouse is in the main menu area at all
                    boolean mouseInMainMenu = mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height;
                    if (mouseInMainMenu) {
                        // Mouse is in main menu but not over a category -> clear submenu
                        hoveredCategory = null;
                    } else {
                        // Mouse is outside main menu, check if it's in the submenu area
                        List<NodeDefinition> catNodes = NodeRegistry.getAll().stream()
                            .filter(def -> def.category().startsWith(hoveredCategory))
                            .collect(Collectors.toList());
                        
                        if (catNodes.isEmpty()) {
                            hoveredCategory = null;
                        } else {
                            // Calculate Submenu Width
                            int maxSubW = 150;
                            for (NodeDefinition def : catNodes) {
                                maxSubW = Math.max(maxSubW, font.width(Component.translatable(def.name()).getString()) + 30);
                            }
                            subMenuWidth = Math.min(maxSubW, screenWidth / 3);
                            int subWidth = subMenuWidth;
                            int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                            int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                            int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                            int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                            int subHeight = subDisplayCount * itemHeight + 6;
                            
                            if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                            if (subY < 0) subY = 5;

                            boolean mouseInSubMenu = mouseX >= subX && mouseX <= subX + subWidth && mouseY >= subY && mouseY <= subY + subHeight;
                            if (!mouseInSubMenu) {
                                hoveredCategory = null;
                            }
                        }
                    }
                }
            }
            
            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = NodeRegistry.getAll().stream()
                    .filter(def -> def.category().startsWith(hoveredCategory))
                    .sorted((a, b) -> Component.translatable(a.name()).getString().compareTo(Component.translatable(b.name()).getString()))
                    .collect(Collectors.toList());

                if (!catNodes.isEmpty()) {
                    int subWidth = subMenuWidth;
                    int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                    
                    int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                    int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                    int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                    int subHeight = subDisplayCount * itemHeight + 6;
                    
                    if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                    if (subY < 0) subY = 5;

                    guiGraphics.fill(subX, subY, subX + subWidth, subY + subHeight, 0xF01E1E1E);
                    guiGraphics.renderOutline(subX, subY, subWidth, subHeight, 0xFF444444);
                    
                    guiGraphics.enableScissor(subX, subY + 3, subX + subWidth, subY + subHeight - 3);
                    int subTotalHeight = catNodes.size() * itemHeight;
                    int subMaxScroll = Math.max(0, subTotalHeight - (subDisplayCount * itemHeight));
                    subScrollAmount = Mth.clamp(subScrollAmount, 0, subMaxScroll);

                    for (int i = 0; i < catNodes.size(); i++) {
                        NodeDefinition def = catNodes.get(i);
                        int itemY = subY + 3 + i * itemHeight - (int)subScrollAmount;
                        
                        if (itemY + itemHeight < subY || itemY > subY + subHeight) continue;

                        boolean hovered = mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + itemHeight;
                        if (hovered) {
                            guiGraphics.fill(subX + 1, itemY, subX + subWidth - 1, itemY + itemHeight, 0x44FFFFFF);
                        }
                        guiGraphics.drawString(font, Component.translatable(def.name()), subX + 8, itemY + 4, 0xFFFFFFFF, false);
                    }
                    guiGraphics.disableScissor();
                    
                    if (subTotalHeight > subHeight - 6) {
                        renderScrollbar(guiGraphics, subX + subWidth - 4, subY + 3, 2, subHeight - 6, subScrollAmount, subTotalHeight);
                    }
                }
            }
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int width, int height, double scroll, int totalHeight) {
        guiGraphics.fill(x, y, x + width, y + height, 0x22FFFFFF);
        int barHeight = (int) ((height / (float) totalHeight) * height);
        int barY = y + (int) ((scroll / (float) totalHeight) * height);
        guiGraphics.fill(x, barY, x + width, barY + barHeight, 0x88FFFFFF);
    }

    private String getLocalizedPath(String path) {
        if (path.equals("node_category.mgmc")) return "/";
        String[] parts = path.split("\\.");
        StringBuilder sb = new StringBuilder();
        String current = "";
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) current += ".";
            current += parts[i];
            if (parts[i].equals("node_category") || parts[i].equals("mgmc")) continue;
            sb.append("/").append(Component.translatable(current).getString());
        }
        return sb.length() == 0 ? "/" : sb.toString();
    }

    private void updateSearch() {
        if (searchQuery.isEmpty()) {
            filteredResults.clear();
            return;
        }

        String fullQuery = searchQuery.toLowerCase();
        String[] terms = fullQuery.split("\\s+");
        
        filteredResults.clear();

        // 1. Process Nodes
        for (NodeDefinition def : NodeRegistry.getAll()) {
            String localizedName = Component.translatable(def.name()).getString().toLowerCase();
            String localizedCat = Component.translatable(def.category()).getString().toLowerCase();
            String rawName = def.name().toLowerCase();
            String rawCat = def.category().toLowerCase();
            
            String locPath = (getLocalizedPath(def.category()) + "/" + localizedName).toLowerCase();
            String rawPath = (def.category().replace(".", "/") + "/" + rawName).toLowerCase();

            SearchResult res = new SearchResult(def);
            int score = calculateScore(terms, fullQuery, localizedName, rawName, localizedCat, rawCat, locPath, rawPath, false, res, def);
            if (score > 0) {
                res.score = score;
                filteredResults.add(res);
            }
        }

        // 2. Process Categories
        Set<String> categories = new HashSet<>();
        for (NodeDefinition def : NodeRegistry.getAll()) {
            String cat = def.category();
            String[] parts = cat.split("\\.");
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) current.append(".");
                current.append(parts[i]);
                categories.add(current.toString());
            }
        }

        for (String cat : categories) {
            // Filter out root category and internal identifiers
            if (cat.equals("node_category") || cat.equals("node_category.mgmc")) continue;
            
            String localizedCatName = Component.translatable(cat).getString().toLowerCase();
            String rawCatName = cat.toLowerCase();
            
            String locPath = getLocalizedPath(cat).toLowerCase();
            String rawPath = cat.replace(".", "/").toLowerCase();
            
            SearchResult res = new SearchResult(cat);
            int score = calculateScore(terms, fullQuery, localizedCatName, rawCatName, "", "", locPath, rawPath, true, res, null);
            if (score > 0) {
                res.score = score;
                filteredResults.add(res);
            }
        }

        // 3. Sort by score, then by name
        filteredResults.sort((a, b) -> {
            if (a.score != b.score) return b.score - a.score;
            String nameA = a.isNode() ? Component.translatable(a.node.name()).getString() : Component.translatable(a.category).getString();
            String nameB = b.isNode() ? Component.translatable(b.node.name()).getString() : Component.translatable(b.category).getString();
            return nameA.compareTo(nameB);
        });
        
        if (selectedIndex >= filteredResults.size()) {
            selectedIndex = Math.max(0, filteredResults.size() - 1);
        }
    }

    private int calculateScore(String[] terms, String fullQuery, String locName, String rawName, String locCat, String rawCat, String locPath, String rawPath, boolean isFolder, SearchResult res, NodeDefinition def) {
        int totalScore = 0;
        
        for (String term : terms) {
            boolean termMatched = false;
            
            // Port search tags: @ (any), in: (input), out: (output)
            if (def != null && (term.startsWith("@") || term.startsWith("in:") || term.startsWith("out:"))) {
                // ... (port search logic remains same)
                String portQuery;
                boolean checkIn = true, checkOut = true;
                
                if (term.startsWith("in:")) {
                    portQuery = term.substring(3);
                    checkOut = false;
                } else if (term.startsWith("out:")) {
                    portQuery = term.substring(4);
                    checkIn = false;
                } else {
                    portQuery = term.substring(1);
                }

                if (!portQuery.isEmpty()) {
                    List<NodeDefinition.PortDefinition> allPorts = new ArrayList<>();
                    if (checkIn) allPorts.addAll(def.inputs());
                    if (checkOut) allPorts.addAll(def.outputs());

                    for (NodeDefinition.PortDefinition port : allPorts) {
                        String pId = port.id().toLowerCase();
                        String localizedPortName = Component.translatable(port.displayName()).getString();
                        String pName = localizedPortName.toLowerCase();
                        String pRawName = port.displayName().toLowerCase();
                        String pType = port.type().name().toLowerCase();

                        boolean matched = false;
                        if (portQuery.contains("*") || portQuery.contains("?")) {
                            String regex = portQuery.replace(".", "\\.").replace("?", ".").replace("*", ".*");
                            try {
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
                                matched = pattern.matcher(pId).find() || pattern.matcher(pName).find() || 
                                          pattern.matcher(pRawName).find() || pattern.matcher(pType).find();
                            } catch (Exception ignored) {}
                        } else {
                            matched = pId.contains(portQuery) || pName.contains(portQuery) || 
                                      pRawName.contains(portQuery) || pType.contains(portQuery);
                        }

                        if (matched) {
                            totalScore += 20;
                            termMatched = true;
                            if (pType.contains(portQuery)) {
                                res.matchedType = port.type().name();
                            } else {
                                if (res.matchedPort == null) res.matchedPort = localizedPortName;
                            }
                        }
                    }
                }
            } else if (term.contains("*") || term.contains("?")) {
                String regex = term.replace(".", "\\.").replace("?", ".").replace("*", ".*");
                try {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(locName).matches() || pattern.matcher(rawName).matches()) {
                        totalScore += 15;
                        termMatched = true;
                    } else if (pattern.matcher(locName).find() || pattern.matcher(rawName).find()) {
                        totalScore += 10;
                        termMatched = true;
                    } else if (pattern.matcher(locCat).find() || pattern.matcher(rawCat).find()) {
                        totalScore += 5;
                        termMatched = true;
                    } else if (pattern.matcher(locPath).find() || pattern.matcher(rawPath).find()) {
                        totalScore += 8;
                        termMatched = true;
                    }
                } catch (Exception ignored) {}
            } else {
                // Normal matching
                if (locName.contains(term) || rawName.contains(term)) {
                    totalScore += 10;
                    termMatched = true;
                }
                if (locCat.contains(term) || rawCat.contains(term)) {
                    totalScore += 5;
                    termMatched = true;
                }
                if (locPath.contains(term) || rawPath.contains(term)) {
                    totalScore += 8;
                    termMatched = true;
                }
            }
            
            if (!termMatched) return 0;
        }

        // Bonus for full query matches (non-wildcard)
        if (!fullQuery.contains("*") && !fullQuery.contains("?")) {
            if (locName.equals(fullQuery) || rawName.equals(fullQuery)) totalScore += 100;
            else if (locName.startsWith(fullQuery) || rawName.startsWith(fullQuery)) totalScore += 50;
            
            if (locCat.equals(fullQuery) || rawCat.equals(fullQuery)) totalScore += 40;
            else if (locCat.startsWith(fullQuery) || rawCat.startsWith(fullQuery)) totalScore += 20;

            if (locPath.equals(fullQuery) || rawPath.equals(fullQuery)) totalScore += 60;
            else if (locPath.startsWith(fullQuery) || rawPath.startsWith(fullQuery)) totalScore += 30;
        }

        if (isFolder) totalScore -= 2;

        return totalScore;
    }

    private void renderHighlightedString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, String query) {
        if (query.isEmpty()) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        String activeQuery = query;
        int matchStart = -1;
        int matchEnd = -1;

        // Helper to perform the match
        java.util.function.BiFunction<String, String, int[]> performMatch = (t, q) -> {
            if (q.contains("*") || q.contains("?")) {
                String regex = q.replace(".", "\\.").replace("?", ".").replace("*", ".*");
                try {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher matcher = pattern.matcher(t);
                    if (matcher.find()) return new int[]{matcher.start(), matcher.end()};
                } catch (Exception ignored) {}
            } else {
                int start = t.toLowerCase().indexOf(q.toLowerCase());
                if (start != -1) return new int[]{start, start + q.length()};
            }
            return null;
        };

        int[] match = performMatch.apply(text, activeQuery);
        if (match != null) {
            matchStart = match[0];
            matchEnd = match[1];
        } else if (query.contains("/")) {
            // Try matching parts of the path
            String[] parts = query.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i].isEmpty()) continue;
                match = performMatch.apply(text, parts[i]);
                if (match != null) {
                    matchStart = match[0];
                    matchEnd = match[1];
                    activeQuery = parts[i];
                    break;
                }
            }
        }

        if (matchStart == -1) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        // Before match
        String before = text.substring(0, matchStart);
        guiGraphics.drawString(font, before, x, y, color, false);
        int curX = x + font.width(before);

        // Match (Highlight with yellow)
        String matchText = text.substring(matchStart, matchEnd);
        guiGraphics.drawString(font, matchText, curX, y, 0xFFFFFF00, false);
        curX += font.width(matchText);

        // After match
        String after = text.substring(matchEnd);
        guiGraphics.drawString(font, after, curX, y, color, false);
    }

    public void reset() {
        currentPath = "node_category.mgmc";
        hoveredCategory = null;
        searchQuery = "";
        selectedIndex = 0;
        filteredResults.clear();
        scrollAmount = 0;
        subScrollAmount = 0;
    }

    public void mouseScrolled(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight, double amount) {
        if (hoveredCategory != null) {
            int x = (int) menuX;
            int y = (int) menuY;
            int width = menuWidth;
            if (x + width > screenWidth) x -= width;
            
            // Submenu detection logic
            // ... (rest of the code should use width and subWidth)
            List<String> subCategories = new ArrayList<>();
            List<NodeDefinition> directNodes = new ArrayList<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                if (def.category().equals(currentPath)) directNodes.add(def);
                else if (def.category().startsWith(currentPath + ".")) {
                    String sub = def.category().substring(currentPath.length() + 1);
                    int dot = sub.indexOf('.');
                    String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                    String fullSubPath = currentPath + "." + immediateSub;
                    if (!subCategories.contains(fullSubPath)) subCategories.add(fullSubPath);
                }
            }
            boolean hasBack = !currentPath.equals("node_category.mgmc");
            int totalItems = (hasBack ? 1 : 0) + subCategories.size() + directNodes.size();
            int maxVisibleItems = 12;
            int itemHeight = 18;
            int displayCount = Math.min(totalItems, maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            int contentY = y + 20 + 25 + 2;
            if (contentY + height > screenHeight) contentY = y - height;

            List<NodeDefinition> catNodes = NodeRegistry.getAll().stream()
                .filter(def -> def.category().startsWith(hoveredCategory))
                .collect(Collectors.toList());
            
            if (!catNodes.isEmpty()) {
                int subWidth = subMenuWidth;
                int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                int subHeight = subDisplayCount * itemHeight + 6;
                
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;

                if (mouseX >= subX && mouseX <= subX + subWidth && mouseY >= subY && mouseY <= subY + subHeight) {
                    subScrollAmount -= (float) (amount * 15);
                    return;
                }
            }
        }
        scrollAmount -= (float) (amount * 15);
    }

    public enum ContextMenuResult {
        DELETE, BREAK_LINKS, NONE
    }

    public ContextMenuResult onClickContextMenu(double mouseX, double mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23) {
            return ContextMenuResult.DELETE;
        }
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43) {
            return ContextMenuResult.BREAK_LINKS;
        }
        return ContextMenuResult.NONE;
    }

    public NodeDefinition onClickNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = menuWidth;
        if (x + width > screenWidth) x -= width;

        int itemHeight = 18;
        int maxVisibleItems = 12;

        if (!searchQuery.isEmpty()) {
            int contentY = lastMenuContentY;
            int height = lastMenuHeight;

            for (int i = 0; i < filteredResults.size(); i++) {
                SearchResult res = filteredResults.get(i);
                int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight < contentY || itemY > contentY + height) continue;
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    if (res.isCategory()) {
                        currentPath = res.category;
                        searchQuery = "";
                        scrollAmount = 0;
                        return null;
                    }
                    return res.node;
                }
            }
        } else {
            // Category navigation logic
            List<String> subCategories = new ArrayList<>();
            List<NodeDefinition> directNodes = new ArrayList<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                if (def.category().equals(currentPath)) directNodes.add(def);
                else if (def.category().startsWith(currentPath + ".")) {
                    String sub = def.category().substring(currentPath.length() + 1);
                    int dot = sub.indexOf('.');
                    String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                    String fullSubPath = currentPath + "." + immediateSub;
                    if (!subCategories.contains(fullSubPath)) subCategories.add(fullSubPath);
                }
            }
            subCategories.sort((a, b) -> Component.translatable(a).getString().compareTo(Component.translatable(b).getString()));
            directNodes.sort((a, b) -> Component.translatable(a.name()).getString().compareTo(Component.translatable(b.name()).getString()));

            boolean hasBack = !currentPath.equals("node_category.mgmc");
            int contentY = lastMenuContentY;
            int height = lastMenuHeight;

            int currentIdx = 0;
            if (hasBack) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                        int lastDot = currentPath.lastIndexOf('.');
                        if (lastDot != -1) currentPath = currentPath.substring(0, lastDot);
                        scrollAmount = 0;
                        return null;
                    }
                }
                currentIdx++;
            }

            for (String subPath : subCategories) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                        currentPath = subPath;
                        scrollAmount = 0;
                        return null;
                    }
                }
                currentIdx++;
            }

            for (NodeDefinition def : directNodes) {
                int itemY = contentY + 3 + currentIdx * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight >= contentY && itemY <= contentY + height) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                        return def;
                    }
                }
                currentIdx++;
            }

            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = NodeRegistry.getAll().stream()
                    .filter(def -> def.category().startsWith(hoveredCategory))
                    .sorted((a, b) -> Component.translatable(a.name()).getString().compareTo(Component.translatable(b.name()).getString()))
                    .collect(Collectors.toList());

                int subWidth = subMenuWidth;
                int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                int subHeight = subDisplayCount * itemHeight + 6;
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;

                for (int i = 0; i < catNodes.size(); i++) {
                    int itemY = subY + 3 + i * itemHeight - (int)subScrollAmount;
                    if (itemY + itemHeight >= subY && itemY <= subY + subHeight) {
                        if (mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                            return catNodes.get(i);
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean isClickInsideNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = menuWidth;
        if (x + width > screenWidth) x -= width;

        // Search box
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25) return true;

        int contentY = lastMenuContentY;
        int height = lastMenuHeight;
        int maxVisibleItems = 12;
        int itemHeight = 18;

        if (!searchQuery.isEmpty()) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height) return true;
        } else {
            if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height) return true;

            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = NodeRegistry.getAll().stream()
                    .filter(def -> def.category().startsWith(hoveredCategory))
                    .collect(Collectors.toList());
                
                if (!catNodes.isEmpty()) {
                    int subWidth = subMenuWidth;
                    int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                    
                    // Use subCategories to find the index of hoveredCategory in the CURRENT path
                    List<String> subCategories = new ArrayList<>();
                    for (NodeDefinition def : NodeRegistry.getAll()) {
                        if (def.category().startsWith(currentPath + ".")) {
                            String sub = def.category().substring(currentPath.length() + 1);
                            int dot = sub.indexOf('.');
                            String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                            String fullSubPath = currentPath + "." + immediateSub;
                            if (!subCategories.contains(fullSubPath)) subCategories.add(fullSubPath);
                        }
                    }
                    subCategories.sort((a, b) -> Component.translatable(a).getString().compareTo(Component.translatable(b).getString()));
                    
                    boolean hasBack = !currentPath.equals("node_category.mgmc");
                    int catItemIdx = (hasBack ? 1 : 0) + subCategories.indexOf(hoveredCategory);
                    int subY = contentY + 3 + catItemIdx * itemHeight - (int)scrollAmount;
                    int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                    int subHeight = subDisplayCount * itemHeight + 6;
                    
                    if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                    if (subY < 0) subY = 5;

                    if (mouseX >= subX && mouseX <= subX + subWidth && mouseY >= subY && mouseY <= subY + subHeight) return true;
                }
            }
        }
        return false;
    }

    public boolean keyPressed(int key) {
        if (key == 259) { // Backspace
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                updateSearch();
                scrollAmount = 0; // Reset scroll on search
                return true;
            }
        } else if (key == 257) { // Enter
            if (!filteredResults.isEmpty() && !searchQuery.isEmpty()) {
                SearchResult res = filteredResults.get(selectedIndex);
                if (res.isCategory()) {
                    currentPath = res.category;
                    searchQuery = "";
                    scrollAmount = 0;
                    return true;
                }
                return true; 
            }
        } else if (key == 265) { // Up
            selectedIndex = Math.max(0, selectedIndex - 1);
            // Auto-scroll to selected
            scrollAmount = Math.min(scrollAmount, selectedIndex * 18);
            return true;
        } else if (key == 264) { // Down
            selectedIndex = Math.min(filteredResults.size() - 1, selectedIndex + 1);
            // Auto-scroll to selected
            scrollAmount = Math.max(scrollAmount, (selectedIndex - 11) * 18);
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint) {
        searchQuery += codePoint;
        updateSearch();
        scrollAmount = 0; // Reset scroll on search
        return true;
    }

    public NodeDefinition getSelectedNode() {
        if (!filteredResults.isEmpty() && selectedIndex >= 0 && selectedIndex < filteredResults.size()) {
            SearchResult res = filteredResults.get(selectedIndex);
            return res.node;
        }
        return null;
    }
}
