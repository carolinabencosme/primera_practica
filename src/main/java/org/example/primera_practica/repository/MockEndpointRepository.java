package org.example.primera_practica.repository;

import org.example.primera_practica.model.HttpMethod;
import org.example.primera_practica.model.MockEndpoint;
import org.example.primera_practica.model.Project;
import org.example.primera_practica.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockEndpointRepository extends JpaRepository<MockEndpoint, Long> {
    List<MockEndpoint> findByProject(Project project);
    Optional<MockEndpoint> findByProjectAndPathAndMethod(Project project, String path, HttpMethod method);
    List<MockEndpoint> findByCreatedBy(User user);
    Optional<MockEndpoint> findByProjectNameAndPathAndMethod(String projectName, String path, HttpMethod method);
}
