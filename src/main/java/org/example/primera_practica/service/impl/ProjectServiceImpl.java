package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.ProjectDTO;
import org.example.primera_practica.exception.ResourceNotFoundException;
import org.example.primera_practica.model.Project;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.ProjectRepository;
import org.example.primera_practica.repository.UserRepository;
import org.example.primera_practica.service.ProjectService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setCreatedBy(user);

        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return convertToDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getProjectByIdForUser(Long id, String username) {
        Project project = findProjectById(id);
        validateProjectAccess(project, username);
        return convertToDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjectsByUser(String username) {
        User user = findUserByUsername(username);

        if (isAdmin(user)) {
            return projectRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        return projectRepository.findByCreatedByOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project project = findProjectById(id);

        if (projectDTO.getName() != null) {
            project.setName(projectDTO.getName());
        }
        if (projectDTO.getDescription() != null) {
            project.setDescription(projectDTO.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        return convertToDTO(updatedProject);
    }

    @Override
    public ProjectDTO updateProjectForUser(Long id, ProjectDTO projectDTO, String username) {
        Project project = findProjectById(id);
        validateProjectAccess(project, username);
        return updateProject(id, projectDTO);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = findProjectById(id);
        projectRepository.delete(project);
    }

    @Override
    public void deleteProjectForUser(Long id, String username) {
        Project project = findProjectById(id);
        validateProjectAccess(project, username);
        projectRepository.delete(project);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private void validateProjectAccess(Project project, String username) {
        User user = findUserByUsername(username);
        if (isAdmin(user)) {
            return;
        }

        String ownerUsername = project.getCreatedBy().getUsername();
        if (!ownerUsername.equals(username)) {
            throw new AccessDeniedException(
                    String.format("User %s is not authorized to access project %d", username, project.getId()));
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ROLE_ADMIN);
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedBy(project.getCreatedBy().getUsername());
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }
}
