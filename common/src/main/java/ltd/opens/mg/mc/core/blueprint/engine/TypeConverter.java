package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 转换引擎 - 负责蓝图系统中各种数据类型的安全转换
 */
public class TypeConverter {

    public static String toString(Object value) {
        if (value == null) return "";
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

    public static XYZ toXYZ(Object value) {
        if (value == null) return XYZ.ZERO;
        if (value instanceof XYZ xyz) return xyz;
        return XYZ.fromString(toString(value));
    }

    public static List<Object> toList(Object value) {
        if (value == null) return new ArrayList<>();
        
        // 1. 如果已经是 List，确保返回可变列表
        if (value instanceof List<?> listValue) {
            return new ArrayList<>(listValue);
        }

        // 2. 处理数组
        if (value instanceof Object[] array) {
            List<Object> list = new ArrayList<>();
            for (Object o : array) {
                list.add(o);
            }
            return list;
        }
        if (value instanceof int[] array) {
            List<Object> list = new ArrayList<>();
            for (int i : array) list.add(i);
            return list;
        }
        if (value instanceof double[] array) {
            List<Object> list = new ArrayList<>();
            for (double d : array) list.add(d);
            return list;
        }
        if (value instanceof boolean[] array) {
            List<Object> list = new ArrayList<>();
            for (boolean b : array) list.add(b);
            return list;
        }
        if (value instanceof long[] array) {
            List<Object> list = new ArrayList<>();
            for (long l : array) list.add(l);
            return list;
        }

        // 3. 处理 JsonArray (Gson)
        if (value instanceof JsonArray array) {
            List<Object> list = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    var primitive = element.getAsJsonPrimitive();
                    if (primitive.isString()) list.add(primitive.getAsString());
                    else if (primitive.isNumber()) list.add(primitive.getAsNumber());
                    else if (primitive.isBoolean()) list.add(primitive.getAsBoolean());
                } else {
                    list.add(element);
                }
            }
            return list;
        }
        
        // 3. 处理字符串
        if (value instanceof String s) {
            if (s.isEmpty()) return new ArrayList<>();
            List<Object> list = new ArrayList<>();
            list.add(s);
            return list;
        }
        
        // 4. 其他任何对象，直接作为列表的唯一元素返回（关键修复：保留原始对象）
        List<Object> list = new ArrayList<>();
        list.add(value);
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
                
            case "XYZ":
                return toXYZ(value);
                
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
