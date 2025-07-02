package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document("conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDocument {

    @Id
    private String id;

    @Field("conversation_uuid")
    private String conversationUuid;

    @Field("user_id")
    private Long userId;

    @Field("messages")
    private List<Message> messages;

    @Field("metadata")
    private Metadata metadata;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String speaker;
        private String text;
        private Date timestamp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Metadata {
        @Field("session_start")
        private Date sessionStart;
        @Field("session_end")
        private Date sessionEnd;
    }
}
