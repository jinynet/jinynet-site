package cn.jinynet.site.api.home;

import cn.jinynet.site.cache.ArticleCache;
import cn.jinynet.site.service.ArticleViewService;
import cn.jinynet.site.service.FullTextSearchService;
import cn.jinynet.site.entity.*;
import cn.jinynet.site.entity.dto.ArticleList;
import cn.jinynet.site.entity.dto.ArticleSpecification;
import cn.jinynet.site.utils.HttpUtils;
import cn.jinynet.starter.common.types.request.PageRequest;
import cn.jinynet.starter.common.types.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 已发布文章接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class PostedArticleApi {

    private final JSqlClient sqlClient;
    private final ArticleViewService articleViewService;
    private final FullTextSearchService fullTextSearchService;
    private final ArticleCache articleCache;

    /**
     * 获取已发布文章列表 - 支持分页、搜索
     *
     * @param sc 搜索条件
     * @param pq      分页参数
     * @return 已发布章列表
     */
    @GetMapping
    public Result<Page<Article>> getPostedArticles(ArticleSpecification  sc, PageRequest pq) {
        List<Order> orders = Order.makeOrders(ArticleTable.$, "publishedAt desc, createdAt desc");
        Page<Article> articlePage = sqlClient.createQuery(ArticleTable.$)
                .where(sc)
                .where(ArticleTable.$.status().eq("published"))
                .orderBy(orders)
                .select(ArticleTable.$.fetch(ArticleList.METADATA.getFetcher()))
                .fetchPage(pq.getPageIndex(), pq.getPageSize());
        return Result.success(articlePage);
    }

    /**
     * 通过 ID 获取文章详情
     *
     * @param id      文章ID
     * @param request HTTP请求对象（用于获取客户端IP）
     * @return 文章详情
     */
    @GetMapping("/id/{id}")
    public Result<Article> getArticleById(@PathVariable long id, HttpServletRequest request) {
        log.debug("获取文章详情，ID: {}", id);

        Article article = articleCache.getArticleById(id);

        if (article != null) {
            String clientIp = HttpUtils.getClientIp(request);
            articleViewService.recordView(id, clientIp);
        }

        return Result.success(article);
    }

    /**
     * 通过 slug 获取文章详情
     *
     * @param slug    文章别名
     * @param request HTTP请求对象（用于获取客户端IP）
     * @return 文章详情
     */
    @GetMapping("/{slug}")
    public Result<Article> getArticleBySlug(@PathVariable String slug, HttpServletRequest request) {
        try {
            long id = Long.parseLong(slug);
            return getArticleById(id, request);
        } catch (NumberFormatException e) {
            Article article = articleCache.getArticleBySlug(slug);

            if (article != null) {
                String clientIp = HttpUtils.getClientIp(request);
                articleViewService.recordView(article.id(), clientIp);
            }

            return Result.success(article);
        }
    }

    /**
     * 获取文章分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<ArticleCategory>> getCategories() {
        List<ArticleCategory> categories = sqlClient.createQuery(ArticleCategoryTable.$)
                .orderBy(ArticleCategoryTable.$.sortOrder().asc())
                .select(ArticleCategoryTable.$)
                .execute();
        return Result.success(categories);
    }

    /**
     * 获取文章标签列表
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    public Result<List<ArticleTag>> getTags() {
        List<ArticleTag> tags = sqlClient.createQuery(ArticleTagTable.$)
                .orderBy(ArticleTagTable.$.sortOrder().asc())
                .select(ArticleTagTable.$)
                .execute();
        return Result.success(tags);
    }

    /**
     * 获取热门文章（按阅读量）
     *
     * @param limit 数量，默认 5
     * @return 热门文章列表
     */
    @GetMapping("/hot")
    public Result<List<Article>> getHotArticles(@RequestParam(defaultValue = "5") int limit) {
        List<Article> articles = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.status().eq("published"))
                .orderBy(ArticleTable.$.viewCount().desc())
                .select(ArticleTable.$.fetch(ArticleList.METADATA.getFetcher()))
                .limit(limit)
                .execute();
        return Result.success(articles);
    }

    /**
     * 获取最新文章
     *
     * @param limit 数量，默认 3
     * @return 最新文章列表
     */
    @GetMapping("/latest")
    public Result<List<Article>> getLatestArticles(@RequestParam(defaultValue = "3") int limit) {
        List<Article> articles = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.status().eq("published"))
                .orderBy(ArticleTable.$.publishedAt().desc(), ArticleTable.$.createdAt().desc())
                .select(ArticleTable.$.fetch(ArticleList.METADATA.getFetcher()))
                .limit(limit)
                .execute();
        return Result.success(articles);
    }

    /**
     * 全文搜索文章
     *
     * @param keyword 搜索关键词
     * @param limit   返回数量，默认 10
     * @return 搜索结果列表
     */
    @GetMapping("/search")
    public Result<List<FullTextSearchService.UnifiedSearchResult>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<FullTextSearchService.UnifiedSearchResult> results = fullTextSearchService.searchArticles(keyword, limit);
        return Result.success(results);
    }
}
