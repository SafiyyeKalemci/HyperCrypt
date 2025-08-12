package com.zehrayt.hypercrypt.service;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

@Service
public class RuleParserService {

    /**
     * Kullanıcı tarafından girilen bir kural metnini, çalıştırılabilir bir Java fonksiyonuna dönüştürür.
     * @param ruleString Kullanıcının girdiği kural, örn: "(a + b) % 5"
     * @return İki Integer alıp bir Set<Integer> döndüren bir BiFunction.
     */
    public BiFunction<Integer, Integer, Set<Integer>> parseRule(String ruleString) {
        
        // GraalVM JavaScript motoru için bir context oluşturuyoruz.
        try (Context context = Context.create("js")) {

            // Kullanıcının kuralını alıp bir JavaScript fonksiyonuna çeviriyoruz.
            // Bu fonksiyon 'a' ve 'b' adında iki parametre alacak.
            String jsFunctionString = String.format("((a, b) => { return %s; })", ruleString);
            
            // JavaScript fonksiyonunu derliyoruz.
            Value jsFunction = context.eval("js", jsFunctionString);
            
            // Bu JavaScript fonksiyonunu çağıran bir Java BiFunction döndürüyoruz.
            return (a, b) -> {
                // JavaScript fonksiyonunu 'a' ve 'b' değerleriyle çalıştırıyoruz.
                Value result = jsFunction.execute(a, b);
                
                // JavaScript'ten dönen sonuç bir sayı mı yoksa bir dizi mi diye kontrol ediyoruz.
                if (result.isNumber()) {
                    return Set.of(result.asInt());
                } else if (result.hasArrayElements()) {
                    Set<Integer> resultSet = new HashSet<>();
                    for (int i = 0; i < result.getArraySize(); i++) {
                        resultSet.add(result.getArrayElement(i).asInt());
                    }
                    return resultSet;
                }
                // Eğer sonuç anlaşılamazsa boş küme döndür.
                return new HashSet<>();
            };
        } catch (Exception e) {
            System.err.println("Error parsing rule '" + ruleString + "': " + e.getMessage());
            // Hata durumunda hiçbir şey yapmayan bir fonksiyon döndür.
            return (a, b) -> new HashSet<>();
        }
    }
}