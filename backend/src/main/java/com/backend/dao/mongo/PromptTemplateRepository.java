package com.backend.dao.mongo;

import com.backend.entity.PromptTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends MongoRepository<PromptTemplate, String> {
    Optional<PromptTemplate> findByName(String name);
}
