// VerificationController.java

package com.zehrayt.hypercrypt.controller;

import com.zehrayt.hypercrypt.exception.InvalidRuleException; 
import com.zehrayt.hypercrypt.service.GeminiSuggestionService; 
import com.zehrayt.hypercrypt.dtos.VerificationResult;
import com.zehrayt.hypercrypt.verification.AxiomVerifier;
import com.zehrayt.hypercrypt.service.RuleParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; 
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
    public ResponseEntity<Object> verifyStructure(@RequestBody VerificationRequest request) { // DÖNÜŞ TİPİ DEĞİŞTİ

        try {
            // Girdi Kontrolü
            if (request.baseSet == null || request.baseSet.isEmpty()) {
                throw new InvalidRuleException("Temel küme boş olamaz.");
            }

            BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule(request.rule);
            
            AxiomVerifier<Integer> verifier = new AxiomVerifier<>(request.baseSet, operation);
            VerificationResult result = verifier.verifyAll();

            if (!result.isHypergroup()) {
                String failingAxiom = result.getFailingAxiom() != null ? result.getFailingAxiom() : "belirtilen aksiyomları";
                String suggestionFromAI = suggestionService.getSuggestion(
                        request.baseSet.toString(), request.rule, failingAxiom);
                result.setSuggestion(suggestionFromAI);
            }
            
            // Başarılı durumda 200 OK ve sonucu döndür.
            return ResponseEntity.ok(result);

        } catch (InvalidRuleException e) {
            // Hatalı kural veya girdi durumunda 400 Bad Request ve hata mesajını döndür.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Beklenmedik diğer tüm sunucu hataları için 500 Internal Server Error döndür.
            return ResponseEntity.status(500).body(Map.of("error", "Sunucuda beklenmedik bir hata oluştu: " + e.getMessage()));
        }
    }
}