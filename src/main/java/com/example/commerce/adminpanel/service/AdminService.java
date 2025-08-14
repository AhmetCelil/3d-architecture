package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.AdminSirketProjeEkleRequestDTO;
import com.example.commerce.adminpanel.dto.AdminSirketProjeEkleResponseDTO;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.profilayarlama.entity.CompanyProject;
import com.example.commerce.util.AppMessageUtil;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MinioClient minioClient;

    public AdminSirketProjeEkleResponseDTO adminSirketProjeEkle(AdminSirketProjeEkleRequestDTO requestDTO, MultipartFile file) {

        AdminSirketProjeEkleResponseDTO responseDTO = new AdminSirketProjeEkleResponseDTO();

        // Şirket kullanıcıyı bul
        User companyUser = userRepository.findByEmail(requestDTO.getCompanyEmail())
                .orElseThrow(() -> new BusinessServiceException("Şirket bulunamadı", "company_not_found"));

        // Proje entity oluştur
        CompanyProject project = CompanyProject.builder()
                .projectName(requestDTO.getProjectName())
                .description(requestDTO.getDescription())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .user(companyUser)
                .build();

        if (companyUser.getProjects() == null) {
            companyUser.setProjects(new ArrayList<>());
        }

        companyUser.getProjects().add(project);

        // Eğer dosya varsa MinIO’ya yükle
        if (file != null && !file.isEmpty()) {
            try {
                String bucketName = "projects";
                // Bucket yoksa oluştur
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                }

                String objectName = companyUser.getEmail() + "/" + file.getOriginalFilename();
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );

                // Projeye dosya yolu kaydet (opsiyonel)
                project.setFilePath(objectName);

            } catch (Exception e) {
                throw new RuntimeException("Dosya yüklenirken hata oluştu: " + e.getMessage());
            }
        }

        userRepository.save(companyUser);

        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode("MSG_PROJE_EKLEME_BASARILI", AppMessageType.SUCCESS)
        ));

        return responseDTO;
    }


}
