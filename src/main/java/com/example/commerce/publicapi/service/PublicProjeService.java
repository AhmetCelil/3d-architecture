package com.example.commerce.publicapi.service;

import com.example.commerce.adminpanel.dto.FloorPlanRoomDetailDTO;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.adminpanel.repository.ProjectFileRepository;
import com.example.commerce.adminpanel.repository.ProjectFileRepository.ProjectFileMetaView;
import com.example.commerce.adminpanel.repository.ProjectFileRepository.RoomDetailRow;
import com.example.commerce.adminpanel.repository.UnitTypeRepository;
import com.example.commerce.common.cache.FileByteCache;
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
import com.example.commerce.tenant.service.ApiKeyService;
import com.example.commerce.util.FileResponseUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicProjeService {

    /** Tek seferde döndürülebilecek dosya için makul bir üst sınır (50MB). */
    private static final long MAX_DOWNLOADABLE_FILE_SIZE = 50L * 1024 * 1024;
    private static final int MAX_PAGE_SIZE = 100;

    private final CompanyProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final FileByteCache fileByteCache;
    private final ApiKeyService apiKeyService;
    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final SubcontractorAssignmentRepository subcontractorAssignmentRepository;

    private Company getCompanyByApiKey(String apiKey) {
        return apiKeyService.resolveCompany(apiKey);
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

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> dosyaIndir(String apiKey, Long projeId, Long dosyaId) {
        Company company = getCompanyByApiKey(apiKey);

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI", "Proje bulunamadı"));

        ProjectFileMetaView meta = projectFileRepository.findMetaByIdAndProjectId(dosyaId, project.getId())
                .orElseThrow(() -> new BusinessServiceException("DOSYA_BULUNAMADI", "Dosya bulunamadı"));

        if (meta.getFileSize() != null && meta.getFileSize() > MAX_DOWNLOADABLE_FILE_SIZE) {
            throw new BusinessServiceException("DOSYA_COK_BUYUK", "Dosya bu uç nokta üzerinden indirilemeyecek kadar büyük");
        }

        byte[] data = fileByteCache.get("projectFile:" + dosyaId,
                key -> projectFileRepository.findById(dosyaId).map(ProjectFile::getFileData).orElse(null));

        return FileResponseUtil.inline(meta.getFileName(), meta.getFileType(), data, "private, max-age=3600");
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
                .dosyaSayisi((int) projectFileRepository.countByProjectAndDeletedFalse(project))
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
                .files(toPublicDosyaMetaDTOs(projectFileRepository.findMetaByProjectAndDeletedFalse(project)))
                .unitTypes(buildUnitTypes(project))
                .schedule(buildSchedule(project))
                .team(buildTeam(project, includeSensitiveData))
                .contracts(includeSensitiveData ? buildContracts(project) : List.of())
                .build();
    }

    /** Dosya içeriğini (bytea) hiç DB'den çekmeden yalnızca metadata döndürür. */
    private List<PublicDosyaMetaDTO> toPublicDosyaMetaDTOs(List<ProjectFileMetaView> views) {
        if (views.isEmpty()) return List.of();

        List<Long> fileIds = views.stream().map(ProjectFileMetaView::getId).toList();
        Map<Long, List<FloorPlanRoomDetailDTO>> roomDetailsByFileId = projectFileRepository.findRoomDetailRows(fileIds).stream()
                .collect(Collectors.groupingBy(RoomDetailRow::getFileId,
                        Collectors.mapping(rd -> FloorPlanRoomDetailDTO.builder()
                                        .roomName(rd.getRoomName())
                                        .value(rd.getValue())
                                        .build(),
                                Collectors.toList())));

        return views.stream()
                .map(v -> PublicDosyaMetaDTO.builder()
                        .id(v.getId())
                        .fileName(v.getFileName())
                        .fileType(v.getFileType())
                        .fileSize(v.getFileSize())
                        .title(v.getTitle())
                        .fileCategory(v.getFileCategory() != null ? v.getFileCategory().name() : null)
                        .roomDetails(roomDetailsByFileId.getOrDefault(v.getId(), List.of()))
                        .build())
                .toList();
    }

    private List<PublicUnitTypeDTO> buildUnitTypes(CompanyProject project) {
        return unitTypeRepository.findByProjectAndDeletedFalse(project).stream()
                .map(ut -> PublicUnitTypeDTO.builder()
                        .id(ut.getId())
                        .blockLabel(ut.getBlockLabel())
                        .label(ut.getLabel())
                        .area(ut.getArea())
                        .roomCount(ut.getRoomCount())
                        .description(ut.getDescription())
                        .roomDetails(ut.getRoomDetails().stream()
                                .map(rd -> FloorPlanRoomDetailDTO.builder()
                                        .roomName(rd.getRoomName())
                                        .value(rd.getValue())
                                        .build())
                                .toList())
                        .files(toPublicDosyaMetaDTOs(projectFileRepository.findMetaByUnitTypeAndDeletedFalse(ut)))
                        .build())
                .toList();
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
