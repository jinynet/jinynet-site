package cn.jinynet.site.api.admin;

import cn.jinynet.site.entity.*;
import cn.jinynet.site.entity.dto.*;
import cn.jinynet.site.service.ArticleService;
import cn.jinynet.starter.common.types.exception.BaseBizException;
import cn.jinynet.starter.common.types.request.PageRequest;
import cn.jinynet.starter.common.types.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class AdminArticleApi {

    private final JSqlClient sqlClient;
    private final ArticleService articleService;

    /**
     * 获取文章列表
     *
     * @param sc 搜索条件
     * @param pq 分页参数
     * @return 文章列表
     */
    @GetMapping
    public Result<Page<Article>> getArticles(ArticleSpecification sc, PageRequest pq) {
        List<Order> orders = pq.order()
                ? Order.makeOrders(ArticleTable.$, pq.getOrderBy())
                : Order.makeOrders(ArticleTable.$, "updatedAt desc");
        Page<Article> articleListPage = sqlClient.createQuery(ArticleTable.$)
                .where(sc)
                .orderBy(orders)
                .select(ArticleTable.$.fetch(ArticleList.METADATA.getFetcher()))
                .fetchPage(pq.getPageIndex(), pq.getPageSize());
        return Result.success(articleListPage);
    }

    /**
     * 创建文章
     *
     * @param articleForm 文章表单
     * @return 创建成功的文章
     */
    @PostMapping
    public Result<Article> createArticle(@RequestBody ArticleForm articleForm) {
        return Result.success(articleService.createArticle(articleForm));
    }

    /**
     * 创建并发布文章
     *
     * @param articleForm 文章表单
     * @return 发布成功的文章
     */
    @PostMapping("/publish")
    public Result<Article> publishArticle(@RequestBody ArticleForm articleForm) {
        return Result.success(articleService.publishArticle(articleForm));
    }

    /**
     * 更新并发布文章
     *
     * @param id          文章ID
     * @param articleForm 文章表单
     * @return 发布成功的文章
     */
    @PutMapping("/{id}/publish")
    public Result<Article> updateAndPublishArticle(@PathVariable long id, @RequestBody ArticleForm articleForm) {
        return Result.success(articleService.updateAndPublishArticle(id, articleForm));
    }

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public Result<Article> getArticle(@PathVariable long id) {
        Article articleDetail = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.id().eq(id))
                .select(ArticleTable.$.fetch(ArticleDetail.METADATA.getFetcher()))
                .fetchOptional()
                .orElseThrow(() -> new BaseBizException("文章不存在"));
        return Result.success(articleDetail);
    }

    /**
     * 更新文章
     *
     * @param id          文章ID
     * @param articleForm 文章表单
     * @return 更新的文章
     */
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable long id, @RequestBody ArticleForm articleForm) {
        return Result.success(articleService.updateArticle(id, articleForm));
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteArticle(@PathVariable long id) {
        return Result.success(articleService.deleteArticle(id));
    }

    /**
     * 获取文章标签列表
     *
     * @param sc 搜索条件
     * @return 标签列表
     */
    @GetMapping("/tags")
    public Result<List<ArticleTag>> getTags(ArticleTagSpecification sc) {
        List<ArticleTag> tags = sqlClient.createQuery(ArticleTagTable.$)
                .where(sc)
                .orderBy(ArticleTagTable.$.sortOrder().asc(), ArticleTagTable.$.id().desc())
                .select(ArticleTagTable.$.fetch(ArticleTagDetail.METADATA.getFetcher()))
                .execute();
        return Result.success(tags);
    }

    /**
     * 创建文章标签
     *
     * @param tag 标签实体
     * @return 创建的标签
     */
    @PostMapping("/tags")
    public Result<ArticleTag> createTag(@RequestBody ArticleTagForm tag) {
        ArticleTag newTag = ArticleTagDraft.$.produce(tag.toEntity(), draft -> {

        });
        return Result.success(sqlClient.saveCommand(newTag)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity());
    }

    /**
     * 更新文章标签
     *
     * @param tagId 标签ID
     * @param tag   标签实体
     * @return 更新后的标签
     */
    @PutMapping("/tags/{tagId}")
    public Result<ArticleTag> updateTag(@PathVariable long tagId, @RequestBody ArticleTagForm tag) {
        ArticleTag updatedTag = ArticleTagDraft.$.produce(tag.toEntityById(tagId), draft -> {
        });
        return Result.success(sqlClient.saveCommand(updatedTag)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity());
    }

    /**
     * 删除文章标签
     *
     * @param tagId 标签ID
     * @return 是否删除成功
     */
    @DeleteMapping("/tags/{tagId}")
    public Result<Boolean> deleteTag(@PathVariable long tagId) {
        return Result.success(sqlClient.deleteById(ArticleTag.class, tagId)
                .getAffectedRowCount(ArticleTag.class) > 0);
    }

    /**
     * 获取文章分类列表
     *
     * @param sc 搜索条件
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<ArticleCategory>> getCategories(ArticleCategorySpecification sc) {
        List<ArticleCategory> categories = sqlClient.createQuery(ArticleCategoryTable.$)
                .where(sc)
                .orderBy(ArticleCategoryTable.$.sortOrder().asc(), ArticleCategoryTable.$.id().desc())
                .select(ArticleCategoryTable.$.fetch(ArticleCategoryDetail.METADATA.getFetcher()))
                .execute();
        return Result.success(categories);
    }

    /**
     * 创建文章分类
     *
     * @param category 分类实体
     * @return 创建的分类
     */
    @PostMapping("/categories")
    public Result<ArticleCategory> createCategory(@RequestBody ArticleCategoryForm category) {
        ArticleCategory newCategory = ArticleCategoryDraft.$.produce(category.toEntity(), draft -> {

        });
        return Result.success(sqlClient.saveCommand(newCategory)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity());
    }

    /**
     * 更新文章分类
     *
     * @param categoryId 分类ID
     * @param category   分类实体
     * @return 更新后的分类
     */
    @PutMapping("/categories/{categoryId}")
    public Result<ArticleCategory> updateCategory(@PathVariable long categoryId, @RequestBody ArticleCategoryForm category) {
        ArticleCategory updatedCategory = ArticleCategoryDraft.$.produce(category.toEntityById(categoryId), draft -> {

        });
        return Result.success(sqlClient.saveCommand(updatedCategory)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity());
    }

    /**
     * 删除文章分类
     *
     * @param categoryId 分类ID
     * @return 是否删除成功
     */
    @DeleteMapping("/categories/{categoryId}")
    public Result<Boolean> deleteCategory(@PathVariable long categoryId) {
        return Result.success(sqlClient.deleteById(ArticleCategory.class, categoryId)
                .getAffectedRowCount(ArticleCategory.class) > 0);
    }


}