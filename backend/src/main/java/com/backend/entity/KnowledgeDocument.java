package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document("knowledge_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    @Id
    private String id;

    private String title;
    private String content;
    private List<String> tags;

    @Field("last_updated")
    private Date lastUpdated;
}
