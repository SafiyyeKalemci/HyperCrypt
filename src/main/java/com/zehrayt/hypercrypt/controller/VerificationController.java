package com.zehrayt.hypercrypt.controller;

import com.zehrayt.hypercrypt.dtos.VerificationResult;
import com.zehrayt.hypercrypt.exception.InvalidRuleException;
import com.zehrayt.hypercrypt.service.GeminiSuggestionService;
import com.zehrayt.hypercrypt.service.RuleParserService;
import com.zehrayt.hypercrypt.verification.AxiomVerifier;
import com.zehrayt.hypercrypt.verification.SymbolicVerifierService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VerificationController {

    private final GeminiSuggestionService suggestionService;
    private final RuleParserService ruleParserService;    
    private final SymbolicVerifierService symbolicVerifierService;

    @Autowired
    public VerificationController(GeminiSuggestionService suggestionService, 
                                RuleParserService ruleParserService, 
                                SymbolicVerifierService symbolicVerifierService) {
        this.suggestionService = suggestionService;
        this.ruleParserService = ruleParserService;
        this.symbolicVerifierService = symbolicVerifierService;
    }

    public static class VerificationRequest {
        public Set<Integer> baseSet; // Sonlu küme
        public String domain;        // Sonsuz küme
        public String rule;
    }

    @PostMapping("/verify")
    public ResponseEntity<Object> verifyStructure(@RequestBody VerificationRequest request) {
        try {
            // --- DEĞİŞİKLİK 3: Analiz tipine göre yönlendirme yapıyoruz. ---
            if (request.domain != null && !request.domain.isBlank()) {
                
                // --- SEMBOLİK ANALİZ YOLU (Sonsuz Küme) ---
                VerificationResult result = symbolicVerifierService.verifySymbolically(request.rule, request.domain);
                return ResponseEntity.ok(result);
            } 
            else if (request.baseSet != null && !request.baseSet.isEmpty()) {

                // 2. Adım: Kuralda kullanılacak 'n' gibi sabitleri tanımla
                Map<String, Object> ruleConstants = Map.of("n", request.baseSet.size());
                
                // 3. Adım: RuleParser'ı çağırarak kuralı çalıştırılabilir bir fonksiyona çevir
                BiFunction<Integer, Integer, Set<Integer>> operation = 
                    ruleParserService.parseRule(request.rule, ruleConstants);
                
                // 4. Adım: Aksiyom motorunu bu fonksiyonla çalıştır
                AxiomVerifier<Integer> verifier = new AxiomVerifier<>(request.baseSet, operation);
                VerificationResult result = verifier.verifyAll();

                // 5. Adım: Gerekirse AI'dan öneri al
                if (!result.isHypergroup()) {
                    String failingAxiom = result.getFailingAxiom() != null ? result.getFailingAxiom() : "belirtilen aksiyomları";
                    
                    // AI'a orijinal kuralı gönderiyoruz.
                    String suggestionFromAI = suggestionService.getSuggestion(
                            request.baseSet.toString(), request.rule, failingAxiom);
                    
                    result.setSuggestion(suggestionFromAI);
                }
            
                // 6. Adım: Başarılı sonucu döndür
                return ResponseEntity.ok(result);
            } 
            else {
                // Eğer ne sonlu ne de sonsuz küme bilgisi verilmediyse hata döndür.
                throw new InvalidRuleException("İstek için 'baseSet' (sonlu küme) veya 'domain' (sonsuz küme) belirtilmelidir.");
            }

        } catch (InvalidRuleException e) {
            // Kontrollü hatalar için 400 Bad Request döndür
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Beklenmedik diğer tüm hatalar için 500 Internal Server Error döndür
            e.printStackTrace(); // Hatanın detayını konsola yazdır
            return ResponseEntity.status(500).body(Map.of("error", "Sunucuda beklenmedik bir hata oluştu."));
        }
    }
}