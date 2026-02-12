package org.example.primera_practica.service;

import org.example.primera_practica.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO, String username);
    ProjectDTO getProjectById(Long id);
    ProjectDTO getProjectByIdForUser(Long id, String username);
    List<ProjectDTO> getAllProjectsByUser(String username);
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    ProjectDTO updateProjectForUser(Long id, ProjectDTO projectDTO, String username);
    void deleteProject(Long id);
    void deleteProjectForUser(Long id, String username);
}
