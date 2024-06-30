package com.example.calculator.data

import com.example.calculator.models.CurrencyRatesResponse
import com.example.calculator.network.CurrencyApiService


interface CurrencyRepository {
    suspend fun getLatestRates(accessKey: String): CurrencyRatesResponse
}

class NetworkCurrencyRepository(
    private val currencyApiService: CurrencyApiService
) : CurrencyRepository {
    override suspend fun getLatestRates(accessKey: String): CurrencyRatesResponse =
        currencyApiService.getLatestRates(accessKey = accessKey)
}