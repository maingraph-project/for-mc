package ltd.opens.mg.mc.client.gui.blueprint.manager;


import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;
import net.minecraft.network.chat.Component;

import java.util.*;

public class BlueprintSearchManager {
    public static class SearchResult {
        public final NodeDefinition node;
        public final String category;
        public String matchedPort = null;
        public String matchedType = null;
        public int score = 0;

        public SearchResult(NodeDefinition node) {
            this.node = node;
            this.category = null;
        }

        public SearchResult(String category) {
            this.category = category;
            this.node = null;
        }

        public boolean isNode() {
            return node != null;
        }

        public boolean isCategory() {
            return category != null;
        }
    }

    public static List<SearchResult> performSearch(String searchQuery) {
        if (searchQuery.isEmpty()) {
            return new ArrayList<>();
        }

        String fullQuery = searchQuery.toLowerCase();
        String[] terms = fullQuery.split("\\s+");
        List<SearchResult> results = new ArrayList<>();

        // 1. Process Nodes
        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
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
                results.add(res);
            }
        }

        // 2. Process Categories
        Set<String> categories = new HashSet<>();
        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
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
            if (cat.equals("node_category") || cat.equals("node_category.mgmc")) continue;

            String localizedCatName = Component.translatable(cat).getString().toLowerCase();
            String rawCatName = cat.toLowerCase();

            String locPath = getLocalizedPath(cat).toLowerCase();
            String rawPath = cat.replace(".", "/").toLowerCase();

            SearchResult res = new SearchResult(cat);
            int score = calculateScore(terms, fullQuery, localizedCatName, rawCatName, "", "", locPath, rawPath, true, res, null);
            if (score > 0) {
                res.score = score;
                results.add(res);
            }
        }

        // 3. Sort by score, then by name
        results.sort((a, b) -> {
            if (a.score != b.score) return b.score - a.score;
            String nameA = a.isNode() ? Component.translatable(a.node.name()).getString() : Component.translatable(a.category).getString();
            String nameB = b.isNode() ? Component.translatable(b.node.name()).getString() : Component.translatable(b.category).getString();
            return nameA.compareTo(nameB);
        });

        return results;
    }

    private static int calculateScore(String[] terms, String fullQuery, String locName, String rawName, String locCat, String rawCat, String locPath, String rawPath, boolean isFolder, SearchResult res, NodeDefinition def) {
        int totalScore = 0;

        for (String term : terms) {
            boolean termMatched = false;

            // Port search tags: @ (any), in: (input), out: (output)
            if (def != null && (term.startsWith("@") || term.startsWith("in:") || term.startsWith("out:"))) {
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
                            } catch (Exception ignored) {
                            }
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
                } catch (Exception ignored) {
                }
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

    public static String getLocalizedPath(String path) {
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
}


