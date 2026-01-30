package org.example.primera_practica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockHeaderDTO {
    private Long id;
    
    @NotBlank(message = "Header key is required")
    private String headerKey;
    
    @NotBlank(message = "Header value is required")
    private String headerValue;
}
