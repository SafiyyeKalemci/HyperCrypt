package com.zehrayt.hypercrypt.verification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AxiomVerifier<T> {

    // Hiper-işlemi temsil eden fonksiyonel arayüz
    // İki eleman alır (T, T), bir küme döndürür (Set<T>)
    private final BiFunction<T, T, Set<T>> hyperOperation;
    private final Set<T> baseSet;
    private final List<T> baseSetAsList; //////////////////////////// Kombinasyonlar için listeye ihtiyacımız olacak

    public AxiomVerifier(Set<T> baseSet, BiFunction<T, T, Set<T>> hyperOperation) {
        this.baseSet = baseSet;
        this.hyperOperation = hyperOperation;
        this.baseSetAsList = new java.util.ArrayList<>(baseSet);
    }

    /**
     * Birleşme özelliğini kontrol eder: (a ο b) ο c = a ο (b ο c)
     * Bu, kümedeki tüm (a, b, c) üçlüleri için kontrol edilmelidir.
     * @return Birleşme özelliği sağlanıyorsa true, aksi halde false.
     */
    public boolean isAssociative() {
        System.out.println("Checking for associativity...");
        // Kümedeki tüm olası (a, b, c) üçlülerini denememiz gerekiyor.
        for (T a : baseSet) {
            for (T b : baseSet) {
                for (T c : baseSet) {
                    // Sol Taraf: (a ο b) ο c
                    Set<T> leftSideResult = new HashSet<>();
                    Set<T> firstOpResult = hyperOperation.apply(a, b);
                    for (T intermediateResult : firstOpResult) {
                        leftSideResult.addAll(hyperOperation.apply(intermediateResult, c));
                    }

                    // Sağ Taraf: a ο (b ο c)
                    Set<T> rightSideResult = new HashSet<>();
                    Set<T> secondOpResult = hyperOperation.apply(b, c);
                    for (T intermediateResult : secondOpResult) {
                        rightSideResult.addAll(hyperOperation.apply(a, intermediateResult));
                    }

                    // İki sonuç kümesi eşit değilse, özellik sağlanmıyor demektir.
                    if (!leftSideResult.equals(rightSideResult)) {
                        System.out.println("Associativity failed for (a,b,c) = (" + a + "," + b + "," + c + ")");
                        System.out.println("LHS: " + leftSideResult);
                        System.out.println("RHS: " + rightSideResult);
                        return false;
                    }
                }
            }
        }
        return true; // Tüm üçlüler için kontrol başarılı oldu.
    }

    /**
     * Üretim aksiyomunu kontrol eder: a ο H = H ve H ο a = H
     * Bu, kümedeki her 'a' elemanı için kontrol edilmelidir.
     * @return Üretim aksiyomu sağlanıyorsa true, aksi halde false.
     */
    public boolean checkReproductionAxiom() {
        System.out.println("Checking for reproduction axiom...");
        for (T a : baseSet) {
            // a ο H kontrolü
            Set<T> leftResult = new HashSet<>();
            for (T h : baseSet) {
                leftResult.addAll(hyperOperation.apply(a, h));
            }
            if (!leftResult.equals(baseSet)) {
                System.out.println("Reproduction failed for a ο H where a = " + a);
                return false;
            }

            // H ο a kontrolü
            Set<T> rightResult = new HashSet<>();
            for (T h : baseSet) {
                rightResult.addAll(hyperOperation.apply(h, a));
            }
            if (!rightResult.equals(baseSet)) {
                 System.out.println("Reproduction failed for H ο a where a = " + a);
                return false;
            }
        }
        return true;
    }
}