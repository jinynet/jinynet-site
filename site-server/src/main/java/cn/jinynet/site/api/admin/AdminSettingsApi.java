package cn.jinynet.site.api.admin;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.SettingsService;
import cn.jinynet.site.entity.SystemSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsApi {

    private final SettingsService settingsService;

    /**
     * 获取所有系统设置
     *
     * @return 设置映射（不包含敏感信息）
     */
    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        List<SystemSettings> settingsList = settingsService.getAllSettings();
        Map<String, Object> settingsMap = new HashMap<>();
        for (SystemSettings setting : settingsList) {
            String key = setting.settingKey();
            // 过滤敏感信息：管理员用户名和密码
            if (!"admin_username".equals(key) && !"admin_password".equals(key)) {
                settingsMap.put(key, setting.settingValue());
            }
        }
        return Result.success(settingsMap);
    }

    /**
     * 更新系统设置
     *
     * @param settingsMap 设置键值对，value为包含value和category的对象
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateSettings(@RequestBody Map<String, Map<String, String>> settingsMap) {
        settingsService.updateSettingsWithCategory(settingsMap);
        return Result.success(true);
    }

    /**
     * 获取单个设置值
     *
     * @param key 设置键
     * @return 设置值
     */
    @GetMapping("/{key}")
    public Result<String> getSetting(@PathVariable String key) {
        return Result.success("操作成功", settingsService.getSettingValue(key));
    }
}