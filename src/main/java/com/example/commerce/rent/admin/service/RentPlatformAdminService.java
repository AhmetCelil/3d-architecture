package com.example.commerce.rent.admin.service;

import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.rent.admin.dto.RentModulDurumDTO;
import com.example.commerce.rent.entity.RentSettings;
import com.example.commerce.rent.repository.RentSettingsRepository;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.repository.CompanyRepository;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * rent modülünün platform tarafından (şirketin kendisi değil) hangi şirketler için
 * açık olduğunu yönetir. Süper admin panelinden bağımsız, rent paketinin kendi içinde tutulur.
 */
@Service
@RequiredArgsConstructor
public class RentPlatformAdminService {

    private final CompanyRepository companyRepository;
    private final RentSettingsRepository rentSettingsRepository;

    @Transactional
    public RentModulDurumDTO durumGuncelle(Long companyId, boolean enabled) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("sirket.bulunamadi", "Şirket bulunamadı"));

        RentSettings settings = rentSettingsRepository.findByCompany(company)
                .orElseGet(() -> RentSettings.builder().company(company).build());
        settings.setEnabled(enabled);
        rentSettingsRepository.save(settings);

        RentModulDurumDTO responseDTO = new RentModulDurumDTO();
        responseDTO.setCompanyId(company.getId());
        responseDTO.setEnabled(enabled);
        responseDTO.setMessages(List.of(AppMessageUtil.create(
                "rent.modul.guncelleme.basarili", "Rezervasyon modülü durumu güncellendi", AppMessageType.SUCCESS)));
        return responseDTO;
    }
}
