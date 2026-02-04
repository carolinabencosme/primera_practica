package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.dto.MockHeaderDTO;
import org.example.primera_practica.exception.ResourceNotFoundException;
import org.example.primera_practica.model.*;
import org.example.primera_practica.repository.MockEndpointRepository;
import org.example.primera_practica.repository.ProjectRepository;
import org.example.primera_practica.repository.UserRepository;
import org.example.primera_practica.service.JwtService;
import org.example.primera_practica.service.MockEndpointService;
import org.example.primera_practica.util.PathNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MockEndpointServiceImpl implements MockEndpointService {

    private final MockEndpointRepository mockEndpointRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public MockEndpointServiceImpl(MockEndpointRepository mockEndpointRepository, 
                                   ProjectRepository projectRepository, 
                                   UserRepository userRepository,
                                   JwtService jwtService) {
        this.mockEndpointRepository = mockEndpointRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public MockEndpointDTO createMockEndpoint(MockEndpointDTO mockEndpointDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Project project = projectRepository.findById(mockEndpointDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + mockEndpointDTO.getProjectId()));

        MockEndpoint mockEndpoint = new MockEndpoint();
        mockEndpoint.setName(mockEndpointDTO.getName());
        mockEndpoint.setDescription(mockEndpointDTO.getDescription());
        mockEndpoint.setPath(PathNormalizer.normalizePath(mockEndpointDTO.getPath()));
        mockEndpoint.setMethod(mockEndpointDTO.getMethod());
        mockEndpoint.setHttpStatusCode(mockEndpointDTO.getHttpStatusCode());
        mockEndpoint.setContentType(mockEndpointDTO.getContentType());
        mockEndpoint.setResponseBody(mockEndpointDTO.getResponseBody());
        MockExpirationOption expirationOption = resolveExpirationOptionOrDefault(mockEndpointDTO.getExpirationOption());
        LocalDateTime expirationDate = LocalDateTime.now().plus(expirationOption.getDuration());
        mockEndpoint.setExpirationDate(expirationDate);
        mockEndpoint.setDelaySeconds(mockEndpointDTO.getDelaySeconds());
        mockEndpoint.setRequiresJwt(mockEndpointDTO.getRequiresJwt());
        mockEndpoint.setCreatedBy(user);
        mockEndpoint.setProject(project);

        if (Boolean.TRUE.equals(mockEndpointDTO.getRequiresJwt())) {
            mockEndpoint.setGeneratedJwt(jwtService.generateToken(user.getUsername(), expirationDate));
        }

        if (mockEndpointDTO.getHeaders() != null && !mockEndpointDTO.getHeaders().isEmpty()) {
            List<MockHeader> headers = mockEndpointDTO.getHeaders().stream()
                    .map(headerDTO -> {
                        MockHeader header = new MockHeader();
                        header.setHeaderKey(headerDTO.getHeaderKey());
                        header.setHeaderValue(headerDTO.getHeaderValue());
                        header.setMockEndpoint(mockEndpoint);
                        return header;
                    })
                    .collect(Collectors.toList());
            mockEndpoint.setHeaders(headers);
        }

        MockEndpoint savedMockEndpoint = mockEndpointRepository.save(mockEndpoint);
        return convertToDTO(savedMockEndpoint);
    }

    @Override
    @Transactional(readOnly = true)
    public MockEndpointDTO getMockEndpointById(Long id) {
        MockEndpoint mockEndpoint = mockEndpointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MockEndpoint not found with id: " + id));
        return convertToDTO(mockEndpoint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MockEndpointDTO> getAllMockEndpointsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        return mockEndpointRepository.findByProject(project).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MockEndpointDTO updateMockEndpoint(Long id, MockEndpointDTO mockEndpointDTO) {
        MockEndpoint mockEndpoint = mockEndpointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MockEndpoint not found with id: " + id));

        boolean shouldRegenerateToken = false;

        if (mockEndpointDTO.getName() != null) {
            mockEndpoint.setName(mockEndpointDTO.getName());
        }
        if (mockEndpointDTO.getDescription() != null) {
            mockEndpoint.setDescription(mockEndpointDTO.getDescription());
        }
        if (mockEndpointDTO.getPath() != null) {
            mockEndpoint.setPath(PathNormalizer.normalizePath(mockEndpointDTO.getPath()));
        }
        if (mockEndpointDTO.getMethod() != null) {
            mockEndpoint.setMethod(mockEndpointDTO.getMethod());
        }
        if (mockEndpointDTO.getHttpStatusCode() != null) {
            mockEndpoint.setHttpStatusCode(mockEndpointDTO.getHttpStatusCode());
        }
        if (mockEndpointDTO.getContentType() != null) {
            mockEndpoint.setContentType(mockEndpointDTO.getContentType());
        }
        if (mockEndpointDTO.getResponseBody() != null) {
            mockEndpoint.setResponseBody(mockEndpointDTO.getResponseBody());
        }
        MockExpirationOption expirationOption = resolveExpirationOption(mockEndpointDTO.getExpirationOption());
        if (expirationOption != null) {
            LocalDateTime expirationDate = LocalDateTime.now().plus(expirationOption.getDuration());
            if (!expirationDate.equals(mockEndpoint.getExpirationDate())) {
                shouldRegenerateToken = true;
            }
            mockEndpoint.setExpirationDate(expirationDate);
        }
        if (mockEndpointDTO.getDelaySeconds() != null) {
            mockEndpoint.setDelaySeconds(mockEndpointDTO.getDelaySeconds());
        }
        if (mockEndpointDTO.getRequiresJwt() != null) {
            if (!mockEndpointDTO.getRequiresJwt().equals(mockEndpoint.getRequiresJwt())) {
                shouldRegenerateToken = true;
            }
            mockEndpoint.setRequiresJwt(mockEndpointDTO.getRequiresJwt());
        }

        if (mockEndpointDTO.getHeaders() != null) {
            mockEndpoint.getHeaders().clear();
            List<MockHeader> headers = mockEndpointDTO.getHeaders().stream()
                    .map(headerDTO -> {
                        MockHeader header = new MockHeader();
                        header.setHeaderKey(headerDTO.getHeaderKey());
                        header.setHeaderValue(headerDTO.getHeaderValue());
                        header.setMockEndpoint(mockEndpoint);
                        return header;
                    })
                    .collect(Collectors.toList());
            mockEndpoint.getHeaders().addAll(headers);
        }

        if (Boolean.TRUE.equals(mockEndpoint.getRequiresJwt())) {
            if (shouldRegenerateToken || mockEndpoint.getGeneratedJwt() == null) {
                mockEndpoint.setGeneratedJwt(jwtService.generateToken(
                        mockEndpoint.getCreatedBy().getUsername(),
                        mockEndpoint.getExpirationDate()));
            }
        } else {
            mockEndpoint.setGeneratedJwt(null);
        }

        MockEndpoint updatedMockEndpoint = mockEndpointRepository.save(mockEndpoint);
        return convertToDTO(updatedMockEndpoint);
    }

    @Override
    public void deleteMockEndpoint(Long id) {
        MockEndpoint mockEndpoint = mockEndpointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MockEndpoint not found with id: " + id));
        mockEndpointRepository.delete(mockEndpoint);
    }

    @Override
    @Transactional(readOnly = true)
    public MockEndpointDTO findMockByProjectAndPathAndMethod(String projectName, String path, HttpMethod method) {
        MockEndpoint mockEndpoint = mockEndpointRepository.findByProjectNameAndPathAndMethod(projectName, path, method)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("MockEndpoint not found with projectName: %s, path: %s, method: %s", 
                                projectName, path, method)));
        return convertToDTO(mockEndpoint);
    }

    private MockEndpointDTO convertToDTO(MockEndpoint mockEndpoint) {
        MockEndpointDTO dto = new MockEndpointDTO();
        dto.setId(mockEndpoint.getId());
        dto.setName(mockEndpoint.getName());
        dto.setDescription(mockEndpoint.getDescription());
        dto.setPath(mockEndpoint.getPath());
        dto.setMethod(mockEndpoint.getMethod());
        dto.setHttpStatusCode(mockEndpoint.getHttpStatusCode());
        dto.setContentType(mockEndpoint.getContentType());
        dto.setResponseBody(mockEndpoint.getResponseBody());
        dto.setExpirationDate(mockEndpoint.getExpirationDate());
        dto.setDelaySeconds(mockEndpoint.getDelaySeconds());
        dto.setRequiresJwt(mockEndpoint.getRequiresJwt());
        dto.setGeneratedJwt(mockEndpoint.getGeneratedJwt());
        dto.setCreatedBy(mockEndpoint.getCreatedBy().getUsername());
        dto.setProjectId(mockEndpoint.getProject().getId());
        dto.setProjectName(mockEndpoint.getProject().getName());
        dto.setCreatedAt(mockEndpoint.getCreatedAt());

        if (mockEndpoint.getHeaders() != null && !mockEndpoint.getHeaders().isEmpty()) {
            List<MockHeaderDTO> headerDTOs = mockEndpoint.getHeaders().stream()
                    .map(header -> {
                        MockHeaderDTO headerDTO = new MockHeaderDTO();
                        headerDTO.setId(header.getId());
                        headerDTO.setHeaderKey(header.getHeaderKey());
                        headerDTO.setHeaderValue(header.getHeaderValue());
                        return headerDTO;
                    })
                    .collect(Collectors.toList());
            dto.setHeaders(headerDTOs);
        }

        return dto;
    }

    private MockExpirationOption resolveExpirationOptionOrDefault(String expirationOption) {
        if (expirationOption == null || expirationOption.isBlank()) {
            return MockExpirationOption.ONE_YEAR;
        }
        return MockExpirationOption.fromValue(expirationOption)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported expiration option: " + expirationOption));
    }

    private MockExpirationOption resolveExpirationOption(String expirationOption) {
        if (expirationOption == null || expirationOption.isBlank()) {
            return null;
        }
        return MockExpirationOption.fromValue(expirationOption)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported expiration option: " + expirationOption));
    }
}
