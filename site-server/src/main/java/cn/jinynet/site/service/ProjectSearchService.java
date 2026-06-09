package cn.jinynet.site.service;

import cn.jinynet.site.entity.Project;
import cn.jinynet.site.entity.ProjectStack;
import cn.jinynet.site.entity.ProjectTable;
import cn.jinynet.site.entity.dto.ProjectDetail;
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
 * 项目索引服务
 *
 * @author jinty
 */
@Slf4j
@Service
public class ProjectSearchService {
    private final JSqlClient sqlClient;
    private final Analyzer analyzer;
    private final String indexPath;

    public ProjectSearchService(JSqlClient sqlClient, @Value("${lucene.index.project-path:./lucene/index/project}") String indexPath) {
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
     * 索引项目
     */
    public void indexProject(Project project) {
        try {
            IndexWriter writer = getIndexWriter();

            Document doc = new Document();
            doc.add(new StringField("id", String.valueOf(project.id()), Field.Store.YES));
            doc.add(new TextField("name", project.name(), Field.Store.YES));

            if (project.description() != null) {
                doc.add(new TextField("description", Objects.requireNonNull(project.description()), Field.Store.YES));
            }

            if (project.content() != null) {
                doc.add(new TextField("content", Objects.requireNonNull(project.content()), Field.Store.YES));
            }

            if (project.role() != null) {
                doc.add(new TextField("role", Objects.requireNonNull(project.role()), Field.Store.YES));
            }

            if (project.contribution() != null) {
                doc.add(new TextField("contribution", Objects.requireNonNull(project.contribution()), Field.Store.YES));
            }

            if (project.stacks() != null && !project.stacks().isEmpty()) {
                String stackNames = project.stacks().stream().map(ProjectStack::name).reduce((a, b) -> a + " " + b).orElse("");
                doc.add(new TextField("stacks", stackNames, Field.Store.YES));
            }

            doc.add(new StringField("status", project.status(), Field.Store.YES));
            doc.add(new StringField("published", String.valueOf(project.published()), Field.Store.YES));

            writer.updateDocument(new Term("id", String.valueOf(project.id())), doc);
            writer.commit();
            writer.close();

            log.info("Indexed project: {}", project.id());
        } catch (IOException e) {
            log.error("Failed to index project: {}", project.id(), e);
        }
    }

    /**
     * 删除项目索引
     */
    public void deleteProjectFromIndex(Long projectId) {
        try {
            IndexWriter writer = getIndexWriter();
            writer.deleteDocuments(new Term("id", String.valueOf(projectId)));
            writer.commit();
            writer.close();
            log.info("Deleted project from index: {}", projectId);
        } catch (IOException e) {
            log.error("Failed to delete project from index: {}", projectId, e);
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
                List<Project> projects = sqlClient.createQuery(ProjectTable.$)
                    .where(ProjectTable.$.published().eq(true))
                    .select(ProjectTable.$.fetch(ProjectDetail.METADATA.getFetcher()))
                    .execute();

                for (Project project : projects) {
                    Document doc = new Document();
                    doc.add(new StringField("id", String.valueOf(project.id()), Field.Store.YES));
                    doc.add(new TextField("name", project.name(), Field.Store.YES));

                    if (project.description() != null) {
                        doc.add(new TextField("description", Objects.requireNonNull(project.description()), Field.Store.YES));
                    }

                    if (project.content() != null) {
                        doc.add(new TextField("content", Objects.requireNonNull(project.content()), Field.Store.YES));
                    }

                    if (project.role() != null) {
                        doc.add(new TextField("role", Objects.requireNonNull(project.role()), Field.Store.YES));
                    }

                    if (project.contribution() != null) {
                        doc.add(new TextField("contribution", Objects.requireNonNull(project.contribution()), Field.Store.YES));
                    }

                    if (project.stacks() != null && !project.stacks().isEmpty()) {
                        String stackNames = project.stacks().stream().map(ProjectStack::name).reduce((a, b) -> a + " " + b).orElse("");
                        doc.add(new TextField("stacks", stackNames, Field.Store.YES));
                    }

                    doc.add(new StringField("status", project.status(), Field.Store.YES));
                    doc.add(new StringField("published", String.valueOf(project.published()), Field.Store.YES));

                    writer.addDocument(doc);
                }

                writer.commit();
                log.info("Rebuilt project index with {} projects", projects.size());
            }
        } catch (LockObtainFailedException e) {
            log.error("Index is locked by another process. Please ensure no other application is accessing the index.", e);
        } catch (IOException e) {
            log.error("Failed to rebuild index", e);
        }
    }


    /**
     * 搜索项目
     */
    public List<SearchResult> search(String keyword, int pageSize) {
        List<SearchResult> results = new ArrayList<>();

        try {
            IndexSearcher searcher = getIndexSearcher();

            BooleanQuery.Builder boolQueryBuilder = new BooleanQuery.Builder();

            QueryParser nameParser = new QueryParser("name", analyzer);
            Query nameQuery = nameParser.parse(keyword);
            boolQueryBuilder.add(nameQuery, BooleanClause.Occur.SHOULD);

            QueryParser descriptionParser = new QueryParser("description", analyzer);
            Query descriptionQuery = descriptionParser.parse(keyword);
            boolQueryBuilder.add(descriptionQuery, BooleanClause.Occur.SHOULD);

            QueryParser contentParser = new QueryParser("content", analyzer);
            Query contentQuery = contentParser.parse(keyword);
            boolQueryBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);

            QueryParser stacksParser = new QueryParser("stacks", analyzer);
            Query stacksQuery = stacksParser.parse(keyword);
            boolQueryBuilder.add(stacksQuery, BooleanClause.Occur.SHOULD);

            BooleanQuery boolQuery = boolQueryBuilder.build();

            TermQuery publishedQuery = new TermQuery(new Term("published", "true"));
            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
            finalQueryBuilder.add(boolQuery, BooleanClause.Occur.MUST);
            finalQueryBuilder.add(publishedQuery, BooleanClause.Occur.MUST);

            Query finalQuery = finalQueryBuilder.build();

            TopDocs topDocs = searcher.search(finalQuery, pageSize);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<mark>", "</mark>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(finalQuery));
            highlighter.setTextFragmenter(new SimpleFragmenter(150));

            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);

                String id = doc.get("id");
                String name = doc.get("name");
                String description = doc.get("description");
                String content = doc.get("content");
                String stacks = doc.get("stacks");

                String highlightedName = highlighter.getBestFragment(analyzer, "name", name);
                if (highlightedName == null) highlightedName = name;

                String highlightedDescription = null;
                if (description != null) {
                    highlightedDescription = highlighter.getBestFragment(analyzer, "description", description);
                }

                String highlightedContent = null;
                if (content != null) {
                    highlightedContent = highlighter.getBestFragment(analyzer, "content", content);
                }

                SearchResult result = new SearchResult();
                result.setId(Long.parseLong(id));
                result.setName(highlightedName);
                result.setDescription(highlightedDescription);
                result.setContent(highlightedContent);
                result.setScore(scoreDoc.score);
                result.setStacks(stacks);

                results.add(result);
            }

            searcher.getIndexReader().close();
        } catch (Exception e) {
            log.error("Search failed for keyword: {}", keyword, e);
        }

        return results;
    }

    /**
     * 项目搜索结果
     */
    @Data
    public static class SearchResult {
        private Long id;
        private String name;
        private String description;
        private String content;
        private float score;
        private String stacks;

    }
}