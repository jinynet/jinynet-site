package cn.jinynet.site.api.admin;

import cn.jinynet.site.entity.UserInfo;
import cn.jinynet.site.entity.UserInfoTable;
import cn.jinynet.site.service.AuthService;
import cn.jinynet.site.service.SettingsService;
import cn.jinynet.site.types.AdminUser;
import cn.jinynet.site.types.request.auth.ChangePasswordRequest;
import cn.jinynet.site.types.request.auth.PwdLoginRequest;
import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.starter.rbac.types.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AdminAuthApi {

    private final JSqlClient sqlClient;
    private final SettingsService settingsService;
    private final AuthService authService;


    /**
     * 获取验证码配置
     *
     * @return 验证码配置
     */
    @GetMapping("/captcha-config")
    public Result<Map<String, Object>> getCaptchaConfig() {
        return Result.success(Map.of("enabled", settingsService.isEnabledCaptcha()));
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（包含username、password，可选captchaToken）
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody PwdLoginRequest loginRequest) {
        TokenUser<AdminUser> tokenUser = authService.login(loginRequest);
        return Result.success("登录成功", tokenUser.getAccessToken());
    }

    /**
     * 用户退出登录
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<UserInfo> getProfile() {
        UserInfo userInfo = sqlClient.createQuery(UserInfoTable.$)
                .select(UserInfoTable.$)
                .fetchOptional()
                .orElse(null);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     *
     * @param changePasswordRequest 密码请求（包含旧密码和新密码，可选验证码token）
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(changePasswordRequest);
        return Result.success();
    }
}