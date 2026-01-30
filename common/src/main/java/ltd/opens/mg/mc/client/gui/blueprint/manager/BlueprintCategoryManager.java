package ltd.opens.mg.mc.client.gui.blueprint.manager;


import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintCategoryManager {
    public static final String ROOT_PATH = "node_category.mgmc";

    public static class CategoryData {
        public final List<String> subCategories;
        public final List<NodeDefinition> directNodes;

        public CategoryData(List<String> subCategories, List<NodeDefinition> directNodes) {
            this.subCategories = subCategories;
            this.directNodes = directNodes;
        }
    }

    public static CategoryData getCategoryData(String currentPath) {
        List<String> subCategories = new ArrayList<>();
        List<NodeDefinition> directNodes = new ArrayList<>();
        
        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
            if (def.category().equals(currentPath)) {
                directNodes.add(def);
            } else if (def.category().startsWith(currentPath + ".")) {
                String sub = def.category().substring(currentPath.length() + 1);
                int dot = sub.indexOf('.');
                String immediateSub = dot == -1 ? sub : sub.substring(0, dot);
                String fullSubPath = currentPath + "." + immediateSub;
                if (!subCategories.contains(fullSubPath)) {
                    subCategories.add(fullSubPath);
                }
            }
        }
        
        subCategories.sort((a, b) -> a.compareTo(b)); // Simple sort, localized sort can be done in UI
        directNodes.sort((a, b) -> a.name().compareTo(b.name()));
        
        return new CategoryData(subCategories, directNodes);
    }

    public static List<NodeDefinition> getNodesInCategory(String categoryPath) {
        return NodeRegistry.getAllDefinitions().stream()
                .filter(def -> def.category().startsWith(categoryPath))
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());
    }
}


