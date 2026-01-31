package org.example.primera_practica.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.exception.ResourceNotFoundException;
import org.example.primera_practica.model.HttpMethod;
import org.example.primera_practica.service.JwtService;
import org.example.primera_practica.service.MockEndpointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/mock")
public class MockApiController {

    private final MockEndpointService mockEndpointService;
    private final JwtService jwtService;

    public MockApiController(MockEndpointService mockEndpointService, JwtService jwtService) {
        this.mockEndpointService = mockEndpointService;
        this.jwtService = jwtService;
    }

    @RequestMapping(
        value = "/{projectName}/**",
        method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
                  RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}
    )
    public ResponseEntity<String> executeMock(
            @PathVariable String projectName,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String mockPath = null;
        HttpMethod httpMethod = null;

        try {
            // Extract path after /api/mock/{projectName}/
            String fullPath = request.getRequestURI();
            String basePath = "/api/mock/" + projectName;
            mockPath = fullPath.substring(fullPath.indexOf(basePath) + basePath.length());
            
            if (mockPath.isEmpty()) {
                mockPath = "/";
            }

            // Get HTTP method
            httpMethod = HttpMethod.valueOf(request.getMethod());

            // Find mock endpoint
            MockEndpointDTO mockEndpoint = mockEndpointService
                .findMockByProjectAndPathAndMethod(projectName, mockPath, httpMethod);

            // Validate not expired
            if (mockEndpoint.getExpirationDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.GONE)
                    .body("{\"error\": \"Mock endpoint has expired\"}");
            }

            // Validate JWT if required
            if (Boolean.TRUE.equals(mockEndpoint.getRequiresJwt())) {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"JWT token is required\"}");
                }

                String token = authHeader.substring(7);
                if (!jwtService.validateToken(token)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid or expired JWT token\"}");
                }
            }

            // Apply delay if configured
            if (mockEndpoint.getDelaySeconds() != null && mockEndpoint.getDelaySeconds() > 0) {
                try {
                    Thread.sleep(mockEndpoint.getDelaySeconds() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Build response with configured headers, status, content-type, body
            var responseBuilder = ResponseEntity.status(mockEndpoint.getHttpStatusCode());
            
            // Add content type header
            responseBuilder.header("Content-Type", mockEndpoint.getContentType());
            
            // Add custom headers
            if (mockEndpoint.getHeaders() != null) {
                mockEndpoint.getHeaders().forEach(header -> 
                    responseBuilder.header(header.getHeaderKey(), header.getHeaderValue())
                );
            }

            // Return response with body
            String responseBody = mockEndpoint.getResponseBody() != null ? 
                mockEndpoint.getResponseBody() : "";
            
            return responseBuilder.body(responseBody);

        } catch (ResourceNotFoundException e) {
            String errorMessage = (httpMethod != null && mockPath != null)
                ? "Mock endpoint not found for " + httpMethod + " " + mockPath
                : e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"" + errorMessage + "\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Invalid HTTP method\"}");
        } catch (Exception e) {
            // Log error for debugging but don't expose details to client
            System.err.println("Error processing mock request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}");
        }
    }
}
