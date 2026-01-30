package org.example.primera_practica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_endpoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String path;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HttpMethod method;
    
    @Column(name = "http_status_code", nullable = false)
    private Integer httpStatusCode;
    
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
    @Lob
    @Column(name = "response_body")
    private String responseBody;
    
    @OneToMany(mappedBy = "mockEndpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockHeader> headers = new ArrayList<>();
    
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    @Column(name = "delay_seconds")
    private Integer delaySeconds;
    
    @Column(name = "requires_jwt")
    private Boolean requiresJwt = false;
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
