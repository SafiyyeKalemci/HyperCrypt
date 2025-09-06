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

@Service
public class RuleParserService {

    public BiFunction<Integer, Integer, Set<Integer>> parseRule(String ruleString) {
        if (ruleString == null || ruleString.isBlank()) {
            throw new InvalidRuleException("Kural metni boş olamaz.");
        }

        // Rhino context'ini thread-safe bir şekilde oluşturuyoruz.
        Context rhinoContext = Context.enter();
        rhinoContext.setOptimizationLevel(-1); // Performans için
        
        try {
            Scriptable scope = rhinoContext.initStandardObjects();
            String functionWrapper = String.format("function(a, b) { return %s; }", ruleString);
            
            // JavaScript fonksiyonunu derliyoruz.
            Function jsFunction = rhinoContext.compileFunction(scope, functionWrapper, "rule", 1, null);

            return (a, b) -> {
                try {
                    // JavaScript fonksiyonunu 'a' ve 'b' parametreleriyle çağırıyoruz.
                    Object result = jsFunction.call(rhinoContext, scope, scope, new Object[]{a, b});
                    Set<Integer> resultSet = new HashSet<>();

                    // Dönen sonucun tipini kontrol ediyoruz.
                    if (result instanceof Number) {
                        resultSet.add(((Number) result).intValue());
                    } else if (result instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) result;
                        for (Object item : nativeArray) {
                            if (item instanceof Number) {
                                resultSet.add(((Number) item).intValue());
                            }
                        }
                    } else {
                         throw new InvalidRuleException(
                            String.format("Kural, beklenmedik bir sonuç tipi döndürdü. Sayı veya dizi bekleniyordu, ancak '%s' geldi.", result.getClass().getSimpleName())
                        );
                    }
                    return resultSet;
                } catch (Exception e) {
                    throw new InvalidRuleException("Kural çalıştırılırken hata oluştu: " + e.getMessage());
                }
            };
        } catch (Exception e) {
            throw new InvalidRuleException("Kuralda sözdizimi hatası var: " + e.getMessage());
        } finally {
            // Context'i her zaman kapatmalıyız.
            Context.exit();
        }
    }
}