package com.example.commerce.profil.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.profil.dto.*;
import com.example.commerce.profil.entity.CompanyProject;
import com.example.commerce.profil.entity.ProjectFile;
import com.example.commerce.profil.entity.ProjectImage;
import com.example.commerce.profil.entity.UserProfile;
import com.example.commerce.profil.repository.CompanyProjectRepository;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl {

    // Message Constants
    private static final String MSG_SIFRE_DEGISTIRILDI = "profil.sifre.degistirildi";
    private static final String MSG_PROFIL_BILGISI_AYARLANDI = "profil.bilgisi.guncellendi";
    private static final String MSG_PROJE_EKLEME_BASARILI = "proje.ekleme.basarili";
    private static final String MSG_PROJE_GUNCELLEME_BASARILI = "proje.guncelleme.basarili";
    private static final String MSG_PROJE_SILME_BASARILI = "proje.silme.basarili";
    private static final String MSG_KULLANICI_BULUNAMADI = "kullanici.bulunamadi";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    private static final String MSG_YETKISIZ_ERISIM = "yetkisiz.erisim";
    private static final String MSG_SIFRE_UZUNLUK_HATASI = "sifre.uzunluk.hatasi";
    private static final String MSG_DOSYA_YUKLEME_HATASI = "dosya.yukleme.hatasi";

    private final UserRepository userRepository;
    private final CompanyProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Şirket şifresini günceller
     */
    @Transactional
    public SirketSifreGuncelleResponseDTO sirketSifreGuncelle(SirketSifreGuncelleRequestDTO requestDTO) {
        SirketSifreGuncelleResponseDTO responseDTO = new SirketSifreGuncelleResponseDTO();

        try {
            User user = getAuthenticatedUser();

            // Şifre validasyonu
            if (requestDTO.getYeniSirketParola() == null || requestDTO.getYeniSirketParola().isBlank()) {
                throw new ValidationServiceException(MSG_SIFRE_UZUNLUK_HATASI, "Şifre boş olamaz");
            }

            if (requestDTO.getYeniSirketParola().length() < 4 || requestDTO.getYeniSirketParola().length() > 40) {
                throw new ValidationServiceException(MSG_SIFRE_UZUNLUK_HATASI,
                        "Şifre uzunluğu 4 ile 40 karakter arasında olmalıdır");
            }

            String encodedPassword = passwordEncoder.encode(requestDTO.getYeniSirketParola());
            user.setPassword(encodedPassword);
            userRepository.save(user);

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_SIFRE_DEGISTIRILDI, AppMessageType.SUCCESS)
            ));

        } catch (ValidationServiceException | BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("HATA", "Beklenmeyen bir hata oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    /**
     * Şirket profil bilgilerini ayarlar veya günceller
     */
    @Transactional
    public SirketProfilAyarlaResponseDTO sirketProfilAyarla(SirketProfilAyarlaRequestDTO requestDTO) {
        SirketProfilAyarlaResponseDTO responseDTO = new SirketProfilAyarlaResponseDTO();

        try {
            User user = getAuthenticatedUser();

            UserProfile profile = user.getUserProfile();
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(user);
                user.setUserProfile(profile);
            }

            profile.setFirstName(requestDTO.getFirstName());
            profile.setLastName(requestDTO.getLastName());
            profile.setPhoneNumber(requestDTO.getPhoneNumber());
            profile.setAddress(requestDTO.getAddress());
            profile.setProfilePicture(requestDTO.getProfilePicture());
            profile.setDescription(requestDTO.getDescription());

            userRepository.save(user);

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROFIL_BILGISI_AYARLANDI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    /**
     * Yeni şirket projesi ekler (tüm alanlarla)
     */
    @Transactional
    public SirketProjeAyarlaResponseDTO sirketProjesiAyarla(SirketProjeAyarlaRequestDTO requestDTO) {
        SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();

        try {
            User user = getAuthenticatedUser();

            // Zorunlu alan validasyonları
            if (requestDTO.getProjectName() == null || requestDTO.getProjectName().isBlank()) {
                throw new ValidationServiceException("PROJE_ADI_BOS", "Proje adı boş olamaz");
            }
            if (requestDTO.getCategory() == null) {
                throw new ValidationServiceException("KATEGORI_BOS", "Kategori boş olamaz");
            }
            if (requestDTO.getStatus() == null) {
                throw new ValidationServiceException("DURUM_BOS", "Durum boş olamaz");
            }
            if (requestDTO.getLocation() == null || requestDTO.getLocation().isBlank()) {
                throw new ValidationServiceException("LOKASYON_BOS", "Lokasyon boş olamaz");
            }

            // Proje oluştur
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
                    .features(requestDTO.getFeatures() != null ? requestDTO.getFeatures() : new ArrayList<>())
                    .user(user)
                    .build();

            // Görselleri ekle
            if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
                List<ProjectImage> images = new ArrayList<>();
                for (MultipartFile file : requestDTO.getImages()) {
                    ProjectImage image = createProjectImage(file, project);
                    images.add(image);
                }
                project.setImages(images);
            }

            // Kat planlarını ekle
            if (requestDTO.getFloorPlans() != null && !requestDTO.getFloorPlans().isEmpty()) {
                List<ProjectFile> floorPlans = new ArrayList<>();
                for (MultipartFile file : requestDTO.getFloorPlans()) {
                    ProjectFile floorPlan = createProjectFile(file, project);
                    floorPlans.add(floorPlan);
                }
                project.setFloorPlans(floorPlans);
            }

            // Kullanıcıya proje ekle
            if (user.getProjects() == null) {
                user.setProjects(new ArrayList<>());
            }
            user.getProjects().add(project);
            userRepository.save(user);

            responseDTO.setProjectId(project.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_EKLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (IOException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(MSG_DOSYA_YUKLEME_HATASI, "Dosya yüklenirken hata oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    /**
     * Mevcut projeyi günceller
     */
    @Transactional
    public SirketProjeGuncelleResponseDTO sirketProjesiGuncelle(Long projectId, SirketProjeGuncelleRequestDTO requestDTO) {
        SirketProjeGuncelleResponseDTO responseDTO = new SirketProjeGuncelleResponseDTO();

        try {
            User user = getAuthenticatedUser();

            CompanyProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı"));

            // Yetki kontrolü
            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi güncelleme yetkiniz yok");
            }

            // Proje bilgilerini güncelle
            if (requestDTO.getProjectName() != null) {
                project.setProjectName(requestDTO.getProjectName());
            }
            if (requestDTO.getCategory() != null) {
                project.setCategory(requestDTO.getCategory());
            }
            if (requestDTO.getLocation() != null) {
                project.setLocation(requestDTO.getLocation());
            }
            if (requestDTO.getTotalArea() != null) {
                project.setTotalArea(requestDTO.getTotalArea());
            }
            if (requestDTO.getStartDate() != null) {
                project.setStartDate(requestDTO.getStartDate());
            }
            if (requestDTO.getEndDate() != null) {
                project.setEndDate(requestDTO.getEndDate());
            }
            if (requestDTO.getStatus() != null) {
                project.setStatus(requestDTO.getStatus());
            }
            if (requestDTO.getDurationMonths() != null) {
                project.setDurationMonths(requestDTO.getDurationMonths());
            }
            if (requestDTO.getDescription() != null) {
                project.setDescription(requestDTO.getDescription());
            }
            if (requestDTO.getTechnicalSpecifications() != null) {
                project.setTechnicalSpecifications(requestDTO.getTechnicalSpecifications());
            }
            if (requestDTO.getFeatures() != null) {
                project.setFeatures(requestDTO.getFeatures());
            }

            // Yeni görseller ekle
            if (requestDTO.getNewImages() != null && !requestDTO.getNewImages().isEmpty()) {
                if (project.getImages() == null) {
                    project.setImages(new ArrayList<>());
                }
                for (MultipartFile file : requestDTO.getNewImages()) {
                    ProjectImage image = createProjectImage(file, project);
                    project.getImages().add(image);
                }
            }

            // Yeni kat planları ekle
            if (requestDTO.getNewFloorPlans() != null && !requestDTO.getNewFloorPlans().isEmpty()) {
                if (project.getFloorPlans() == null) {
                    project.setFloorPlans(new ArrayList<>());
                }
                for (MultipartFile file : requestDTO.getNewFloorPlans()) {
                    ProjectFile floorPlan = createProjectFile(file, project);
                    project.getFloorPlans().add(floorPlan);
                }
            }

            projectRepository.save(project);

            responseDTO.setProjectId(project.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (IOException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(MSG_DOSYA_YUKLEME_HATASI, "Dosya yüklenirken hata oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    /**
     * Projeyi siler
     */
    @Transactional
    public SirketProjeSilResponseDTO sirketProjesiSil(Long projectId) {
        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();

        try {
            User user = getAuthenticatedUser();

            CompanyProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı"));

            // Yetki kontrolü
            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi silme yetkiniz yok");
            }

            projectRepository.delete(project);

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_SILME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    /**
     * Kullanıcının tüm projelerini listeler
     */
    public SirketProjelerListeleResponseDTO sirketProjeleriniListele() {
        SirketProjelerListeleResponseDTO responseDTO = new SirketProjelerListeleResponseDTO();

        try {
            User user = getAuthenticatedUser();

            List<CompanyProject> projects = projectRepository.findByUserId(user.getId());

            List<ProjeDetayDTO> projeDetaylar = projects.stream()
                    .map(this::convertToProjeDetayDTO)
                    .collect(Collectors.toList());

            responseDTO.setProjeler(projeDetaylar);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("LISTE_BASARILI", AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getCause().toString(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ===== HELPER METHODS =====

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Kullanıcı doğrulanmamış");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException(MSG_KULLANICI_BULUNAMADI, "Kullanıcı bulunamadı"));
    }

    private ProjectImage createProjectImage(MultipartFile file, CompanyProject project) throws IOException {
        return ProjectImage.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .imageData(file.getBytes())
                .project(project)
                .build();
    }

    private ProjectFile createProjectFile(MultipartFile file, CompanyProject project) throws IOException {
        return ProjectFile.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileData(file.getBytes())
                .project(project)
                .build();
    }

    private ProjeDetayDTO convertToProjeDetayDTO(CompanyProject project) {
        return ProjeDetayDTO.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .category(project.getCategory())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .durationMonths(project.getDurationMonths())
                .description(project.getDescription())
                .technicalSpecifications(project.getTechnicalSpecifications())
                .features(project.getFeatures())
                .imageCount(project.getImages() != null ? project.getImages().size() : 0)
                .floorPlanCount(project.getFloorPlans() != null ? project.getFloorPlans().size() : 0)
                .build();
    }
}