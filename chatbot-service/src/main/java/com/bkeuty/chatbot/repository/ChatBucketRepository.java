package com.bkeuty.chatbot.repository;

import com.bkeuty.chatbot.entity.ChatBucket;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ChatBucketRepository extends MongoRepository<ChatBucket, String> {
    Optional<ChatBucket> findTopBySessionIdOrderByBucketIndexDesc(String sessionId);
    List<ChatBucket> findAllBySessionIdOrderByBucketIndexAsc(String sessionId);
}
