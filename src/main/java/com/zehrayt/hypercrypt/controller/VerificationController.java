// VerificationController.java

package com.zehrayt.hypercrypt.controller;

import com.zehrayt.hypercrypt.service.GeminiSuggestionService; 
import com.zehrayt.hypercrypt.dtos.VerificationResult;
import com.zehrayt.hypercrypt.verification.AxiomVerifier;
import com.zehrayt.hypercrypt.service.RuleParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Geliştirme aşaması için tüm isteklere izin veriyoruz.
public class VerificationController {

    // GeminiSuggestionService, AI önerilerini almak için kullanılıyor.
    // Bu servisi, Spring'in otomatik olarak enjekte etmesini sağlıyoruz.
    private final GeminiSuggestionService suggestionService;
    private final RuleParserService ruleParserService; 

    @Autowired
    public VerificationController(GeminiSuggestionService suggestionService, RuleParserService ruleParserService) {
        this.suggestionService = suggestionService;
        this.ruleParserService = ruleParserService;
    }

    // Frontend'den gelen isteği temsil eden DTO (Data Transfer Object)
    // Bu sınıfı, controller sınıfının içinde veya dtos paketinde ayrı bir dosyada tanımlayabilirsiniz.
    public static class VerificationRequest {
        public Set<Integer> baseSet;
        public String rule;
    }

    /**
     * Frontend'den gelen bir hiperyapı tanımını alır, aksiyomlarını kontrol eder,
     * ve detaylı bir sonuç raporu döndürür.
     * Eğer yapı bir hipergrup değilse, AI'dan alınacak öneri için yer bırakır.
     * @param request Kullanıcının girdiği temel küme ve kuralı içeren istek.
     * @return Yapının cebirsel özelliklerini ve AI önerisini içeren detaylı sonuç.
     */
    @PostMapping("/verify")
    public VerificationResult verifyStructure(@RequestBody VerificationRequest request) {

        // =======================================================================
        // Adım 1: Kuralı dinamik bir fonksiyona çevir.
        // BU KISIM İLERİDE GELİŞTİRİLECEK. ŞİMDİLİK BASİT KURALLARI TANIYOR.
        // unutmaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // =======================================================================
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule(request.rule);

        // =======================================================================
        // Adım 2: Aksiyom motorunu oluştur ve tüm kontrolleri çalıştır.
        // =======================================================================
        AxiomVerifier<Integer> verifier = new AxiomVerifier<>(request.baseSet, operation);
        VerificationResult result = verifier.verifyAll(); // Tek çağrı ile tüm sonuçları alıyoruz.

        // =======================================================================
        // Adım 3: Sonuç hipergrup değilse, AI önerisi için metin hazırla.
        // =======================================================================
        // AI önerisi için Gemini servisini çağırıyoruz.
        if (!result.isHypergroup()) {
            String failingAxiom = result.getFailingAxiom() != null ? result.getFailingAxiom() : "belirtilen aksiyomları";
            
            // Gemini servisini çağırıyoruz!
            String suggestionFromAI = suggestionService.getSuggestion(
                    request.baseSet.toString(), 
                    request.rule, 
                    failingAxiom
            );
            
            result.setSuggestion(suggestionFromAI);
        }

        // =======================================================================
        // Adım 4: Detaylı sonuç nesnesini Frontend'e döndür.
        // =======================================================================
        return result;
    }
}