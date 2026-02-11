package com.github.ronlievens.examples.camel.predicate;

import org.apache.camel.Predicate;
import org.junit.jupiter.api.Test;

public class PredicateBTest extends PredicateAbstractTest {

    @Override
    protected Predicate getPredicateToTest() {
        return new PredicateB();
    }

    @Test
    void goodFlow() throws Exception {
        evaluatePredicate("a", false);
        evaluatePredicate("b", true);
        evaluatePredicate("x", false);
        evaluatePredicate("nonsense", false);
    }
}
