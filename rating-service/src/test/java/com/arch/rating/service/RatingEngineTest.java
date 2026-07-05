package com.arch.rating.service;

import com.arch.rating.domain.ProductType;
import com.arch.rating.service.RatingEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class RatingEngineTest {

    private final RatingEngine engine = new RatingEngine();

    @Test
    void autoPremium_isRateTimesCoveragePlusFee() {
        // 0.012 * 50000 + 150 = 750.00
        assertThat(engine.annualPremium(ProductType.AUTO, new BigDecimal("50000")))
            .isEqualByComparingTo("750.00");
    }

    @Test
    void homePremium_isRateTimesCoveragePlusFee() {
        // 0.004 * 100000 + 300 = 700.00
        assertThat(engine.annualPremium(ProductType.HOME, new BigDecimal("100000")))
            .isEqualByComparingTo("700.00");
    }

    @Test
    void umbrellaPremium_isRateTimesCoveragePlusFee() {
        // 0.002 * 100000 + 100 = 300.00
        assertThat(engine.annualPremium(ProductType.UMBRELLA, new BigDecimal("100000")))
            .isEqualByComparingTo("300.00");
    }

    @Test
    void premiumAlwaysHasScaleOfTwo() {
        assertThat(engine.annualPremium(ProductType.AUTO, new BigDecimal("33333")).scale())
            .isEqualTo(2);
    }
}
