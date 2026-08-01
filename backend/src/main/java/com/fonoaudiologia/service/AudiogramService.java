package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.AudiogramRequest;
import com.fonoaudiologia.entity.Audiogram;
import com.fonoaudiologia.entity.Consultation;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AudiogramRepository;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AudiogramService {

    private final AudiogramRepository audiogramRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;

    public AudiogramService(AudiogramRepository audiogramRepository,
                            ConsultationRepository consultationRepository,
                            UserRepository userRepository) {
        this.audiogramRepository = audiogramRepository;
        this.consultationRepository = consultationRepository;
        this.userRepository = userRepository;
    }

    public List<Audiogram> findByConsultation(Long consultationId) {
        return audiogramRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId);
    }

    public java.util.Optional<Audiogram> findByConsultationId(Long consultationId) {
        List<Audiogram> list = audiogramRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    public Audiogram findById(Long id) {
        return audiogramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audiograma não encontrado"));
    }

    public Audiogram create(AudiogramRequest request, Long professionalId) {
        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        Audiogram audiogram = new Audiogram();
        audiogram.setConsultation(consultation);
        audiogram.setProfessional(professional);
        mapRequestToEntity(request, audiogram);

        return audiogramRepository.save(audiogram);
    }

    public Audiogram update(Long id, AudiogramRequest request) {
        Audiogram audiogram = audiogramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audiograma não encontrado"));
        mapRequestToEntity(request, audiogram);
        return audiogramRepository.save(audiogram);
    }

    private void mapRequestToEntity(AudiogramRequest request, Audiogram audiogram) {
        audiogram.setRight250(request.getRight250());
        audiogram.setRight500(request.getRight500());
        audiogram.setRight1000(request.getRight1000());
        audiogram.setRight2000(request.getRight2000());
        audiogram.setRight3000(request.getRight3000());
        audiogram.setRight4000(request.getRight4000());
        audiogram.setRight6000(request.getRight6000());
        audiogram.setRight8000(request.getRight8000());
        audiogram.setLeft250(request.getLeft250());
        audiogram.setLeft500(request.getLeft500());
        audiogram.setLeft1000(request.getLeft1000());
        audiogram.setLeft2000(request.getLeft2000());
        audiogram.setLeft3000(request.getLeft3000());
        audiogram.setLeft4000(request.getLeft4000());
        audiogram.setLeft6000(request.getLeft6000());
        audiogram.setLeft8000(request.getLeft8000());
        audiogram.setHearingLossType(request.getHearingLossType());
        audiogram.setObservations(request.getObservations());
    }
}
