package com.backend.dao.mongo;

import com.backend.entity.KnowledgeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends MongoRepository<KnowledgeDocument, String> {
    // 可添加自定义查询，例如按标题模糊查找
    List<KnowledgeDocument> findByTitleContaining(String keyword);
}
