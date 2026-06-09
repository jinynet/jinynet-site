package cn.jinynet.site.cache;

import cn.jinynet.site.entity.Article;
import cn.jinynet.site.entity.ArticleTable;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCache {

    private final JSqlClient sqlClient;
    private final CacheManager cacheManager;

    private Cache<String, Article> articleCache;

    @PostConstruct
    public void init() {
        QuickConfig qc = QuickConfig.newBuilder("articleCache")
                .expire(Duration.ofMinutes(30))
                .cacheType(CacheType.REMOTE)
                .build();
        articleCache = cacheManager.getOrCreateCache(qc);
        log.info("文章缓存初始化完成");
    }

    public Article getArticleById(long articleId) {
        String key = "article:id:" + articleId;
        Article cached = articleCache.get(key);
        if (cached != null) {
            log.debug("从缓存获取文章，ID: {}", articleId);
            return cached;
        }

        Article article = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.id().eq(articleId))
                .where(ArticleTable.$.status().eq("published"))
                .select(ArticleTable.$)
                .fetchOptional()
                .orElse(null);

        if (article != null) {
            articleCache.put(key, article, 30, TimeUnit.MINUTES);
            log.debug("文章已缓存，ID: {}", articleId);
        }
        return article;
    }

    public Article getArticleBySlug(String slug) {
        String key = "article:slug:" + slug;
        Article cached = articleCache.get(key);
        if (cached != null) {
            log.debug("从缓存获取文章，slug: {}", slug);
            return cached;
        }

        Article article = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.slug().eq(slug))
                .where(ArticleTable.$.status().eq("published"))
                .select(ArticleTable.$)
                .fetchOptional()
                .orElse(null);

        if (article != null) {
            articleCache.put(key, article, 30, TimeUnit.MINUTES);
            log.debug("文章已缓存，slug: {}", slug);
        }
        return article;
    }

    public void evictArticleById(long articleId) {
        String key = "article:id:" + articleId;
        articleCache.remove(key);
        log.debug("文章缓存已清除，ID: {}", articleId);
    }

    public void evictArticleBySlug(String slug) {
        String key = "article:slug:" + slug;
        articleCache.remove(key);
        log.debug("文章缓存已清除，slug: {}", slug);
    }

    public void evictAllArticleCache() {
        articleCache.close();
        log.info("所有文章缓存已清除");
    }
}