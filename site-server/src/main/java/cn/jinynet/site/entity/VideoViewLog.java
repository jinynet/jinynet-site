package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 视频播放记录表
 */
@Entity
@Table(name = "video_view_log")
public interface VideoViewLog extends EntityId, BaseEntity {

    /**
     * 视频ID
     */
    @Column(name = "video_id")
    long videoId();

    /**
     * 用户ID（可为空）
     */
    @Column(name = "user_id")
    @Nullable
    Long userId();

    /**
     * IP地址
     */
    @Column(name = "ip_address")
    @Nullable
    String ipAddress();

    /**
     * User-Agent
     */
    @Column(name = "user_agent")
    @Nullable
    String userAgent();

    /**
     * 播放时间
     */
    @Column(name = "viewed_at")
    LocalDateTime viewedAt();
}
