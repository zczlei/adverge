package com.adverge.backend.repository;

import com.adverge.backend.model.Config;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends MongoRepository<Config, String> {
} 