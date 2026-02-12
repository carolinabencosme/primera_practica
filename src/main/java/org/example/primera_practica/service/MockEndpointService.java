package org.example.primera_practica.service;

import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.model.HttpMethod;

import java.util.List;

public interface MockEndpointService {
    MockEndpointDTO createMockEndpoint(MockEndpointDTO mockEndpointDTO, String username);
    MockEndpointDTO getMockEndpointById(Long id);
    MockEndpointDTO getMockEndpointByIdForUser(Long id, String username);
    List<MockEndpointDTO> getAllMockEndpointsByProject(Long projectId);
    List<MockEndpointDTO> getAllMockEndpointsByProjectForUser(Long projectId, String username);
    MockEndpointDTO updateMockEndpoint(Long id, MockEndpointDTO mockEndpointDTO);
    MockEndpointDTO updateMockEndpointForUser(Long id, MockEndpointDTO mockEndpointDTO, String username);
    void deleteMockEndpoint(Long id);
    void deleteMockEndpointForUser(Long id, String username);
    MockEndpointDTO findMockByProjectAndPathAndMethod(String projectName, String path, HttpMethod method);
}
