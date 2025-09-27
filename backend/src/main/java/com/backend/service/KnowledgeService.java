package com.backend.service;

import com.backend.entity.KnowledgeDocument;

import java.util.List;

public interface KnowledgeService {
    KnowledgeDocument addKnowledge(String title, String content, List<String> tags);
    List<KnowledgeDocument> searchByTitle(String keyword);
    void updateContentById(String id, KnowledgeDocument newDoc);
    void deleteById(String id);
    List<KnowledgeDocument> listAll();
}