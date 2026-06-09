package cn.jinynet.site.types.enums;

import org.babyfish.jimmer.sql.EnumType;

/**
 * 文章状态枚举
 *
 * @author jinty
 * @since 1.0
 */
@EnumType(EnumType.Strategy.NAME)
public enum ArticleStatus {
    DRAFT,
    PUBLISHED,
    DELETED;
}
