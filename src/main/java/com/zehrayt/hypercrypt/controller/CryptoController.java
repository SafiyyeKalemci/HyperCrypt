package com.zehrayt.hypercrypt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zehrayt.hypercrypt.dtos.KeyExchangeRequest;
import com.zehrayt.hypercrypt.dtos.KeyExchangeResult;
import com.zehrayt.hypercrypt.service.CryptoService;

@RestController
@RequestMapping("/api/crypto")
@CrossOrigin(origins = "*") 
public class CryptoController {
    private final CryptoService cryptoService;

    @Autowired
    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @PostMapping("/calculate-key") // Hem Public Key hem de Shared Secret'ı hesaplayacak
    public KeyExchangeResult calculateKey(@RequestBody KeyExchangeRequest request) {
        try {
            // Hangi işlemi yapacağımıza karar veriyoruz.
            // Eğer karşıdan gelen anahtar null ise, genel anahtarı hesaplıyoruz.
            // Değilse, ortak sırrı hesaplıyoruz.

            int base = request.generator;
            String explanation = "Genel anahtarınız hesaplandı.";
            
            // Eğer karşıdan gelen genel anahtar varsa (Ortak Sır hesaplanacak)
            if (request.theirPublicKey != null) {
                base = request.theirPublicKey; // İşlemin tabanı karşıdan gelen anahtar olur.
                explanation = "Ortak gizli anahtarınız hesaplandı.";
            }

            Integer result = cryptoService.calculate(
                request.rule,
                base,
                request.privateKey,
                request.modulus
            );

            return new KeyExchangeResult(result, explanation);

        } catch (Exception e) {
            return new KeyExchangeResult(null, "Hata: " + e.getMessage());
        }
    }

    // Eve'in ortak sırrı kırma denemesini simüle eden DTO
    public static class EveAttackRequest {
        public Integer publicKeyA;
        public Integer publicKeyB;
        // Eve'in gizli anahtarı tahmin etmesi için başka parametreler de eklenebilir.
    }

    @PostMapping("/eve-attack")
    public KeyExchangeResult eveAttack(@RequestBody EveAttackRequest request) {
        // Bu, Eve'in A ve B'yi kullanarak S'yi bulmaya çalışmasını simüle eder.
        // Gerçek bir kriptografik sistemde bu "imkansız" olmalıdır.
        // Biz burada sadece "Eve'in denemesi başarısız oldu" mesajı döndüreceğiz.
        // İleri seviye: Burada gerçekten bir kaba kuvvet (brute-force) denemesi başlatıp
        // ne kadar uzun sürdüğünü gösterebiliriz.
        
        return new KeyExchangeResult(
            null, // Eve sırrı bulamadı.
            "Eve, genel anahtarları (A ve B) görerek ortak sırrı (S) hesaplayamadı. Hiper-işlem kuralının tek yönlü doğası, bu işlemi zorlaştırmaktadır."
        );
    }
}

