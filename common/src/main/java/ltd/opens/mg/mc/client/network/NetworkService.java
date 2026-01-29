package ltd.opens.mg.mc.client.network;

import ltd.opens.mg.mc.network.payloads.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 独立网络服务层，解耦 UI 与网络协议。
 * UI 应调用此处的业务接口，而非直接构造和发送网络包。
 */
public class NetworkService {
    private static final NetworkService INSTANCE = new NetworkService();

    private NetworkService() {}

    public static NetworkService getInstance() {
        return INSTANCE;
    }

    /**
     * 保存蓝图数据
     */
    public void saveBlueprint(String name, String json, long version) {
        sendPacket(new SaveBlueprintPayload(name, json, version));
    }

    /**
     * 请求蓝图列表
     */
    public void requestBlueprintList() {
        sendPacket(new RequestBlueprintListPayload());
    }

    /**
     * 删除蓝图
     */
    public void deleteBlueprint(String name) {
        sendPacket(new DeleteBlueprintPayload(name));
    }

    /**
     * 重命名蓝图
     */
    public void renameBlueprint(String oldName, String newName) {
        sendPacket(new RenameBlueprintPayload(oldName, newName));
    }

    /**
     * 复制蓝图
     */
    public void duplicateBlueprint(String sourceName, String targetName) {
        sendPacket(new DuplicateBlueprintPayload(sourceName, targetName));
    }

    /**
     * 请求蓝图具体数据
     */
    public void requestMappings() {
        sendPacket(new RequestMappingsPayload());
    }

    /**
     * 保存 ID 映射
     */
    public void saveMappings(Map<String, Set<String>> mappings) {
        sendPacket(new SaveMappingsPayload(new HashMap<>(mappings)));
    }

    /**
     * 请求蓝图具体数据
     */
    public void requestBlueprintData(String name) {
        sendPacket(new RequestBlueprintDataPayload(name));
    }

    /**
     * 发送蓝图工作台动作
     */
    public void sendWorkbenchAction(WorkbenchActionPayload.Action action, String path) {
        sendPacket(new WorkbenchActionPayload(action, path));
    }

    /**
     * 请求导出蓝图
     */
    public void requestExport(String name) {
        sendPacket(new RequestExportPayload(name));
    }

    /**
     * 导入蓝图
     */
    public void importBlueprint(String name, String data, Map<String, Set<String>> mappings) {
        sendPacket(new ImportBlueprintPayload(name, data, mappings));
    }

    /**
     * 统一发送自定义载荷数据包
     */
    private void sendPacket(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(payload));
        }
    }
}
