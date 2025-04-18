package com.adverge.backend.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class UserData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    private GeoData geo;
    
    private String device;
    private String os;
} 