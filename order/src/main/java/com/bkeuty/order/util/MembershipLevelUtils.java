package com.bkeuty.order.util;

import java.math.BigDecimal;

public class MembershipLevelUtils {

    private static final BigDecimal DIAMOND_THRESHOLD = new BigDecimal("30000000");
    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("15000000");
    private static final BigDecimal GOLD_THRESHOLD = new BigDecimal("5000000");
    private static final BigDecimal SILVER_THRESHOLD = new BigDecimal("2000000");

    public static int calculateLevel(BigDecimal totalSpending) {
        if (totalSpending == null) return 0;
        if (totalSpending.compareTo(DIAMOND_THRESHOLD) >= 0) return 4;
        if (totalSpending.compareTo(PLATINUM_THRESHOLD) >= 0) return 3;
        if (totalSpending.compareTo(GOLD_THRESHOLD) >= 0) return 2;
        if (totalSpending.compareTo(SILVER_THRESHOLD) >= 0) return 1;
        return 0;
    }
}
