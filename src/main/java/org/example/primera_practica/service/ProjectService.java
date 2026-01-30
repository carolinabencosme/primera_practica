package org.example.primera_practica.service;

import org.example.primera_practica.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO, String username);
    ProjectDTO getProjectById(Long id);
    List<ProjectDTO> getAllProjectsByUser(String username);
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    void deleteProject(Long id);
}
