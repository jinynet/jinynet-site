package cn.jinynet.site.api.home;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开系统设置接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class PublicSettingsApi {

    private final SettingsService settingsService;

    /**
     * 获取公开的网站配置信息
     * 包括网站标题、描述、备案号、主题配置等
     *
     * @return 网站配置信息
     */
    @GetMapping("/public")
    public Result<Map<String, Object>> getPublicSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        // 网站基本信息
        settings.put("site_title", settingsService.getSettingValue("site_title"));
        settings.put("site_description", settingsService.getSettingValue("site_description"));
        settings.put("site_keywords", settingsService.getSettingValue("site_keywords"));
        settings.put("site_logo", settingsService.getSettingValue("site_logo"));
        settings.put("site_favicon", settingsService.getSettingValue("site_favicon"));
        
        // 备案信息
        settings.put("site_icp", settingsService.getSettingValue("site_icp"));
        settings.put("site_security_record", settingsService.getSettingValue("site_security_record"));
        
        // 版权信息
        settings.put("site_copyright", settingsService.getSettingValue("site_copyright"));
        
        // 主题配置（与前端 ThemeConfig 接口字段对齐，确保关闭浏览器后再打开能完整恢复）
        Map<String, Object> themeSettings = new HashMap<>();
        themeSettings.put("theme_mode", settingsService.getSettingValue("theme_mode"));
        themeSettings.put("primary_color", settingsService.getSettingValue("primary_color"));
        themeSettings.put("primary_color_hover", settingsService.getSettingValue("primary_color_hover"));
        themeSettings.put("primary_color_pressed", settingsService.getSettingValue("primary_color_pressed"));
        themeSettings.put("accent_color", settingsService.getSettingValue("accent_color"));
        themeSettings.put("success_color", settingsService.getSettingValue("success_color"));
        themeSettings.put("success_color_hover", settingsService.getSettingValue("success_color_hover"));
        themeSettings.put("success_color_pressed", settingsService.getSettingValue("success_color_pressed"));
        themeSettings.put("warning_color", settingsService.getSettingValue("warning_color"));
        themeSettings.put("warning_color_hover", settingsService.getSettingValue("warning_color_hover"));
        themeSettings.put("warning_color_pressed", settingsService.getSettingValue("warning_color_pressed"));
        themeSettings.put("error_color", settingsService.getSettingValue("error_color"));
        themeSettings.put("error_color_hover", settingsService.getSettingValue("error_color_hover"));
        themeSettings.put("error_color_pressed", settingsService.getSettingValue("error_color_pressed"));
        themeSettings.put("info_color", settingsService.getSettingValue("info_color"));
        themeSettings.put("info_color_hover", settingsService.getSettingValue("info_color_hover"));
        themeSettings.put("info_color_pressed", settingsService.getSettingValue("info_color_pressed"));
        themeSettings.put("font_family", settingsService.getSettingValue("font_family"));
        themeSettings.put("font_size", settingsService.getSettingValue("font_size"));
        themeSettings.put("layout_mode", settingsService.getSettingValue("layout_mode"));
        themeSettings.put("animation_enabled", settingsService.getSettingValue("animation_enabled"));
        themeSettings.put("border_radius", settingsService.getSettingValue("border_radius"));
        settings.put("theme", themeSettings);
        
        return Result.success(settings);
    }
}
