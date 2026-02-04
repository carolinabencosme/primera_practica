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
    
    @NotBlank(message = "{mock.validation.name.required}")
    @Size(max = 255, message = "{mock.validation.name.max}")
    private String name;
    
    @Size(max = 1000, message = "{mock.validation.description.max}")
    private String description;
    
    @NotBlank(message = "{mock.validation.path.required}")
    private String path;
    
    @NotNull(message = "{mock.validation.method.required}")
    private HttpMethod method;
    
    @NotNull(message = "{mock.validation.statusCode.required}")
    @Min(value = 100, message = "{mock.validation.statusCode.min}")
    @Max(value = 599, message = "{mock.validation.statusCode.max}")
    private Integer httpStatusCode;
    
    @NotBlank(message = "{mock.validation.contentType.required}")
    private String contentType;
    
    private String responseBody;
    
    private List<MockHeaderDTO> headers = new ArrayList<>();

    @Pattern(
            regexp = "^(ONE_YEAR|ONE_MONTH|ONE_WEEK|ONE_DAY|ONE_HOUR)?$",
            message = "{mock.validation.expirationOption.invalid}")
    private String expirationOption;

    private LocalDateTime expirationDate;
    
    @Min(value = 0, message = "{mock.validation.delaySeconds.min}")
    private Integer delaySeconds;
    
    private Boolean requiresJwt = false;

    private String generatedJwt;
    
    private String createdBy;
    
    @NotNull(message = "{mock.validation.project.required}")
    private Long projectId;
    
    private String projectName;
    
    private LocalDateTime createdAt;
}
