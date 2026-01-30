package org.example.primera_practica.service;

import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.model.HttpMethod;

import java.util.List;

public interface MockEndpointService {
    MockEndpointDTO createMockEndpoint(MockEndpointDTO mockEndpointDTO, String username);
    MockEndpointDTO getMockEndpointById(Long id);
    List<MockEndpointDTO> getAllMockEndpointsByProject(Long projectId);
    MockEndpointDTO updateMockEndpoint(Long id, MockEndpointDTO mockEndpointDTO);
    void deleteMockEndpoint(Long id);
    MockEndpointDTO findMockByProjectAndPathAndMethod(String projectName, String path, HttpMethod method);
}
