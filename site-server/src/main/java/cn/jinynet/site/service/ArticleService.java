package cn.jinynet.site.service;

import cn.jinynet.site.cache.ArticleCache;
import cn.jinynet.site.entity.Article;
import cn.jinynet.site.entity.ArticleDraft;
import cn.jinynet.site.entity.ArticleProps;
import cn.jinynet.site.entity.dto.ArticleDetail;
import cn.jinynet.site.entity.dto.ArticleForm;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.babyfish.jimmer.sql.ast.mutation.DeleteResult;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 文章服务
 *
 * @author jinty
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final JSqlClient sqlClient;
    private final ArticleSearchService articleSearchService;
    private final ArticleCache articleCache;

    @Transactional(rollbackFor = Exception.class)
    public Article createArticle(ArticleForm articleForm) {
        Article newArticle = ArticleDraft.$.produce(articleForm.toEntity(), draft -> {
            draft.setStatus("draft");
            draft.setViewCount(0);
            draft.setLikeCount(0);
        });
        Article savedArticle = sqlClient.saveCommand(newArticle)
                .setMode(SaveMode.INSERT_ONLY)
                .setAssociatedMode(ArticleProps.CATEGORY, AssociatedSaveMode.APPEND_IF_ABSENT)
                .setAssociatedMode(ArticleProps.TAGS, AssociatedSaveMode.APPEND_IF_ABSENT)
                .setKeyOnlyAsReference(ArticleProps.TAGS, true)
                .execute(ArticleDetail.METADATA.getFetcher())
                .getModifiedEntity();

        articleSearchService.indexArticle(savedArticle);
        return savedArticle;
    }

    @Transactional(rollbackFor = Exception.class)
    public Article updateArticle(long id, ArticleForm articleForm) {
        Article article = ArticleDraft.$.produce(articleForm.toEntityById(id), draft -> {

        });
        Article updatedArticle = sqlClient.saveCommand(article)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute(ArticleDetail.METADATA.getFetcher())
                .getModifiedEntity();

        articleSearchService.indexArticle(updatedArticle);
        if ("published".equals(updatedArticle.status())) {
            articleCache.evictArticleById(id);
            articleCache.evictArticleBySlug(updatedArticle.slug());
        }
        return updatedArticle;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticle(long id) {
        Article existingArticle = sqlClient.findById(Article.class, id);
        DeleteResult deleteResult = sqlClient.deleteById(Article.class, id);
        boolean deleted = deleteResult.getAffectedRowCount(Article.class) > 0;
        if (deleted) {
            articleSearchService.deleteArticleFromIndex(id);
            if (existingArticle != null) {
                articleCache.evictArticleById(id);
                articleCache.evictArticleBySlug(existingArticle.slug());
            }
        }
        return deleted;
    }

    @Transactional(rollbackFor = Exception.class)
    public Article publishArticle(ArticleForm articleForm) {
        Article newArticle = ArticleDraft.$.produce(articleForm.toEntity(), draft -> {
            draft.setStatus("published");
            draft.setViewCount(0);
            draft.setLikeCount(0);
            draft.setPublishedAt(LocalDateTime.now());
        });
        Article modifiedEntity = sqlClient.saveCommand(newArticle)
                .setMode(SaveMode.INSERT_ONLY)
                .setAssociatedMode(ArticleProps.CATEGORY, AssociatedSaveMode.APPEND_IF_ABSENT)
                .setAssociatedMode(ArticleProps.TAGS, AssociatedSaveMode.APPEND_IF_ABSENT)
                .setKeyOnlyAsReference(ArticleProps.TAGS, true)
                .execute(ArticleDetail.METADATA.getFetcher())
                .getModifiedEntity();
        articleSearchService.indexArticle(modifiedEntity);
        articleCache.evictArticleById(modifiedEntity.id());
        articleCache.evictArticleBySlug(modifiedEntity.slug());
        return modifiedEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    public Article updateAndPublishArticle(long id, ArticleForm articleForm) {
        Article article = ArticleDraft.$.produce(articleForm.toEntityById(id), draft -> {
            draft.setStatus("published");
            draft.setPublishedAt(LocalDateTime.now());
        });
        Article modifiedEntity = sqlClient.saveCommand(article)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute(ArticleDetail.METADATA.getFetcher())
                .getModifiedEntity();
        articleSearchService.indexArticle(modifiedEntity);
        articleCache.evictArticleById(id);
        articleCache.evictArticleBySlug(modifiedEntity.slug());
        return modifiedEntity;
    }
}