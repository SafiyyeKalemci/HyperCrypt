package com.zehrayt.hypercrypt.controller;

import com.zehrayt.hypercrypt.verification.AxiomVerifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Geçici olarak tüm isteklere izin veriyoruz. Sonra spesifik adrese çevir!!!!!!!!!!!!!!!
public class VerificationController {

    // Dışarıdan gelen isteği temsil eden bir DTO (Data Transfer Object)
    public static class VerificationRequest {
        public Set<Integer> baseSet;
        public String rule; // Kuralı şimdilik basit bir string olarak alıyoruz.
    }

    // Dışarıya gönderilecek cevabı temsil eden bir DTO
    public static class VerificationResponse {
        public boolean isHypergroup;
        public Map<String, Boolean> checks;
        public String suggestion; //////////////////////// AI için yer
    }

    @PostMapping("/verify")
    public VerificationResponse verifyStructure(@RequestBody VerificationRequest request) {
        
        // Kural string'ini gerçek bir fonksiyona çeviriyoruz.
        //////////////////////////////////////// DEĞİŞTİRMEYİ UNUTMA Bu kısım projenin en karmaşık yerlerinden biri olacak, şimdilik basit 
        BiFunction<Integer, Integer, Set<Integer>> operation = (a, b) -> {
            if ("a+b".equals(request.rule)) {
                // Modulo işlemi kümedeki en büyük elemana göre yapılabilir, şimdilik 3 varsayalım.
                return Set.of((a + b) % 3); 
            } else if ("{a,b}".equals(request.rule)) {
                return Set.of(a, b);
            }
            return new HashSet<>(); // Bilinmeyen kural
        };

        // Aksiyom motorumuzu oluşturup çalıştırıyoruz.
        AxiomVerifier<Integer> verifier = new AxiomVerifier<>(request.baseSet, operation);
        
        boolean isAssociative = verifier.isAssociative();
        boolean hasReproduction = verifier.checkReproductionAxiom();
        
        VerificationResponse response = new VerificationResponse();
        response.isHypergroup = isAssociative && hasReproduction;
        response.checks = Map.of("isAssociative", isAssociative, "hasReproduction", hasReproduction);

        // Eğer isHypergroup false ise, burada AI servisini çağır.
        if (!response.isHypergroup) {
            response.suggestion = "AI suggestion will be here...";
        }

        return response;
    }
}