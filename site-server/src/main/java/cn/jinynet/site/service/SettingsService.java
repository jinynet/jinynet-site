package cn.jinynet.site.service;

import cn.jinynet.site.types.AdminUser;
import cn.jinynet.site.entity.SystemSettings;
import cn.jinynet.site.entity.SystemSettingsDraft;
import cn.jinynet.site.entity.SystemSettingsTable;
import cn.jinynet.starter.crypto.utils.BCryptUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务
 *
 * @author jinty
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SettingsService {
    private final JSqlClient sqlClient;

    /**
     * 获取所有系统设置
     *
     * @return 系统设置列表
     */
    public List<SystemSettings> getAllSettings() {
        return sqlClient.createQuery(SystemSettingsTable.$)
                .select(SystemSettingsTable.$)
                .execute();
    }

    /**
     * 根据键获取设置值
     *
     * @param key 设置键
     * @return 设置值
     */
    public String getSettingValue(String key) {
        return sqlClient.createQuery(SystemSettingsTable.$)
                .where(SystemSettingsTable.$.settingKey().eq(key))
                .select(SystemSettingsTable.$.settingValue())
                .fetchOptional()
                .orElse(null);
    }

    /**
     * 更新设置
     *
     * @param key   设置键
     * @param value 设置值
     * @return 更新后的设置
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemSettings updateSetting(String key, String value, String category) {
        // 如果是 admin_password，需要进行加密存储
        if ("admin_password".equals(key) && value != null && !value.isEmpty()) {
            value = BCryptUtils.encrypt(value);
        }
        SystemSettings existing = sqlClient.createQuery(SystemSettingsTable.$)
                .where(SystemSettingsTable.$.settingKey().eq(key))
                .select(SystemSettingsTable.$)
                .fetchOptional()
                .orElse(null);

        String finalValue = value;
        SystemSettings settings = SystemSettingsDraft.$.produce(draft -> {
            if (existing != null) {
                draft.setId(existing.id());
            }
            draft.setSettingKey(key);
            draft.setSettingValue(finalValue);
            if (existing != null) {
                draft.setSettingType(existing.settingType());
                draft.setDescription(existing.description());
                draft.setCategory(existing.category());
            } else {
                draft.setSettingType("string");
                draft.setDescription("");
                draft.setCategory(StringUtils.isBlank(category) ? "other" : category);
            }
        });
        return sqlClient.saveCommand(settings)
                .setMode(SaveMode.UPSERT)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 批量更新设置（带分类）
     *
     * @param settingsMap 设置键值对，value为包含value和category的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSettingsWithCategory(Map<String, Map<String, String>> settingsMap) {
        settingsMap.forEach((key, valueMap) -> {
            String value = valueMap.get("value");
            String category = valueMap.get("category");
            updateSetting(key, value, category);
        });
    }

    /**
     * 获取是否启用验证码
     *
     * @return 是否启用验证码
     */
    public boolean isEnabledCaptcha() {
        return "true".equals(getSettingValue("enable_captcha"));
    }

    /**
     * 获取管理员用户信息
     *
     * @return 管理员用户信息
     */
    public AdminUser getAdminUser(String account) {
        String adminUsername = getSettingValue("admin_username");
        if (adminUsername == null || !adminUsername.equals(account)) {
            return null;
        }
        return AdminUser.builder()
                .id(1L)
                .username(adminUsername)
                .password(getSettingValue("admin_password"))
                .build();
    }
}