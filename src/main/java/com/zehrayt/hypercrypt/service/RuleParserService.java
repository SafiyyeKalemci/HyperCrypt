package com.zehrayt.hypercrypt.service;

import com.zehrayt.hypercrypt.exception.InvalidRuleException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.Map;

@Service
public class RuleParserService {

    public BiFunction<Integer, Integer, Set<Integer>> parseRule(String ruleString, Map<String, Object> constants){
        if (ruleString == null || ruleString.isBlank()) {
            throw new InvalidRuleException("Kural metni boş olamaz.");
        }

        // JavaScript fonksiyonunu string olarak hazırlıyoruz.
        // ÖNEMLİ: Bu, derleme işlemini her bir işlem çağrısında yapacağımız için
        // daha az verimli olabilir, ama thread-safety (iş parçacığı güvenliği) sağlar.
        // İleride performans optimizasyonu için derlenmiş script'leri cache'leyebiliriz.
        final String functionWrapper = String.format("function(a, b) { return %s; }", ruleString);

        // Bu JavaScript fonksiyonunu çağıran bir Java BiFunction döndürüyoruz.
        return (a, b) -> {
            Context rhinoContext = Context.enter(); // <<< --- DÜZELTME 1: Context'i burada oluştur.
            try {
                rhinoContext.setOptimizationLevel(-1);
                Scriptable scope = rhinoContext.initStandardObjects();

                // <<< --- DEĞİŞİKLİK 2: Sabitleri JavaScript'in scope'una ekle ---
                if (constants != null) {
                    for (Map.Entry<String, Object> entry : constants.entrySet()) {
                        // Java'dan gelen 'n' gibi sabitleri JavaScript'in anlayacağı formata çevirip ortama ekliyoruz.
                        Object jsValue = Context.javaToJS(entry.getValue(), scope);
                        scope.put(entry.getKey(), scope, jsValue);
                    }
                }
                // --- Değişikliğin sonu ---
                
                // Fonksiyonu her çağrıda yeniden derliyoruz.
                Function jsFunction = rhinoContext.compileFunction(scope, functionWrapper, "rule", 1, null);
                
                Object result = jsFunction.call(rhinoContext, scope, scope, new Object[]{a, b});
                Set<Integer> resultSet = new HashSet<>();

                if (result instanceof Number) {
                    // Math.sqrt gibi fonksiyonlar double döndürebilir, int'e çeviriyoruz.
                    resultSet.add(((Number) result).intValue());
                } else if (result instanceof NativeArray) {
                    NativeArray nativeArray = (NativeArray) result;
                    for (Object item : nativeArray) {
                        if (item instanceof Number) {
                            resultSet.add(((Number) item).intValue());
                        }
                    }
                } else if (result != null && "undefined".equals(Context.toString(result))) {
                    // JavaScript "undefined" döndürürse, boş küme kabul edelim.
                    return resultSet;
                } else {
                     throw new InvalidRuleException(
                        String.format("Kural, beklenmedik bir sonuç tipi döndürdü. Sayı veya dizi bekleniyordu, ancak '%s' geldi.", result != null ? result.getClass().getSimpleName() : "null")
                    );
                }
                return resultSet;
            } catch (Exception e) {
                throw new InvalidRuleException("Kural çalıştırılırken hata oluştu: " + e.getMessage());
            } finally {
                Context.exit(); // <<< --- DÜZELTME 2: Context'i burada kapat.
            }
        };
    }
}