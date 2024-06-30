package com.example.calculator.models

import java.math.BigDecimal

data class CurrencyRatesResponse (
    val rates: Map<String, BigDecimal>
)

