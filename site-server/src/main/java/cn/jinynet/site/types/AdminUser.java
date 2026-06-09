package cn.jinynet.site.types;

import cn.jinynet.starter.rbac.types.SysUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 系统管理员
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Accessors(fluent = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser implements SysUser {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    @Override
    public Object id() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
//
//    @Override
//    public String username() {
//        return username;
//    }
//
//    public String password() {
//        return password;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
