package cn.jinynet.site.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码错误限制
 *
 * @author jinty
 */
@Data
public class PasswdErrLimit implements Serializable {
    /**
     * 用户
     */
    private String account;
    /**
     * 错误次数
     */
    private int count;
    /**
     * 过期时间， -1表示为到达开启密码错误限制条件
     */
    private long expire;
}
