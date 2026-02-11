package com.github.ronlievens.examples.camel.predicate;

import org.apache.camel.Predicate;
import org.junit.jupiter.api.Test;

public class PredicateATest extends PredicateAbstractTest {

    @Override
    protected Predicate getPredicateToTest() {
        return new PredicateA();
    }

    @Test
    void goodFlow() throws Exception {
        evaluatePredicate("a", true);
        evaluatePredicate("b", false);
        evaluatePredicate("x", false);
        evaluatePredicate("nonsense", false);
    }
}
