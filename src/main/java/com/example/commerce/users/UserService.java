package com.example.commerce.users;

import com.example.commerce.users.dto.CompanyProjectResponseDTO;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.profilayarlama.entity.CompanyProject;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MinioClient minioClient;

    public List<CompanyProjectResponseDTO> getSirketProjeleri(String companyEmail) {
        User companyUser = userRepository.findByEmail(companyEmail)
                .orElseThrow(() -> new BusinessServiceException("Şirket bulunamadı", "company_not_found"));

        List<CompanyProjectResponseDTO> projectDTOs = new ArrayList<>();

        if (companyUser.getProjects() != null) {
            for (CompanyProject project : companyUser.getProjects()) {
                String fileUrl = null;

                try {
                    if (project.getFilePath() != null) {
                        fileUrl = minioClient.getPresignedObjectUrl(
                                GetPresignedObjectUrlArgs.builder()
                                        .method(Method.GET)
                                        .bucket("projects")
                                        .object(project.getFilePath())
                                        .expiry(60 * 60) // 1 saat geçerli
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    fileUrl = null; // Hata varsa null bırak
                }

                projectDTOs.add(new CompanyProjectResponseDTO(
                        project.getProjectName(),
                        project.getDescription(),
                        project.getStartDate(),
                        project.getEndDate(),
                        fileUrl
                ));
            }
        }

        return projectDTOs;
    }
}
