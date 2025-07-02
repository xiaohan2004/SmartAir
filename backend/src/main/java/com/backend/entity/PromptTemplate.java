package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document("prompt_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    @Id
    private String id;

    private String name;
    private String template;
    private String description;

    @Field("updated_at")
    private Date updatedAt;
}
