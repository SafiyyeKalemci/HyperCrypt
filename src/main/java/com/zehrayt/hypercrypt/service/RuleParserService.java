package com.zehrayt.hypercrypt.service;

import com.zehrayt.hypercrypt.exception.InvalidRuleException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Service
public class RuleParserService {

    public BiFunction<Integer, Integer, Set<Integer>> parseRule(String ruleString, Map<String, Object> constants) {
        if (ruleString == null || ruleString.isBlank()) {
            throw new InvalidRuleException("Kural metni boş olamaz.");
        }

        final String functionWrapper = String.format("function(a, b) { return %s; }", ruleString);
        
        // Bu, daha sonra kullanılacak olan ana scope (çalışma alanı).
        final Scriptable mainScope;

        // --- DÜZELTME BAŞLANGICI: Derleme işlemini dışarı alıyoruz ---
        Context rhinoContext = Context.enter();
        try {
            rhinoContext.setOptimizationLevel(-1);
            mainScope = rhinoContext.initStandardObjects();

            // Sabitleri scope'a burada ekliyoruz.
            if (constants != null) {
                for (Map.Entry<String, Object> entry : constants.entrySet()) {
                    Object jsValue = Context.javaToJS(entry.getValue(), mainScope);
                    mainScope.put(entry.getKey(), mainScope, jsValue);
                }
            }
            
            // Kuralın sözdizimini burada, en başta kontrol ediyoruz.
            // Eğer "a +* b" gibi bir hata varsa, Exception burada fırlatılacak.
            rhinoContext.compileFunction(mainScope, functionWrapper, "rule", 1, null);

        } catch (Exception e) {
            throw new InvalidRuleException("Kuralda sözdizimi hatası var: " + e.getMessage());
        } finally {
            Context.exit();
        }
        // --- DÜZELTME BİTİŞİ ---

        return (a, b) -> {
            Context executionContext = Context.enter();
            try {
                // Her çalıştırmada, daha önce oluşturduğumuz ve sabitleri içeren scope'u kullanıyoruz.
                // Ve fonksiyonu yeniden derleyip çalıştırıyoruz.
                Function jsFunction = executionContext.compileFunction(mainScope, functionWrapper, "rule", 1, null);
                
                Object result = jsFunction.call(executionContext, mainScope, mainScope, new Object[]{a, b});
                Set<Integer> resultSet = new HashSet<>();

                if (result instanceof Number) {
                    resultSet.add(((Number) result).intValue());
                } else if (result instanceof NativeArray) {
                    NativeArray nativeArray = (NativeArray) result;
                    for (Object item : nativeArray) {
                        if (item instanceof Number) {
                            resultSet.add(((Number) item).intValue());
                        }
                    }
                } else if (result != null && "undefined".equals(Context.toString(result))) {
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
                Context.exit();
            }
        };
    }
}