package cn.jinynet.site.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 密码错误限制缓存
 * <p>
 * 时间间隔 {@link PasswdErrLimitCache#COUNT_INTERVAL} ms内，密码连续错误{@link PasswdErrLimitCache#MAX_COUNT}次，则限制
 * {@link PasswdErrLimitCache#LIMIT_EXPIRE} ms时间内不允许进行再次认证
 *
 * @author jinty
 */
@Component
@RequiredArgsConstructor
public class PasswdErrLimitCache {

    private final RedisTemplate<String, PasswdErrLimit> redisTemplate;

    private static final String KEY_SPLIT = ":";

    /**
     * 间隔时间： 10 分钟
     */
    public static final long COUNT_INTERVAL = 1000L * 60 * 10;

    /**
     * 默认启动限制的密码错误最大次数： 3
     */
    public static final int MAX_COUNT = 3;


    /**
     * 默认限制过期时间(毫秒)： 3分钟
     */
    public static final long LIMIT_EXPIRE = 1000L * 60 * 3;

    /**
     * 密码错误限制缓存key
     *
     * @param account 用户
     * @return key
     */
    private String key(String account) {
        return "PasswdErrLimit" + KEY_SPLIT + account;
    }

    /**
     * 获取密码错误限制
     *
     * @param account 用户
     * @return 密码错误限制
     */
    public PasswdErrLimit get(String account) {
        return redisTemplate.opsForValue().get(key(account));
    }

    /**
     * 获取密码错误限制
     *
     * @param account 用户
     * @return true - 存在限制且限制未过期
     */
    public boolean existsLimit(String account) {
        return limitLeftTime(account) > 0;
    }

    /**
     * 获取密码错误限制剩余时间
     *
     * @param account 用户
     * @return 剩余时间（毫秒）
     */
    public long limitLeftTime(String account) {
        PasswdErrLimit passwdErrLimit = get(account);
        if (passwdErrLimit == null) {
            return -1;
        }
        if (passwdErrLimit.getCount() < MAX_COUNT) {
            return -1;
        }
        return passwdErrLimit.getExpire() - System.currentTimeMillis();
    }

    /**
     * 移除密码错误限制
     *
     * @param account 用户
     */
    public void remove(String account) {
        redisTemplate.delete(key(account));
    }

    /**
     * 密码错误次数加1
     * @param account 用户
     */
    public int errCount(String account) {
        // 是否达到最大错误次数
        boolean maxErrCount = false;
        // 获取密码错误限制
        PasswdErrLimit passwdErrLimit = get(account);
        if (passwdErrLimit == null) {   // 首次错误
            passwdErrLimit = new PasswdErrLimit();
            passwdErrLimit.setAccount(account);
            passwdErrLimit.setCount(1);
        } else {  // 错误次数加1
            passwdErrLimit.setCount(passwdErrLimit.getCount() + 1);
        }

        // 是否达到最大错误次数
        if (passwdErrLimit.getCount() >= MAX_COUNT) {
            maxErrCount = true;
            passwdErrLimit.setExpire(System.currentTimeMillis() + LIMIT_EXPIRE);
        } else {
            passwdErrLimit.setExpire(System.currentTimeMillis() + COUNT_INTERVAL);
        }
        redisTemplate.opsForValue().set(key(account), passwdErrLimit, maxErrCount ? LIMIT_EXPIRE : COUNT_INTERVAL, TimeUnit.MILLISECONDS);
        return passwdErrLimit.getCount();
    }

}
