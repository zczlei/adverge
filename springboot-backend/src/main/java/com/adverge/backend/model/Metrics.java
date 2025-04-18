package com.adverge.backend.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "metrics")
@CompoundIndexes({
    @CompoundIndex(name = "platform_timestamp", def = "{'platform': 1, 'timestamp': 1}"),
    @CompoundIndex(name = "placementId_timestamp", def = "{'placementId': 1, 'timestamp': 1}")
})
public class Metrics {
    
    @Id
    private String id;
    
    @Indexed
    private String placementId;
    
    private String platform;
    
    private double price;
    
    @Indexed
    private Date timestamp;
    
    private UserData userData;
    
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
} 