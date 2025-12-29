package ltd.opens.mg.mc.core.blueprint.engine;

import java.util.UUID;

/**
 * 转换引擎 - 负责蓝图系统中各种数据类型的安全转换
 */
public class TypeConverter {

    public static String cast(String value, String targetType) {
        if (value == null) return "";
        
        targetType = targetType.toUpperCase();
        
        switch (targetType) {
            case "STRING":
                return value;
                
            case "FLOAT":
                try {
                    // 处理可能的科学计数法或非标准格式
                    return String.valueOf(Double.parseDouble(value));
                } catch (Exception e) {
                    return "0.0";
                }
                
            case "BOOLEAN":
                if (value.equalsIgnoreCase("true") || value.equals("1")) return "true";
                if (value.equalsIgnoreCase("false") || value.equals("0")) return "false";
                return String.valueOf(!value.isEmpty() && !value.equalsIgnoreCase("null"));
                
            case "UUID":
                try {
                    return UUID.fromString(value).toString();
                } catch (Exception e) {
                    return "";
                }
                
            case "INT":
                try {
                    return String.valueOf((int) Double.parseDouble(value));
                } catch (Exception e) {
                    return "0";
                }

            case "LIST":
                // 确保符合列表格式（用 | 分隔）
                return value;

            default:
                return value;
        }
    }
}
