package com.zehrayt.hypercrypt.verification;

import com.zehrayt.hypercrypt.dtos.VerificationResult;
import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;
import org.springframework.stereotype.Service;

@Service
public class SymbolicVerifierService {

    /**
     * Verilen bir kuralı, belirtilen bir sonsuz alan üzerinde sembolik olarak analiz eder.
     * Şu an için sadece birleşme özelliğini test eder.
     */
    public VerificationResult verifySymbolically(String rule, String domain) {
        System.out.println("Performing symbolic verification for rule '" + rule + "' on domain '" + domain + "'...");
        
        VerificationResult result = new VerificationResult();
        
        try {
            // 1. Domain'e göre matematiksel alanı (katsayı fabrikasını) seç.
            RingFactory<? extends RingElem> coeffFactory = getCoefficientFactory(domain);
            
            // 2. 'a,b,c' değişkenlerini içeren polinom halkasını oluştur.
            GenPolynomialRing ring = new GenPolynomialRing(coeffFactory, new String[]{"a", "b", "c"});

            // 3. Kullanıcının kuralını, birleşme aksiyomunun sol ve sağ taraflarına yerleştir.
            // Bu, basit bir metin değiştirme yöntemiyle yapılır.
            // Kuraldaki 'a' ve 'b' yerine geçici olarak kullanılacak ifadeler hazırlanır.
            
            // Sol Taraf (LHS) için: (a ο b) ο c
            // İlk adım (a ο b): Kuraldaki 'a' yerine 'a', 'b' yerine 'b' konulur.
            String step1_lhs = rule; 
            // İkinci adım ((a ο b) ο c): Kuraldaki 'a' yerine ilk adımın sonucu, 'b' yerine 'c' konulur.
            String final_lhs_rule = rule.replace("a", "(" + step1_lhs + ")").replace("b", "(c)");

            // Sağ Taraf (RHS) için: a ο (b ο c)
            // İlk adım (b ο c): Kuraldaki 'a' yerine 'b', 'b' yerine 'c' konulur.
            String step1_rhs = rule.replace("a", "(b)").replace("b", "(c)");
            // İkinci adım (a ο (b ο c)): Kuraldaki 'a' yerine 'a', 'b' yerine ilk adımın sonucu konulur.
            String final_rhs_rule = rule.replace("a", "(a)").replace("b", "(" + step1_rhs + ")");

            System.out.println("LHS Rule to parse: " + final_lhs_rule);
            System.out.println("RHS Rule to parse: " + final_rhs_rule);

            // 4. Bu yeni, karmaşık kuralları Jas-Lib ile ayrıştır (parse).
            GenPolynomial lhs = ring.parse(final_lhs_rule);
            GenPolynomial rhs = ring.parse(final_rhs_rule);

            // 5. İki sembolik ifadenin eşit olup olmadığını kontrol et.
            if (lhs.equals(rhs)) {
                result.setSemihypergroup(true);
                result.setHighestStructure("At least a Semihypergroup (Symbolic)");
                result.setFailingAxiom(null);
            } else {
                result.setSemihypergroup(false);
                result.setFailingAxiom("Birleşme Özelliği (Associativity)");
                result.setHighestStructure("Hypergroupoid (Symbolic)");
            }
            
            // TODO: Üretim aksiyomunun sembolik kontrolü buraya eklenebilir.
            // Bu çok daha zor bir problemdir. Şimdilik varsayılan değerleri bırakıyoruz.
            result.setQuasihypergroup(false);
            result.setHypergroup(false);


        } catch (Exception e) {
            e.printStackTrace();
            result.setSuggestion("Symbolic analysis failed: " + e.getMessage());
            // Hata durumunda tüm kontrolleri başarısız sayalım.
            result.setSemihypergroup(false);
            result.setQuasihypergroup(false);
            result.setHypergroup(false);
        }
        
        return result;
    }
    
    // getCoefficientFactory metodu aynı kalıyor.
    private RingFactory<? extends RingElem> getCoefficientFactory(String domain) {
        if (domain == null) {
            return new BigInteger();
        }
        
        switch (domain.toUpperCase()) {
            case "INTEGERS":
                return new BigInteger();
            case "RATIONALS":
                return new BigRational();
            default:
                throw new IllegalArgumentException("Unsupported domain: " + domain);
        }
    }
}