package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.components.GuiNode;
import ltd.opens.mg.mc.client.gui.components.GuiRegion;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import dev.architectury.platform.Platform;

import java.util.*;

public class OnEditorSearchManager {
    private static boolean jechLoaded = false;
    private static boolean jechChecked = false;

    private static boolean isJechLoaded() {
        if (!jechChecked) {
            jechLoaded = Platform.isModLoaded("jecharacters");
            jechChecked = true;
        }
        return jechLoaded;
    }

    private static boolean matches(String text, String query) {
        if (text == null || query == null) return false;
        if (text.toLowerCase().contains(query.toLowerCase())) return true;
        
        if (isJechLoaded()) {
            try {
                Class<?> matchClass = Class.forName("me.towdium.jecharacters.utils.Match");
                java.lang.reflect.Method containsMethod = matchClass.getMethod("contains", String.class, CharSequence.class);
                return (boolean) containsMethod.invoke(null, text, query);
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private static class SearchResult {
        public final Object target;
        public int score;

        public SearchResult(Object target, int score) {
            this.target = target;
            this.score = score;
        }
    }

    public static List<Object> performSearch(List<GuiNode> allNodes, List<GuiRegion> allRegions, String query) {
        if (query == null || query.isEmpty()) return new ArrayList<>();

        String fullQuery = query.toLowerCase();
        String[] terms = fullQuery.split("\\s+");
        List<SearchResult> results = new ArrayList<>();

        // Search Markers
        for (GuiNode node : allNodes) {
            if (!node.definition.properties().containsKey("is_marker")) continue;

            String comment = node.inputValues.has(NodePorts.COMMENT) ? 
                             node.inputValues.get(NodePorts.COMMENT).getAsString() : "";
            
            int score = calculateScore(terms, fullQuery, comment);
            if (score > 0) {
                results.add(new SearchResult(node, score));
            }
        }

        // Search Regions
        if (allRegions != null) {
            for (GuiRegion region : allRegions) {
                int score = calculateScore(terms, fullQuery, region.title);
                if (score > 0) {
                    results.add(new SearchResult(region, score));
                }
            }
        }

        // Sort by score descending
        results.sort((a, b) -> Integer.compare(b.score, a.score));

        List<Object> sortedObjects = new ArrayList<>();
        for (SearchResult res : results) {
            sortedObjects.add(res.target);
        }
        return sortedObjects;
    }

    private static int calculateScore(String[] terms, String fullQuery, String text) {
        if (text == null) text = "";
        int totalScore = 0;
        String lowerText = text.toLowerCase();

        for (String term : terms) {
            boolean termMatched = false;

            if (matches(text, term)) {
                totalScore += 10;
                // Bonus for exact start
                if (lowerText.startsWith(term)) totalScore += 5;
                termMatched = true;
            }

            if (!termMatched) return 0; // All terms must match
        }

        // Bonus for full query match
        if (matches(text, fullQuery)) {
            totalScore += 20;
            if (lowerText.equals(fullQuery)) totalScore += 50;
        }

        return totalScore;
    }
}
