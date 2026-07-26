package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.Audiogram;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AudiogramRepository extends JpaRepository<Audiogram, Long> {
    List<Audiogram> findByConsultationIdOrderByCreatedAtDesc(Long consultationId);
    List<Audiogram> findByProfessionalIdOrderByCreatedAtDesc(Long professionalId);
}
