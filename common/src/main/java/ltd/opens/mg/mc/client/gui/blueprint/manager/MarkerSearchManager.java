package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.components.GuiNode;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import dev.architectury.platform.Platform;

import java.util.*;

public class MarkerSearchManager {
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

    public static class MarkerSearchResult {
        public final GuiNode node;
        public int score;

        public MarkerSearchResult(GuiNode node, int score) {
            this.node = node;
            this.score = score;
        }
    }

    public static List<GuiNode> performSearch(List<GuiNode> allNodes, String query) {
        if (query == null || query.isEmpty()) return new ArrayList<>();

        String fullQuery = query.toLowerCase();
        String[] terms = fullQuery.split("\\s+");
        List<MarkerSearchResult> results = new ArrayList<>();

        for (GuiNode node : allNodes) {
            if (!node.definition.properties().containsKey("is_marker")) continue;

            String comment = node.inputValues.has(NodePorts.COMMENT) ? 
                             node.inputValues.get(NodePorts.COMMENT).getAsString() : "";
            
            int score = calculateScore(terms, fullQuery, comment);
            if (score > 0) {
                results.add(new MarkerSearchResult(node, score));
            }
        }

        // Sort by score descending
        results.sort((a, b) -> Integer.compare(b.score, a.score));

        List<GuiNode> sortedNodes = new ArrayList<>();
        for (MarkerSearchResult res : results) {
            sortedNodes.add(res.node);
        }
        return sortedNodes;
    }

    private static int calculateScore(String[] terms, String fullQuery, String comment) {
        int totalScore = 0;
        String lowerComment = comment.toLowerCase();

        for (String term : terms) {
            boolean termMatched = false;

            if (matches(comment, term)) {
                totalScore += 10;
                // Bonus for exact start
                if (lowerComment.startsWith(term)) totalScore += 5;
                termMatched = true;
            }

            if (!termMatched) return 0; // All terms must match
        }

        // Bonus for full query match
        if (matches(comment, fullQuery)) {
            totalScore += 20;
            if (lowerComment.equals(fullQuery)) totalScore += 50;
        }

        return totalScore;
    }
}
