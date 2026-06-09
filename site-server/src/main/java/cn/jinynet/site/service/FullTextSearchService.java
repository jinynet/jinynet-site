package cn.jinynet.site.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullTextSearchService {
    private final ArticleSearchService articleSearchService;
    private final ProjectSearchService projectSearchService;

    /**
     * 全文搜索
     *
     * @param keyword 关键词
     * @param pageSize 页大小
     * @return 搜索结果
     */
    public List<UnifiedSearchResult> searchAll(String keyword, int pageSize) {

        /// 文章搜索
        List<UnifiedSearchResult> unifiedSearchResults = searchArticles(keyword, pageSize);
        List<UnifiedSearchResult> results = new ArrayList<>(unifiedSearchResults);
        /// 项目搜索
        unifiedSearchResults = searchProjects(keyword, pageSize);
        results.addAll(unifiedSearchResults);
        /// 排序
        results.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
        
        return results;
    }

    /**
     * 文章全文搜索
     *
     * @param keyword 关键词
     * @param pageSize 页大小
     * @return 搜索结果
     */
    public List<UnifiedSearchResult> searchArticles(String keyword, int pageSize) {
        List<UnifiedSearchResult> results = new ArrayList<>();
        
        List<ArticleSearchService.SearchResult> articleResults = articleSearchService.search(keyword, pageSize);
        for (ArticleSearchService.SearchResult articleResult : articleResults) {
            UnifiedSearchResult result = new UnifiedSearchResult();
            result.setType("article");
            result.setId(articleResult.getId());
            result.setTitle(articleResult.getTitle());
            result.setContent(articleResult.getContent());
            result.setExcerpt(articleResult.getExcerpt());
            result.setScore(articleResult.getScore());
            result.setCategoryId(articleResult.getCategoryId());
            result.setCategoryName(articleResult.getCategoryName());
            result.setTags(articleResult.getTags());
            results.add(result);
        }
        
        return results;
    }

    /**
     * 项目全文搜索
     *
     * @param keyword 关键词
     * @param pageSize 页大小
     * @return 搜索结果
     */
    public List<UnifiedSearchResult> searchProjects(String keyword, int pageSize) {
        List<UnifiedSearchResult> results = new ArrayList<>();
        
        List<ProjectSearchService.SearchResult> projectResults = projectSearchService.search(keyword, pageSize);
        for (ProjectSearchService.SearchResult projectResult : projectResults) {
            UnifiedSearchResult result = new UnifiedSearchResult();
            result.setType("project");
            result.setId(projectResult.getId());
            result.setTitle(projectResult.getName());
            result.setContent(projectResult.getContent());
            result.setDescription(projectResult.getDescription());
            result.setScore(projectResult.getScore());
            result.setStacks(projectResult.getStacks());
            results.add(result);
        }
        
        return results;
    }

    public void rebuildAllIndexes() {
        log.info("Starting to rebuild all search indexes...");
        articleSearchService.rebuildIndex();
        projectSearchService.rebuildIndex();
        log.info("All search indexes rebuilt successfully");
    }

    /**
     * 重建文章索引
     */
    public void rebuildArticleIndex() {
        log.info("Starting to rebuild article search index...");
        articleSearchService.rebuildIndex();
        log.info("Article search index rebuilt successfully");
    }

    /**
     * 重建项目索引
     */
    public void rebuildProjectIndex() {
        log.info("Starting to rebuild project search index...");
        projectSearchService.rebuildIndex();
        log.info("Project search index rebuilt successfully");
    }

    /**
     * 统一搜索结果
     */
    @Data
    public static class UnifiedSearchResult {
        /**
         * 类型
         */
        private String type;
        /**
         * ID
         */
        private Long id;
        /**
         * 标题
         */
        private String title;
        /**
         * 内容
         */
        private String content;
        /**
         * 摘要
         */
        private String excerpt;
        /**
         * 描述
         */
        private String description;
        /**
         * 分数
         */
        private float score;
        /**
         * 分类ID
         */
        private Long categoryId;
        /**
         * 分类名称
         */
        private String categoryName;
        /**
         * 标签
         */
        private String tags;
        /**
         * 技术栈
         */
        private String stacks;
    }
}