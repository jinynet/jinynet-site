package cn.jinynet.site.config.jimmer.translator;

import cn.jinynet.site.entity.ArticleCategoryProps;
import cn.jinynet.site.entity.ArticleProps;
import cn.jinynet.site.entity.ArticleTagProps;
import cn.jinynet.site.entity.ProjectStackProps;
import cn.jinynet.starter.common.types.exception.BaseBizCode;
import cn.jinynet.starter.common.types.exception.BizException;
import org.babyfish.jimmer.sql.exception.SaveException;
import org.babyfish.jimmer.sql.runtime.ExceptionTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * 违法唯一约束异常转换
 */
@Component
public class NotUniqueExceptionTranslator implements ExceptionTranslator<SaveException.NotUnique> {
    @Override
    public @Nullable Exception translate(@NotNull SaveException.NotUnique exception, @Nullable Args args) {
        BizException.throwIf(exception.isMatched(ArticleProps.SLUG), BaseBizCode.DATA_EXIST, "文章别名已存在");
        BizException.throwIf(exception.isMatched(ArticleCategoryProps.SLUG), BaseBizCode.DATA_EXIST, "分类别名已存在");
        BizException.throwIf(exception.isMatched(ArticleTagProps.SLUG), BaseBizCode.DATA_EXIST, "标签别名已存在");
        BizException.throwIf(exception.isMatched(ProjectStackProps.NAME), BaseBizCode.DATA_EXIST, "技术栈名称已存在");
        return null;
    }
}
