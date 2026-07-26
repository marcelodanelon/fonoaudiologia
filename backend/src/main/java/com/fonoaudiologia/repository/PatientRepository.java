package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR p.cpf LIKE CONCAT('%', :search, '%') OR p.phone LIKE CONCAT('%', :search, '%')")
    List<Patient> search(String search);

    long countByActiveTrue();

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt >= :date")
    long countCreatedAfter(java.time.LocalDateTime date);
}
