package com.example.calculator

import android.app.Application
import com.example.calculator.data.AppContainer
import com.example.calculator.data.DefaultAppContainer

class CalculatorApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}