package com.backend.service;

import com.backend.entity.PromptTemplate;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateService {
    PromptTemplate createTemplate(String name, String template, String description);
    Optional<PromptTemplate> getByName(String name);
    List<PromptTemplate> listAll();
    void updateTemplate(String id, PromptTemplate newTemplate);
    void deleteTemplate(String id);
}
