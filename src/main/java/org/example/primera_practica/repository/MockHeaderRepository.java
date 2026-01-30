package org.example.primera_practica.repository;

import org.example.primera_practica.model.MockEndpoint;
import org.example.primera_practica.model.MockHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockHeaderRepository extends JpaRepository<MockHeader, Long> {
    List<MockHeader> findByMockEndpoint(MockEndpoint mockEndpoint);
}
