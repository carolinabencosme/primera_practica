package org.example.primera_practica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.primera_practica.model.HttpMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockEndpointDTO {
    private Long id;
    
    @NotBlank(message = "Endpoint name is required")
    @Size(max = 255, message = "Endpoint name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Path is required")
    private String path;
    
    @NotNull(message = "HTTP method is required")
    private HttpMethod method;
    
    @NotNull(message = "HTTP status code is required")
    @Min(value = 100, message = "HTTP status code must be at least 100")
    @Max(value = 599, message = "HTTP status code must not exceed 599")
    private Integer httpStatusCode;
    
    @NotBlank(message = "Content type is required")
    private String contentType;
    
    private String responseBody;
    
    private List<MockHeaderDTO> headers = new ArrayList<>();
    
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expirationDate;
    
    @Min(value = 0, message = "Delay seconds must be non-negative")
    private Integer delaySeconds;
    
    private Boolean requiresJwt = false;
    
    private String createdBy;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private String projectName;
    
    private LocalDateTime createdAt;
}
