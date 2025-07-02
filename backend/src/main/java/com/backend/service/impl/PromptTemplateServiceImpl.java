package com.backend.service.impl;

import com.backend.dao.mongo.PromptTemplateRepository;
import com.backend.service.PromptTemplateService;
import com.backend.entity.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateRepository promptDao;

    @Override
    public PromptTemplate createTemplate(String name, String template, String description) {
        PromptTemplate pt = new PromptTemplate();
        pt.setName(name);
        pt.setTemplate(template);
        pt.setDescription(description);
        pt.setUpdatedAt(new Date());
        return promptDao.save(pt);
    }

    @Override
    public Optional<PromptTemplate> getByName(String name) {
        return promptDao.findByName(name);
    }

    @Override
    public List<PromptTemplate> listAll() {
        return promptDao.findAll();
    }

    @Override
    public void updateTemplate(String id, String newTemplate) {
        PromptTemplate pt = promptDao.findById(id)
            .orElseThrow(() -> new RuntimeException("模板不存在"));
        pt.setTemplate(newTemplate);
        pt.setUpdatedAt(new Date());
        promptDao.save(pt);
    }

    @Override
    public void deleteTemplate(String id) {
        promptDao.deleteById(id);
    }
}
