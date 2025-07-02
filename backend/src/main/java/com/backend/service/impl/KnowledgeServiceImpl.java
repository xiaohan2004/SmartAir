package com.backend.service.impl;

import com.backend.dao.mongo.KnowledgeDocumentRepository;
import com.backend.entity.KnowledgeDocument;
import com.backend.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDao;

    @Override
    public KnowledgeDocument addKnowledge(String title, String content, List<String> tags) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setTags(tags);
        doc.setLastUpdated(new Date());
        return knowledgeDao.save(doc);
    }

    @Override
    public List<KnowledgeDocument> searchByTitle(String keyword) {
        return knowledgeDao.findByTitleContaining(keyword);
    }

    @Override
    public void updateContentById(String id, String newContent) {
        KnowledgeDocument doc = knowledgeDao.findById(id)
            .orElseThrow(() -> new RuntimeException("文档不存在"));
        doc.setContent(newContent);
        doc.setLastUpdated(new Date());
        knowledgeDao.save(doc);
    }

    @Override
    public void deleteById(String id) {
        knowledgeDao.deleteById(id);
    }

    @Override
    public List<KnowledgeDocument> listAll() {
        return knowledgeDao.findAll();
    }
}
