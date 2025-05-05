package org.visage.backend.service;

import cn.hutool.core.collection.CollectionUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.QueryResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.response.QueryResultsWrapper;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.visage.backend.util.FileUtil;
import java.io.File;
import java.nio.file.Files;
import java.rmi.ServerException;
import java.util.*;


@Service
public class MilvusService {

    @Resource
    private MilvusVectorStore vectorStore;

    @Value("${spring.ai.vectorstore.milvus.collectionName}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.metadata-field-name}")
    private String metadataFieldName;

    private final String hashFieldName = "hash"; // 用于去重的字段名

    public List<Document> testMilvus() {
        // 1. 构造三条 Document 数据，每条有内容（text）和可选的 metadata。
        // metadata 通常用于辅助筛选、记录上下文信息等。可以用它来标识文档所属。
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        // 2. 把这些文本通过 Embedding 模型转为向量，然后插入到 Milvus 向量数据库中。
        vectorStore.add(documents);
        // 3. 使用一个查询词 "Spring"，做相似度搜索，返回最接近的 5 条记录（topK=5）。
        return vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
    }


    public List<Document> importAndSearchDocuments() throws Exception {
        List<File> files = List.of(
                new File("/Users/zhouxujin/Documents/pcc/谈一谈家校共育的内容有哪些.docx"),
                new File("/Users/zhouxujin/Documents/pcc/第一单元教学设计案例.docx"),
                new File("/Users/zhouxujin/Documents/pcc/家校共育小故事.docx")
                );
        List<Document> documents = new ArrayList<>();
        for (File file : files) {
            String text = FileUtil.extractTextFromFile(file);
            if (text != null && !text.isBlank()) {
                String filename = file.getName();
                String filetype = FileUtil.getFileExtension(file);
                long filesize = Files.size(file.toPath());
                // 分片逻辑：每 chunk 500 字符，重叠 100 字符
                List<String> chunks = FileUtil.splitText(text, 500, 100);
                for (int i = 0; i < chunks.size(); i++) {
                    String hash = FileUtil.sha256(chunks.get(i));
                    // 去重逻辑：如果 hash 已存在，则跳过（要求 hashFieldName 必须为索引字段）
                    if(!this.exists(hash)) {
                        Map<String, Object> metadata = Map.of(
                                "filename", filename,
                                "filetype", filetype,
                                "filesize", filesize,
                                "chunk_index", i,
                                hashFieldName, hash// 计算哈希值
                        );
                        documents.add(new Document(chunks.get(i), metadata));
                    }
                }
            }
        }
        if(CollectionUtil.isNotEmpty(documents)) {
            // 插入向量数据库
            vectorStore.add(documents);
        }
        // 相似度搜索
        return vectorStore.similaritySearch(
                SearchRequest.builder().query("如何开展第一单元的教学设计，并且给出教学设计方面的教学反思").topK(5).build()
        );
    }

    /**
     * 根据hash去重
     * @param hash
     * @return
     * @throws ServerException
     */
    private boolean exists(String hash) throws ServerException {
        Optional<MilvusServiceClient> o = vectorStore.getNativeClient();
        if (o.isEmpty()) {
            throw new ServerException("Milvus client 不可用");
        }
        MilvusServiceClient milvusClient = o.get();

        // Milvus 的查询表达式，使用 json_contains() 函数检查 JSON 字段是否包含特定值。
        String expr = String.format("%s['%s'] == '%s'",
                metadataFieldName,  // "field_json"
                hashFieldName,     // "hash"
                hash
        );

        // 调试输出
        System.out.println("执行去重查询，表达式: " + expr);

        QueryParam queryParam = QueryParam.newBuilder()
                .withCollectionName(collectionName)  // 使用集合名
                .withExpr(expr)
                .withOutFields(Collections.singletonList(metadataFieldName)) // 查询返回整个JSON字段
                .build();

        R<QueryResults> result = milvusClient.query(queryParam);
        QueryResultsWrapper wrapper = new QueryResultsWrapper(result.getData());
        long count =  wrapper.getFieldWrapper(metadataFieldName).getRowCount();
        return count > 0;
    }

}
