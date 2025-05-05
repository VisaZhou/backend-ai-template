package org.visage.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.visage.backend.service.MilvusService;
import org.visage.backend.util.R;
import java.util.List;

@Tag(name = "milvus-RAG增强", description = "milvus-RAG增强")
@RestController
@RequestMapping("/milvus")
public class MilvusController {

    @Resource
    private MilvusService milvusService;

    @GetMapping("/test")
    public R<List<Document>> test() {
        List<Document> documents = milvusService.testMilvus();
        return R.ok(documents);
    }

    @GetMapping("/testFile")
    public R<List<Document>> testFile() throws Exception {
        List<Document> documents = milvusService.importAndSearchDocuments();
        return R.ok(documents);
    }
}
