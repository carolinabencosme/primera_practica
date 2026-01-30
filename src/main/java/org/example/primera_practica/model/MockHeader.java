package org.example.primera_practica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_headers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "header_key", nullable = false)
    private String headerKey;
    
    @Column(name = "header_value", nullable = false)
    private String headerValue;
    
    @ManyToOne
    @JoinColumn(name = "mock_endpoint_id", nullable = false)
    private MockEndpoint mockEndpoint;
}
