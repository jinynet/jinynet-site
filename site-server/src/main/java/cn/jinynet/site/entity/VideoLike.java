package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;

import java.time.LocalDateTime;

/**
 * 视频点赞表
 */
@Entity
@Table(name = "video_like")
public interface VideoLike extends EntityId, BaseEntity {

    /**
     * 视频ID
     */
    @Column(name = "video_id")
    long videoId();

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    long userId();

    /**
     * 点赞时间
     */
    @Column(name = "liked_at")
    LocalDateTime likedAt();
}
