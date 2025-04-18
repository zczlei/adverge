package com.adverge.backend.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class GeoData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String country;
    private String region;
    private String city;
} 