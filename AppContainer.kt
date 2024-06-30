package com.example.calculator.data

import com.example.calculator.network.CurrencyApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val currencyRepository: CurrencyRepository
}

class DefaultAppContainer : AppContainer {
    private val baseUrl = "http://data.fixer.io/api/"

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()

    private val retrofitService: CurrencyApiService by lazy {
        retrofit.create(CurrencyApiService::class.java)
    }

    override val currencyRepository: CurrencyRepository by lazy {
        NetworkCurrencyRepository(retrofitService)
    }
}