package com.example.commerce.altcha.controller;

import com.example.commerce.altcha.dto.AltchaChallengeDTO;
import com.example.commerce.altcha.AltchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/altcha")
@RequiredArgsConstructor
public class AltchaController {

    private final AltchaService altchaService;

    @GetMapping
    public AltchaChallengeDTO getChallenge() {
        return altchaService.createChallenge();
    }
}
