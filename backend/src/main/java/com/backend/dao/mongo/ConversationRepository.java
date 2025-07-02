package com.backend.dao.mongo;

import com.backend.entity.ConversationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<ConversationDocument, String> {
    Optional<ConversationDocument> findByConversationUuid(String uuid);
    List<ConversationDocument> findByUserId(Long userId);
}
