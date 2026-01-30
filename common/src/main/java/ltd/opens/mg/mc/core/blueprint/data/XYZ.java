package ltd.opens.mg.mc.core.blueprint.data;


/**
 * 代表一个三维向量或坐标，存储三个浮点数。
 */
public record XYZ(double x, double y, double z) {
    public static final XYZ ZERO = new XYZ(0, 0, 0);

    @Override
    public String toString() {
        return String.format("%.2f, %.2f, %.2f", x, y, z);
    }

    /**
     * 从字符串解析 XYZ，格式支持 "x, y, z" 或 "x y z"
     */
    public static XYZ fromString(String s) {
        if (s == null || s.isEmpty()) return ZERO;
        try {
            String[] parts = s.split("[,\\s]+");
            if (parts.length >= 3) {
                return new XYZ(
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim())
                );
            }
        } catch (Exception ignored) {}
        return ZERO;
    }
}
