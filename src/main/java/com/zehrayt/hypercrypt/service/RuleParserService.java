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
                try {
                    // JavaScript fonksiyonunu 'a' ve 'b' değerleriyle çalıştırıyoruz.
                    Value result = jsFunction.execute(a, b);
                    Set<Integer> resultSet = new HashSet<>();

                    // JavaScript'ten dönen sonuç bir sayı mı, dizi mi, yoksa başka bir şey mi?
                    if (result.isNumber()) {
                        // Eğer sonuç tek bir sayı ise (örn: "a+b"), onu tek elemanlı bir kümeye koy.
                        resultSet.add(result.asInt());
                    } else if (result.hasArrayElements()) {
                        // Eğer sonuç bir dizi ise (örn: "[2*a*b, 3*a*b]"), tüm elemanlarını kümeye ekle.
                        for (int i = 0; i < result.getArraySize(); i++) {
                            Value element = result.getArrayElement(i);
                            if (element.isNumber()) {
                                resultSet.add(element.asInt());
                            }
                        }
                    }
                    // Eğer yukarıdakilerden hiçbiri değilse (veya tanımsızsa), boş küme döndür.
                    return resultSet;

                } catch (Exception e) {
                    // JavaScript kodunda bir hata oluşursa (örn: syntax hatası),
                    // hatayı konsola yazdır ve boş bir küme döndür.
                    System.err.println("Error executing JavaScript rule for a=" + a + ", b=" + b + ": " + e.getMessage());
                    return new HashSet<>();
                }
            };
        } catch (Exception e) {
            System.err.println("Error parsing rule '" + ruleString + "': " + e.getMessage());
            // Hata durumunda hiçbir şey yapmayan bir fonksiyon döndür.
            return (a, b) -> new HashSet<>();
        }
    }
}

