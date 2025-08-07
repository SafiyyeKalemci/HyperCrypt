package com.zehrayt.hypercrypt.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bu sınıf, klasik bir halkadan (Z_n) bölüm alınarak oluşturulan
 * bir Krasner hiperhalkasını modeller.
 * Hiper-toplama işlemi, teorik tanımına uygun olarak burada implemente edilmiştir.
 */
public class KrasnerQuotientHyperring {

    private final int modulus; // Z_n için n değeri (örneğimizde 13)
    private final Set<Integer> subgroupH; // Çarpımsal alt grup H (örneğimizde {1, 5, 8, 12})
    private final Map<Integer, Set<Integer>> cosets; // Denklik sınıfları (1H, 2H, 4H...)
    private final Map<Integer, Integer> elementToCosetRepresentative; // Bir elemanın hangi denklik sınıfında olduğunu hızlıca bulmak için

    public KrasnerQuotientHyperring(int modulus, Set<Integer> subgroupH) {
        this.modulus = modulus;
        this.subgroupH = Collections.unmodifiableSet(subgroupH);
        this.cosets = new HashMap<>();
        this.elementToCosetRepresentative = new HashMap<>();
        
        generateCosets();
    }

    // Denklik sınıflarını (cosets) ve haritaları oluşturan metot
    private void generateCosets() {
        Set<Integer> remainingElements = new HashSet<>();
        for (int i = 1; i < modulus; i++) {
            remainingElements.add(i);
        }
        
        cosets.put(0, Set.of(0));
        elementToCosetRepresentative.put(0, 0);

        while (!remainingElements.isEmpty()) {
            int representative = remainingElements.iterator().next();
            Set<Integer> currentCoset = subgroupH.stream()
                .map(h -> (representative * h) % modulus)
                .collect(Collectors.toSet());
            
            cosets.put(representative, currentCoset);
            for (int element : currentCoset) {
                elementToCosetRepresentative.put(element, representative);
                remainingElements.remove(element);
            }
        }
    }

    /**
     * Krasner hiperhalkasındaki hiper-toplama işlemini gerçekleştirir.
     * @param repA Birinci denklik sınıfının temsilcisi (örn: 2)
     * @param repB İkinci denklik sınıfının temsilcisi (örn: 4)
     * @return Sonuç denklik sınıflarının temsilcilerinden oluşan bir küme.
     */
    public Set<Integer> hyperAdd(int repA, int repB) {
        // Temsilcilerin geçerli olup olmadığını kontrol et
        if (!cosets.containsKey(repA) || !cosets.containsKey(repB)) {
            throw new IllegalArgumentException("Invalid coset representative.");
        }

        // Adım a: b'nin denklik sınıfındaki tüm elemanları al.
        Set<Integer> elementsInBCoset = cosets.get(repB);
        
        Set<Integer> resultRepresentatives = new HashSet<>();

        // Adım b & c: her elemanı a ile topla ve hangi denklik sınıfına ait olduğunu bul.
        for (int c : elementsInBCoset) {
            int sum = (repA + c) % modulus;
            int representativeOfSum = elementToCosetRepresentative.get(sum);
            resultRepresentatives.add(representativeOfSum);
        }
        
        // Adım d: Sonuç kümesini döndür.
        return resultRepresentatives;
    }

    // Test ve gösterim için main metodu
    public static void main(String[] args) {
        // Z_13 ve H = {1, 5, 8, 12} ile bir Krasner hiperhalkası oluşturalım.
        int modulus = 13;
        Set<Integer> subgroupH = Set.of(1, 5, 8, 12);
        
        KrasnerQuotientHyperring krasnerRing = new KrasnerQuotientHyperring(modulus, subgroupH);

        System.out.println("Oluşturulan Denklik Sınıfları (Cosets):");
        krasnerRing.cosets.forEach((rep, set) -> System.out.println("Temsilci " + rep + ": " + set));
        
        System.out.println("\n-----------------------------------\n");
        
        // Örnek işlem: 2H ⊕ 4H
        int repA = 2;
        int repB = 4;
        
        System.out.println("İşlem: " + repA + "H ⊕ " + repB + "H");
        Set<Integer> result = krasnerRing.hyperAdd(repA, repB);
        
        System.out.println("Sonuç Kümesi (temsilciler): " + result);
    }
}