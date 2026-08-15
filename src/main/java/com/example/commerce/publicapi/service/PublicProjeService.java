package com.example.commerce.publicapi.service;

import com.example.commerce.adminpanel.dto.FloorPlanRoomDetailDTO;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.contract.entity.Contract;
import com.example.commerce.contract.repository.ContractRepository;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.publicapi.dto.*;
import com.example.commerce.resource.entity.SubcontractorAssignment;
import com.example.commerce.resource.repository.SubcontractorAssignmentRepository;
import com.example.commerce.schedule.entity.Milestone;
import com.example.commerce.schedule.entity.ProjectTask;
import com.example.commerce.schedule.repository.MilestoneRepository;
import com.example.commerce.schedule.repository.ProjectTaskRepository;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.repository.CompanyRepository;
import com.example.commerce.tenant.service.ApiKeyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicProjeService {

    /** Tek seferde döndürülebilecek dosya için makul bir üst sınır (50MB). */
    private static final long MAX_DOWNLOADABLE_FILE_SIZE = 50L * 1024 * 1024;
    private static final int MAX_PAGE_SIZE = 100;

    private final CompanyRepository companyRepository;
    private final CompanyProjectRepository projectRepository;
    private final ApiKeyService apiKeyService;
    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final SubcontractorAssignmentRepository subcontractorAssignmentRepository;

    private Company getCompanyByApiKey(String apiKey) {
        return companyRepository.findByApiKeyHashAndActiveTrue(apiKeyService.hash(apiKey))
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API key"));
    }

    @Transactional
    public PublicProjelerResponseDTO projeleriGetir(String apiKey, PublicProjeleriGetirRequestDTO request) {
        Company company = getCompanyByApiKey(apiKey);

        Pageable pageable = request.toPageable(MAX_PAGE_SIZE);
        Page<CompanyProject> projects = projectRepository.findByCompanyAndDeletedFalse(company, pageable);

        PublicProjelerResponseDTO responseDTO = new PublicProjelerResponseDTO();
        responseDTO.loadFrom(projects, this::convertToPublicProjeDTO);
        return responseDTO;
    }

    @Transactional
    public PublicProjeDetayResponseDTO projeDetayGetir(String apiKey, Long projeId) {
        Company company = getCompanyByApiKey(apiKey);

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI", "Proje bulunamadı"));

        PublicProjeDetayResponseDTO responseDTO = new PublicProjeDetayResponseDTO();
        responseDTO.setData(convertToPublicProjeDetayDTO(project, true));
        return responseDTO;
    }

    /** Herkese açık: unique-code ile erişimde finansal/kişisel veri döndürmez. */
    @Transactional
    public PublicProjeDetayResponseDTO projeDetayGetirByCode(String uniqueCode) {
        CompanyProject project = projectRepository.findByUniqueCodeAndDeletedFalse(uniqueCode)
                .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI", "Proje bulunamadı"));

        PublicProjeDetayResponseDTO responseDTO = new PublicProjeDetayResponseDTO();
        responseDTO.setData(convertToPublicProjeDetayDTO(project, false));
        return responseDTO;
    }

    public record DownloadableFile(String fileName, String fileType, byte[] fileData) {}

    @Transactional
    public DownloadableFile dosyaIndir(String apiKey, Long projeId, Long dosyaId) {
        Company company = getCompanyByApiKey(apiKey);

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI", "Proje bulunamadı"));

        ProjectFile file = project.getFiles().stream()
                .filter(f -> f.getId().equals(dosyaId) && !f.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessServiceException("DOSYA_BULUNAMADI", "Dosya bulunamadı"));

        if (file.getFileData() != null && file.getFileData().length > MAX_DOWNLOADABLE_FILE_SIZE) {
            throw new BusinessServiceException("DOSYA_COK_BUYUK", "Dosya bu uç nokta üzerinden indirilemeyecek kadar büyük");
        }

        return new DownloadableFile(file.getFileName(), file.getFileType(), file.getFileData());
    }

    // Helpers
    private PublicProjeDTO convertToPublicProjeDTO(CompanyProject project) {
        return PublicProjeDTO.builder()
                .id(project.getId())
                .uniqueCode(project.getUniqueCode())
                .projectName(project.getProjectName())
                .category(project.getCategory().name())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .description(project.getDescription())
                .dosyaSayisi((int) project.getFiles().stream().filter(f -> !f.isDeleted()).count())
                .build();
    }

    private PublicProjeDetayDTO convertToPublicProjeDetayDTO(CompanyProject project, boolean includeSensitiveData) {
        return PublicProjeDetayDTO.builder()
                .id(project.getId())
                .uniqueCode(project.getUniqueCode())
                .projectName(project.getProjectName())
                .category(project.getCategory().name())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .durationMonths(project.getDurationMonths())
                .description(project.getDescription())
                .technicalSpecifications(project.getTechnicalSpecifications())
                .features(project.getFeatures())
                .files(project.getFiles().stream()
                        .filter(f -> !f.isDeleted())
                        .map(f -> PublicDosyaMetaDTO.builder()
                                .id(f.getId())
                                .fileName(f.getFileName())
                                .fileType(f.getFileType())
                                .fileSize(f.getFileSize())
                                .title(f.getTitle())
                                .fileCategory(f.getFileCategory() != null ? f.getFileCategory().name() : null)
                                .roomDetails(f.getRoomDetails().stream()
                                        .map(rd -> FloorPlanRoomDetailDTO.builder()
                                                .roomName(rd.getRoomName())
                                                .value(rd.getValue())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .schedule(buildSchedule(project))
                .team(buildTeam(project, includeSensitiveData))
                .contracts(includeSensitiveData ? buildContracts(project) : List.of())
                .build();
    }

    private List<PublicScheduleItemDTO> buildSchedule(CompanyProject project) {
        List<PublicScheduleItemDTO> schedule = new ArrayList<>();

        for (Milestone m : milestoneRepository.findByProject(project)) {
            schedule.add(PublicScheduleItemDTO.builder()
                    .name(m.getName())
                    .plannedDate(m.getPlannedDate())
                    .actualDate(m.getActualDate())
                    .status(m.getStatus().name())
                    .build());
        }

        for (ProjectTask t : projectTaskRepository.findByProject(project)) {
            schedule.add(PublicScheduleItemDTO.builder()
                    .name(t.getName())
                    .plannedDate(t.getPlannedStart())
                    .actualDate(t.getActualStart())
                    .status(t.getStatus().name())
                    .progressPercent(t.getProgressPercent())
                    .build());
        }

        return schedule;
    }

    private List<PublicTeamMemberDTO> buildTeam(CompanyProject project, boolean includeContactInfo) {
        Map<String, PublicTeamMemberDTO> team = new LinkedHashMap<>();

        Stream<PublicTeamMemberDTO> personnel = projectTaskRepository.findByProject(project).stream()
                .map(ProjectTask::getAssignedPersonnel)
                .filter(p -> p != null)
                .map(p -> PublicTeamMemberDTO.builder()
                        .name(p.getFullName())
                        .role(p.getTitle() != null ? p.getTitle() : p.getPersonnelType().name())
                        .phone(includeContactInfo ? p.getPhone() : null)
                        .email(includeContactInfo ? p.getEmail() : null)
                        .build());

        Stream<PublicTeamMemberDTO> subcontractors = subcontractorAssignmentRepository.findByProject(project).stream()
                .map(SubcontractorAssignment::getSubcontractor)
                .map(s -> PublicTeamMemberDTO.builder()
                        .name(s.getName())
                        .role(s.getSpecialty())
                        .phone(includeContactInfo ? s.getPhone() : null)
                        .email(includeContactInfo ? s.getEmail() : null)
                        .build());

        Stream.concat(personnel, subcontractors).forEach(member -> team.putIfAbsent(member.getName(), member));

        return new ArrayList<>(team.values());
    }

    private List<PublicContractSummaryDTO> buildContracts(CompanyProject project) {
        List<Contract> contracts = contractRepository.findByProject(project);
        return contracts.stream()
                .map(c -> PublicContractSummaryDTO.builder()
                        .contractNumber(c.getContractNumber())
                        .contractType(c.getContractType() != null ? c.getContractType().name() : null)
                        .status(c.getStatus() != null ? c.getStatus().name() : null)
                        .contractAmount(c.getContractAmount())
                        .currency(c.getCurrency())
                        .build())
                .toList();
    }
}
