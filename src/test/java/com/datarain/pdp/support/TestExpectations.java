package com.datarain.pdp.support;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public final class TestExpectations {

    private TestExpectations() {
    }

    public static ResultMatcher status(int expectedStatus) {
        return result -> {
            System.out.println(AnsiColors.GREEN + "\n=== EXPECTED STATUS ===\n" + expectedStatus + AnsiColors.RESET);
            MockMvcResultMatchers.status().is(expectedStatus).match(result);
        };
    }

    public static int restStatus(int expectedStatus) {
        System.out.println(AnsiColors.GREEN + "\n=== EXPECTED STATUS ===\n" + expectedStatus + AnsiColors.RESET);
        return expectedStatus;
    }
}
