package ltd.opens.mg.mc.core.blueprint.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 转换引擎 - 负责蓝图系统中各种数据类型的安全转换
 */
public class TypeConverter {

    public static String toString(Object value) {
        if (value == null) return "";
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(TypeConverter::toString)
                    .collect(Collectors.joining("|"));
        }
        return String.valueOf(value);
    }

    public static double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof Boolean) return (Boolean) value ? 1.0 : 0.0;
        try {
            String s = String.valueOf(value);
            if (s.isEmpty()) return 0.0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        String s = String.valueOf(value).toLowerCase();
        return s.equals("true") || s.equals("1") || s.equals("yes");
    }

    public static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return (int) Math.round(toDouble(value));
    }

    public static List<Object> toList(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) {
            //noinspection unchecked
            return (List<Object>) value;
        }
        
        String s = String.valueOf(value);
        if (s.isEmpty()) return new ArrayList<>();
        
        List<Object> list = new ArrayList<>();
        if (s.contains("|")) {
            String[] parts = s.split("\\|");
            for (String part : parts) {
                // 简单的去引号处理，用于兼容旧的 item.contains("|") 逻辑
                if (part.startsWith("\"") && part.endsWith("\"") && part.length() >= 2) {
                    list.add(part.substring(1, part.length() - 1));
                } else {
                    list.add(part);
                }
            }
        } else {
            list.add(s);
        }
        return list;
    }

    public static Object cast(Object value, String targetType) {
        if (value == null) return null;
        if (targetType == null) return value;
        
        targetType = targetType.toUpperCase();
        
        switch (targetType) {
            case "STRING":
                return toString(value);
                
            case "FLOAT":
                return toDouble(value);
                
            case "BOOLEAN":
                return toBoolean(value);
                
            case "INT":
                return toInt(value);
                
            case "UUID":
                try {
                    String s = toString(value);
                    if (s.isEmpty()) return "";
                    return UUID.fromString(s).toString();
                } catch (Exception e) {
                    return "";
                }

            case "LIST":
                return toList(value);

            default:
                return value;
        }
    }
}
