package com.example.commerce.mail.controller;

import com.example.commerce.mail.service.MailService;
import com.example.commerce.mail.dto.MailRequestDTO;
import com.example.commerce.mail.dto.MailResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/gonder")
    public ResponseEntity<MailResponseDTO> mailGonder(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody MailRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(mailService.mailGonder(apiKey, requestDTO));
    }
}

