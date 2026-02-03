package org.example.primera_practica.controller.web;

import jakarta.validation.Valid;
import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.model.HttpMethod;
import org.example.primera_practica.service.MockEndpointService;
import org.example.primera_practica.service.ProjectService;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/mocks")
public class MockEndpointController {

    private final MockEndpointService mockEndpointService;
    private final ProjectService projectService;
    private final Environment environment;

    public MockEndpointController(MockEndpointService mockEndpointService, ProjectService projectService, Environment environment) {
        this.mockEndpointService = mockEndpointService;
        this.projectService = projectService;
        this.environment = environment;
    }

    @GetMapping
    public String listMocks(@RequestParam(required = false) Long projectId,
                           Model model,
                           Authentication authentication) {
        if (projectId != null) {
            model.addAttribute("mocks", mockEndpointService.getAllMockEndpointsByProject(projectId));
            model.addAttribute("projectId", projectId);
        } else {
            model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
        }
        return "mocks/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) Long projectId,
                                 Model model,
                                 Authentication authentication) {
        MockEndpointDTO mockEndpoint = new MockEndpointDTO();
        if (projectId != null) {
            mockEndpoint.setProjectId(projectId);
        }
        model.addAttribute("mock", mockEndpoint);
        model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
        model.addAttribute("httpMethods", HttpMethod.values());
        return "mocks/form";
    }

    @PostMapping
    public String createMock(@Valid @ModelAttribute("mock") MockEndpointDTO mockEndpointDTO,
                            BindingResult result,
                            Authentication authentication,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
            model.addAttribute("httpMethods", HttpMethod.values());
            return "mocks/form";
        }
        
        try {
            MockEndpointDTO created = mockEndpointService.createMockEndpoint(mockEndpointDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Mock endpoint created successfully!");
            return "redirect:/mocks/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating mock: " + e.getMessage());
            return "redirect:/mocks";
        }
    }

    @GetMapping("/{id}")
    public String viewMock(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("mock", mockEndpointService.getMockEndpointById(id));
            model.addAttribute("isDevOrTest", isDevOrTest());
            return "mocks/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mock endpoint not found");
            return "redirect:/mocks";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("mock", mockEndpointService.getMockEndpointById(id));
            model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
            model.addAttribute("httpMethods", HttpMethod.values());
            return "mocks/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mock endpoint not found");
            return "redirect:/mocks";
        }
    }

    @PostMapping("/{id}")
    public String updateMock(@PathVariable Long id,
                            @Valid @ModelAttribute("mock") MockEndpointDTO mockEndpointDTO,
                            BindingResult result,
                            Authentication authentication,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("projects", projectService.getAllProjectsByUser(authentication.getName()));
            model.addAttribute("httpMethods", HttpMethod.values());
            return "mocks/form";
        }
        
        try {
            mockEndpointService.updateMockEndpoint(id, mockEndpointDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Mock endpoint updated successfully!");
            return "redirect:/mocks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating mock: " + e.getMessage());
            return "redirect:/mocks/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mockEndpointService.deleteMockEndpoint(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mock endpoint deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting mock: " + e.getMessage());
        }
        return "redirect:/mocks";
    }

    private boolean isDevOrTest() {
        return Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("test"));
    }
}
