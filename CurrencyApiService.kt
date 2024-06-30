package com.example.calculator.network

import com.example.calculator.models.CurrencyRatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") accessKey: String = "9071cb507d5c2632be6eb8b731a7e1b3",
    ): CurrencyRatesResponse
}