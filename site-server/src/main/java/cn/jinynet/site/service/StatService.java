package cn.jinynet.site.service;

import cn.jinynet.site.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 统计服务
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {
    private final JSqlClient sqlClient;
    private static final ExecutorService STATS_EXECUTOR = Executors.newFixedThreadPool(
            4,
            r -> {
                Thread thread = new Thread(r, "stats-thread");
                thread.setDaemon(true);
                return thread;
            }
    );
    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 文章数量
        CompletableFuture<Long> articleCountFuture = CompletableFuture.supplyAsync(
                () -> sqlClient.createQuery(ArticleTable.$)
                        .select(ArticleTable.$.count())
                        .fetchOne(),
                STATS_EXECUTOR);
        // 分类数量
        CompletableFuture<Long> categoryCountFuture = CompletableFuture.supplyAsync(
                () -> sqlClient.createQuery(ArticleCategoryTable.$)
                        .select(ArticleCategoryTable.$.count())
                        .fetchOne(),
                STATS_EXECUTOR
        );
        // 项目数量
        CompletableFuture<Long> projectCountFuture = CompletableFuture.supplyAsync(
                () -> sqlClient.createQuery(ProjectTable.$)
                        .select(ProjectTable.$.count())
                        .fetchOne(),
                STATS_EXECUTOR
        );
        // 总阅读量
        CompletableFuture<Integer> totalViewsFuture = CompletableFuture.supplyAsync(
                () -> sqlClient.createQuery(ArticleTable.$)
                        .select(ArticleTable.$.viewCount().sum())
                        .fetchOne(),
                STATS_EXECUTOR
        );
        try {
            CompletableFuture.allOf(articleCountFuture, categoryCountFuture, projectCountFuture, totalViewsFuture).join();
            stats.put("articles", articleCountFuture.get());
            stats.put("categories", categoryCountFuture.get());
            stats.put("projects", projectCountFuture.get());
            stats.put("views", totalViewsFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get dashboard stats", e);
        }
        log.info("Dashboard stats: {}", stats);
        return stats;
    }

    /**
     * 获取最新文章
     */
    public List<Article> getLatestArticles(int limit) {
        return sqlClient.createQuery(ArticleTable.$)
                .orderBy(ArticleTable.$.updatedAt().desc())
                .select(ArticleTable.$.fetch(ArticleFetcher.$
                        .allScalarFields()
                        .category(ArticleCategoryFetcher.$.allScalarFields())))
                .fetchSlice(limit, 0)
                .getRows();
    }

    /**
     * 获取热门文章
     */
    public List<Article> getHotArticles(int limit) {
        return sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.status().eq("published"))
                .orderBy(ArticleTable.$.viewCount().desc())
                .select(ArticleTable.$)
                .fetchSlice(limit, 0)
                .getRows();
    }
}
