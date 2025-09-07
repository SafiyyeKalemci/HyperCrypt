package com.zehrayt.hypercrypt.service;

import com.zehrayt.hypercrypt.exception.InvalidRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class RuleParserServiceTest {

    private RuleParserService ruleParserService;

    @BeforeEach
    void setUp() {
        ruleParserService = new RuleParserService();
    }

    @Test
    void test_simpleAdditionRule_shouldReturnCorrectSet() {
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule("a + b", null);
        Set<Integer> result = operation.apply(10, 5);
        assertEquals(Set.of(15), result);
    }

    @Test
    void test_arrayResultRule_shouldReturnCorrectSet() {
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule("[a+b, a-b]", null);
        Set<Integer> result = operation.apply(10, 5);
        assertEquals(Set.of(15, 5), result);
    }

    @Test
    void test_conditionalRule_shouldWorkCorrectly() {
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule("a === b ? [a] : [a, b]", null);
        
        Set<Integer> resultWhenEqual = operation.apply(7, 7);
        assertEquals(Set.of(7), resultWhenEqual);

        Set<Integer> resultWhenNotEqual = operation.apply(7, 8);
        assertEquals(Set.of(7, 8), resultWhenNotEqual);
    }

    @Test
    void test_dynamicModuloRule_withConstants_shouldWorkCorrectly() {
        Map<String, Object> constants = Map.of("n", 3);
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule("(a + b) % n", constants);
        
        Set<Integer> result = operation.apply(2, 2); // (2+2)%3 = 4%3 = 1
        assertEquals(Set.of(1), result);
    }
    
    @Test
    void test_mathFunctionRule_shouldWorkCorrectly() {
        BiFunction<Integer, Integer, Set<Integer>> operation = ruleParserService.parseRule("Math.pow(a, b)", null);
        Set<Integer> result = operation.apply(2, 3); // 2^3 = 8
        assertEquals(Set.of(8), result);
    }

    @Test
    void test_invalidSyntaxRule_shouldThrowInvalidRuleException() {
        assertThrows(InvalidRuleException.class, () -> {
            ruleParserService.parseRule("a +* b", null);
        });
    }
    
    @Test
    void test_emptyRule_shouldThrowInvalidRuleException() {
         assertThrows(InvalidRuleException.class, () -> {
            ruleParserService.parseRule("", null);
        });
    }
}