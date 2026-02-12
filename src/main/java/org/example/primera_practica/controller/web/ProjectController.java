package org.example.primera_practica.controller.web;

import jakarta.validation.Valid;
import org.example.primera_practica.dto.ProjectDTO;
import org.example.primera_practica.service.ProjectService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String listProjects(Model model, Authentication authentication) {
        model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
        return "projects/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new ProjectDTO());
        return "projects/form";
    }

    @PostMapping
    public String createProject(@Valid @ModelAttribute("project") ProjectDTO projectDTO,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "projects/form";
        }

        try {
            ProjectDTO created = projectService.createProject(projectDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Project created successfully!");
            return "redirect:/projects/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating project: " + e.getMessage());
            return "redirect:/projects";
        }
    }

    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("project", projectService.getProjectByIdForUser(id, authentication.getName()));
            return "projects/view";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied: you cannot access this project.");
            return "redirect:/projects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
            return "redirect:/projects";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("project", projectService.getProjectByIdForUser(id, authentication.getName()));
            return "projects/form";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied: you cannot edit this project.");
            return "redirect:/projects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
            return "redirect:/projects";
        }
    }

    @PostMapping("/{id}")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("project") ProjectDTO projectDTO,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "projects/form";
        }

        try {
            projectService.updateProjectForUser(id, projectDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully!");
            return "redirect:/projects/" + id;
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied: you cannot update this project.");
            return "redirect:/projects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating project: " + e.getMessage());
            return "redirect:/projects/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteProjectForUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully!");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied: you cannot delete this project.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting project: " + e.getMessage());
        }
        return "redirect:/projects";
    }
}
