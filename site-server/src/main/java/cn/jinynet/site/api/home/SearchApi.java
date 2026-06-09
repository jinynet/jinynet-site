package cn.jinynet.site.api.home;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.FullTextSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全文搜索接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchApi {

    private final FullTextSearchService fullTextSearchService;

    /**
     * 全局搜索（文章+项目）
     *
     * @param keyword 搜索关键词
     * @param limit   返回数量，默认 20
     * @return 搜索结果列表
     */
    @GetMapping
    public Result<List<FullTextSearchService.UnifiedSearchResult>> searchAll(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Global search with keyword: {}", keyword);
        List<FullTextSearchService.UnifiedSearchResult> results = fullTextSearchService.searchAll(keyword, limit);
        return Result.success(results);
    }

    /**
     * 重建所有搜索索引
     *
     * @return 操作结果
     */
    @PostMapping(value = "/rebuild-index")
    public Result<Void> rebuildIndex() {
        log.info("Rebuilding all search indexes...");
        fullTextSearchService.rebuildAllIndexes();
        return Result.success(null);
    }

    /**
     * 重建文章索引
     *
     * @return 操作结果
     */
    @PostMapping(value = "/rebuild-article-index")
    public Result<Void> rebuildArticleIndex() {
        log.info("Rebuilding article search index...");
        fullTextSearchService.rebuildArticleIndex();
        return Result.success(null);
    }

    /**
     * 重建项目索引
     *
     * @return 操作结果
     */
    @PostMapping(value = "/rebuild-project-index")
    public Result<Void> rebuildProjectIndex() {
        log.info("Rebuilding project search index...");
        fullTextSearchService.rebuildProjectIndex();
        return Result.success(null);
    }
}