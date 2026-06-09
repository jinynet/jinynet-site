package cn.jinynet.site.service;

import cn.jinynet.site.entity.Article;
import cn.jinynet.site.entity.ArticleTable;
import cn.jinynet.site.entity.ArticleTag;
import cn.jinynet.site.entity.dto.ArticleDetail;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文章搜索服务
 */
@Slf4j
@Service
public class ArticleSearchService {
    private final JSqlClient sqlClient;
    private final Analyzer analyzer;
    private final String indexPath;

    public ArticleSearchService(JSqlClient sqlClient, 
                               @Value("${lucene.index.article-path:./lucene/index/article}") String indexPath) {
        this.sqlClient = sqlClient;
        this.analyzer = new SmartChineseAnalyzer();
        this.indexPath = indexPath;
        ensureIndexDirectory();
    }

    /**
     * 确保索引目录存在
     */
    private void ensureIndexDirectory() {
        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            if (!DirectoryReader.indexExists(dir)) {
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                IndexWriter writer = new IndexWriter(dir, config);
                writer.close();
            }
            dir.close();
        } catch (IOException e) {
            log.error("Failed to ensure index directory", e);
        }
    }

    /**
     * 获取索引写入器
     */
    private IndexWriter getIndexWriter() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(dir, config);
    }

    /**
     * 获取索引搜索器
     */
    private IndexSearcher getIndexSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

    /**
     * 索引文章
     */
    public void indexArticle(Article article) {
        if (article == null) return;
        try {
            IndexWriter writer = getIndexWriter();
            
            Document doc = new Document();
            doc.add(new StringField("id", String.valueOf(article.id()), Field.Store.YES));
            doc.add(new TextField("title", article.title(), Field.Store.YES));
            
            if (article.content() != null) {
                doc.add(new TextField("content", Objects.requireNonNull(article.content()), Field.Store.YES));
            }
            
            if (article.excerpt() != null) {
                doc.add(new TextField("excerpt", Objects.requireNonNull(article.excerpt()), Field.Store.YES));
            }
            
            if (article.category() != null) {
                doc.add(new StringField("categoryId", String.valueOf(Objects.requireNonNull(article.category()).id()), Field.Store.YES));
                doc.add(new TextField("categoryName", Objects.requireNonNull(article.category()).name(), Field.Store.YES));
            }
            
            if (article.tags() != null && !article.tags().isEmpty()) {
                String tagNames = article.tags().stream()
                    .map(ArticleTag::name)
                    .reduce((a, b) -> a + " " + b)
                    .orElse("");
                doc.add(new TextField("tags", tagNames, Field.Store.YES));
            }
            
            doc.add(new StringField("status", article.status(), Field.Store.YES));
            
            writer.updateDocument(new Term("id", String.valueOf(article.id())), doc);
            writer.commit();
            writer.close();
            
            log.info("Indexed article: {}", article.id());
        } catch (IOException e) {
            log.error("Failed to index article: {}", article.id(), e);
        }
    }

    /**
     * 从索引中删除文章
     */
    public void deleteArticleFromIndex(Long articleId) {
        try {
            IndexWriter writer = getIndexWriter();
            writer.deleteDocuments(new Term("id", String.valueOf(articleId)));
            writer.commit();
            writer.close();
            log.info("Deleted article from index: {}", articleId);
        } catch (IOException e) {
            log.error("Failed to delete article from index: {}", articleId, e);
        }
    }


    /**
     * 重建索引
     */
    public void rebuildIndex() {
        try (Directory dir = FSDirectory.open(Paths.get(indexPath))) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // 完全重建
            
            try (IndexWriter writer = new IndexWriter(dir, config)) {
                List<Article> articles = sqlClient.createQuery(ArticleTable.$)
                    .where(ArticleTable.$.status().eq("published"))
                    .select(ArticleTable.$.fetch(ArticleDetail.METADATA.getFetcher()))
                    .execute();

                for (Article article : articles) {
                    Document doc = new Document();
                    doc.add(new StringField("id", String.valueOf(article.id()), Field.Store.YES));
                    doc.add(new TextField("title", article.title(), Field.Store.YES));

                    if (article.content() != null) {
                        doc.add(new TextField("content", Objects.requireNonNull(article.content()), Field.Store.YES));
                    }

                    if (article.excerpt() != null) {
                        doc.add(new TextField("excerpt", Objects.requireNonNull(article.excerpt()), Field.Store.YES));
                    }

                    if (article.category() != null) {
                        doc.add(new StringField("categoryId", String.valueOf(Objects.requireNonNull(article.category()).id()), Field.Store.YES));
                        doc.add(new TextField("categoryName", Objects.requireNonNull(article.category()).name(), Field.Store.YES));
                    }

                    if (article.tags() != null && !article.tags().isEmpty()) {
                        String tagNames = article.tags().stream()
                            .map(ArticleTag::name)
                            .reduce((a, b) -> a + " " + b)
                            .orElse("");
                        doc.add(new TextField("tags", tagNames, Field.Store.YES));
                    }

                    doc.add(new StringField("status", article.status(), Field.Store.YES));

                    writer.addDocument(doc);
                }

                writer.commit();
                log.info("Rebuilt article index with {} articles", articles.size());
            }
        } catch (LockObtainFailedException e) {
            log.error("Index is locked by another process. Please ensure no other application is accessing the index.", e);
        } catch (IOException e) {
            log.error("Failed to rebuild index", e);
        }
    }

    /**
     * 搜索文章
     * @param keyword 关键词
     * @param pageSize 页大小
     */
    public List<SearchResult> search(String keyword, int pageSize) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            IndexSearcher searcher = getIndexSearcher();
            
            BooleanQuery.Builder boolQueryBuilder = new BooleanQuery.Builder();
            
            QueryParser titleParser = new QueryParser("title", analyzer);
            Query titleQuery = titleParser.parse(keyword);
            boolQueryBuilder.add(titleQuery, BooleanClause.Occur.SHOULD);
            
            QueryParser contentParser = new QueryParser("content", analyzer);
            Query contentQuery = contentParser.parse(keyword);
            boolQueryBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);
            
            QueryParser excerptParser = new QueryParser("excerpt", analyzer);
            Query excerptQuery = excerptParser.parse(keyword);
            boolQueryBuilder.add(excerptQuery, BooleanClause.Occur.SHOULD);
            
            QueryParser tagsParser = new QueryParser("tags", analyzer);
            Query tagsQuery = tagsParser.parse(keyword);
            boolQueryBuilder.add(tagsQuery, BooleanClause.Occur.SHOULD);
            
            BooleanQuery boolQuery = boolQueryBuilder.build();
            
            TermQuery statusQuery = new TermQuery(new Term("status", "published"));
            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
            finalQueryBuilder.add(boolQuery, BooleanClause.Occur.MUST);
            finalQueryBuilder.add(statusQuery, BooleanClause.Occur.MUST);
            
            Query finalQuery = finalQueryBuilder.build();
            
            TopDocs topDocs = searcher.search(finalQuery, pageSize);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<mark>", "</mark>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(finalQuery));
            highlighter.setTextFragmenter(new SimpleFragmenter(150));
            
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                
                String id = doc.get("id");
                String title = doc.get("title");
                String content = doc.get("content");
                String excerpt = doc.get("excerpt");
                String categoryId = doc.get("categoryId");
                String categoryName = doc.get("categoryName");
                String tags = doc.get("tags");
                
                String highlightedTitle = highlighter.getBestFragment(analyzer, "title", title);
                if (highlightedTitle == null) highlightedTitle = title;
                
                String highlightedContent = null;
                if (content != null) {
                    highlightedContent = highlighter.getBestFragment(analyzer, "content", content);
                }
                
                String highlightedExcerpt = null;
                if (excerpt != null) {
                    highlightedExcerpt = highlighter.getBestFragment(analyzer, "excerpt", excerpt);
                }
                
                SearchResult result = new SearchResult();
                result.setId(Long.parseLong(id));
                result.setTitle(highlightedTitle);
                result.setContent(highlightedContent);
                result.setExcerpt(highlightedExcerpt);
                result.setScore(scoreDoc.score);
                result.setCategoryId(categoryId != null ? Long.parseLong(categoryId) : null);
                result.setCategoryName(categoryName);
                result.setTags(tags);
                
                results.add(result);
            }
            
            searcher.getIndexReader().close();
        } catch (Exception e) {
            log.error("Search failed for keyword: {}", keyword, e);
        }
        
        return results;
    }

    /**
     * 文章搜索结果
     */
    @Data
    public static class SearchResult {
        private Long id;
        private String title;
        private String content;
        private String excerpt;
        private float score;
        private Long categoryId;
        private String categoryName;
        private String tags;
    }
}