package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.util.AppMessageUtil;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjeAyarlariService {

    private final UserRepository userRepository;
    private final CompanyProjectRepository projectRepository;

    @Transactional
    public SirketProjeAyarlaResponseDTO projeEkle(SirketProjeAyarlaRequestDTO requestDTO) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            CompanyProject project = CompanyProject.builder()
                    .projectName(requestDTO.getProjectName())
                    .category(requestDTO.getCategory())
                    .location(requestDTO.getLocation())
                    .totalArea(requestDTO.getTotalArea())
                    .startDate(requestDTO.getStartDate())
                    .endDate(requestDTO.getEndDate())
                    .status(requestDTO.getStatus())
                    .durationMonths(requestDTO.getDurationMonths())
                    .description(requestDTO.getDescription())
                    .technicalSpecifications(requestDTO.getTechnicalSpecifications())
                    .features(requestDTO.getFeatures())
                    .user(authenticatedUser) // Projeyi ekleyen kullanıcı
                    .build();

            CompanyProject savedProject = projectRepository.save(project);

            SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();


            log.info("Yeni proje eklendi: {} (ID: {})", savedProject.getProjectName(), savedProject.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("Proje kaydedildi", AppMessageType.SUCCESS)
            ));
            return responseDTO;

        } catch (Exception e) {
            log.error("Proje eklenirken hata oluştu: {}", e.getMessage());
            throw new BusinessServiceException("PROJE_KAYIT_HATASI", "Proje kaydedilemedi");
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessServiceException("YETKISIZ_ERISIM", "Kullanıcı doğrulanmamış");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException("KULLANICI_BULUNAMADI", "Kullanıcı bulunamadı"));
    }

    @Transactional(readOnly = true)
    public SirketProjelerListeleResponseDTO projeleriListele() {
        User authenticatedUser = getAuthenticatedUser();

        // Bu kullanıcıya ait tüm projeleri getir
        List<CompanyProject> projects = projectRepository.findByUserAndDeletedFalse(authenticatedUser);

        SirketProjelerListeleResponseDTO responseDTO = new SirketProjelerListeleResponseDTO();

        // Eğer hiç proje yoksa bilgi mesajı dön
        if (projects.isEmpty()) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("Henüz proje eklenmemiş", AppMessageType.INFO)
            ));
            return responseDTO;
        }

        List<ProjeListDataResponseDTO> projeListesi = projects.stream().map(project -> {
            ProjeListDataResponseDTO data = new ProjeListDataResponseDTO();
            data.setId(project.getId());
            data.setProjectName(project.getProjectName());
            data.setCategory(project.getCategory());
            data.setLocation(project.getLocation());
            data.setTotalArea(project.getTotalArea());
            data.setStartDate(project.getStartDate());
            data.setEndDate(project.getEndDate());
            data.setStatus(project.getStatus());
            data.setDurationMonths(project.getDurationMonths());
            data.setDescription(project.getDescription());
            data.setTechnicalSpecifications(project.getTechnicalSpecifications());
            data.setFeatures(project.getFeatures());
            return data;
        }).toList();

       responseDTO.setData(projeListesi);
        /*responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode("Projeler başarıyla listelendi", AppMessageType.SUCCESS)
        ));*/

        return responseDTO;
    }

    @Transactional
    public SirketProjeSilResponseDTO projeSoftDelete(Long projectId) {
        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();
        User user = getAuthenticatedUser();
        CompanyProject project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı veya zaten silinmiş"));

        if (!project.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu proje üzerinde yetkiniz yok");
        }

        project.setDeleted(true);
        projectRepository.save(project);
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode("proje başarıyla silindi", AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }
}