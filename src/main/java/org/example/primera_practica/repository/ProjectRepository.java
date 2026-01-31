package org.example.primera_practica.repository;

import org.example.primera_practica.model.Project;
import org.example.primera_practica.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
    List<Project> findByCreatedBy(User user);
    List<Project> findByCreatedByOrderByCreatedAtDesc(User user);
}
