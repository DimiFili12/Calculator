package com.example.calculator.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calculator.CalculatorApplication
import com.example.calculator.data.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import java.math.MathContext

sealed interface CalculatorUiState {
    data class Success(
        val calculations: List<StateFlow<String>>,
        val currencies: StateFlow<List<String>>,
        val chosenCurrencies: StateFlow<Map<Int, String>>
    ) : CalculatorUiState
    data class Error(val calculations: List<StateFlow<String>>) : CalculatorUiState
    data class Loading(val calculations: List<StateFlow<String>>) : CalculatorUiState
}

class HomeViewModel(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _calculatedTotal = MutableStateFlow("")
    val calculatedTotal: StateFlow<String> = _calculatedTotal

    private val _convertedTotal = MutableStateFlow("")
    val convertedTotal: StateFlow<String> = _convertedTotal

    var calculatorUiState: CalculatorUiState by mutableStateOf(CalculatorUiState.Loading(listOf(calculatedTotal)))
        private set

    private val _allCurrencies = MutableStateFlow<Map<String, BigDecimal>>(emptyMap())
    val allCurrencies: StateFlow<List<String>> = _allCurrencies.map {
        it.keys.toList()
    }
        .stateIn(
            scope =  viewModelScope,
            started =  SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val initialChosenCurrencies: Map<Int, String> = mapOf(
        0 to "EUR",
        1 to "USD"
    )

    private val _chosenCurrencies = MutableStateFlow(initialChosenCurrencies)
    val chosenCurrencies: StateFlow<Map<Int, String>> = _chosenCurrencies

    private var firstNumber = ""
    private var operator = ""
    private var secondNumber = ""
    private var total = BigDecimal(0)
    private var isAfterCalculateTotal = false

    init {
        getLatestRates()
    }

    fun putNumber(digit: Int) {
        if (!isAfterCalculateTotal) {
            if (operator.isEmpty()) {
                firstNumber += digit
                _calculatedTotal.value = firstNumber
            } else {
                secondNumber += digit
                _calculatedTotal.value = firstNumber + operator + secondNumber
            }
        } else {
            if (operator.isEmpty()) {
                firstNumber = digit.toString()
                _calculatedTotal.value = firstNumber
            } else {
                secondNumber = digit.toString()
                _calculatedTotal.value = firstNumber + operator + secondNumber
            }
            isAfterCalculateTotal = false
        }
        _convertedTotal.value = ""
    }

    fun putOperator(inputOperator: String) {
        if (_calculatedTotal.value.isNotEmpty()) {
            if (operator.isNotEmpty()) {
                calculateTotal()
            }
            isAfterCalculateTotal = false
            operator = inputOperator
            _calculatedTotal.value = firstNumber + operator
        }
    }

    fun putDecimal() {
        if (!isAfterCalculateTotal) {
            if (operator.isEmpty()) {
                if (firstNumber.isNotEmpty()) {
                    if (!firstNumber.contains(".")) {
                        firstNumber += "."
                        _calculatedTotal.value = firstNumber
                    }
                } else {
                    firstNumber = "0."
                    _calculatedTotal.value = firstNumber
                }
            } else {
                if (secondNumber.isNotEmpty()) {
                    if (!secondNumber.contains(".")) {
                        secondNumber += "."
                        _calculatedTotal.value = firstNumber + operator + secondNumber
                    }
                } else {
                    secondNumber = "0."
                    _calculatedTotal.value = firstNumber + operator + secondNumber
                }
            }
        } else {
            firstNumber = "0."
            isAfterCalculateTotal = false
            _calculatedTotal.value = firstNumber
        }
    }

    fun calculateTotal() {
        if (operator.isNotEmpty() && secondNumber.isNotEmpty()) {
            try {
                when (operator) {
                    "+" -> addNumbers(firstNumber.toBigDecimal(), secondNumber.toBigDecimal())
                    "-" -> subtractNumbers(firstNumber.toBigDecimal(), secondNumber.toBigDecimal())
                    "x" -> multiplyNumbers(firstNumber.toBigDecimal(), secondNumber.toBigDecimal())
                    "/" -> divideNumbers(firstNumber.toBigDecimal(), secondNumber.toBigDecimal())
                }
                firstNumber = total.toPlainString()
                _calculatedTotal.value = firstNumber
                isAfterCalculateTotal = true
                secondNumber = ""
                operator = ""
            } catch (e: NumberFormatException) {
                _calculatedTotal.value = "Error"
                isAfterCalculateTotal = true
                secondNumber = ""
                operator = ""
            }
        }
    }

    private fun addNumbers(x: BigDecimal, y: BigDecimal) {
        total = x.add(y, MathContext.DECIMAL64).stripTrailingZeros()
    }

    private fun subtractNumbers(x: BigDecimal, y: BigDecimal) {
        total = x.subtract(y, MathContext.DECIMAL64).stripTrailingZeros()
    }

    private fun multiplyNumbers(x: BigDecimal, y: BigDecimal) {
        total = x.multiply(y, MathContext.DECIMAL64).stripTrailingZeros()
    }

    private fun divideNumbers(x: BigDecimal, y: BigDecimal) {
        if (y != BigDecimal(0)) {
            total = x.divide(y, MathContext.DECIMAL64).stripTrailingZeros()
        }
    }

    fun deleteAll() {
        firstNumber = ""
        secondNumber = ""
        operator = ""
        total = BigDecimal(0)
        isAfterCalculateTotal = false
        _calculatedTotal.value = firstNumber
        _convertedTotal.value = ""
    }

    fun deleteLastChar() {
        if (_calculatedTotal.value.isNotEmpty()) {
            if (operator.isNotEmpty() && secondNumber.isNotEmpty()) {
                secondNumber = secondNumber.substring(0, secondNumber.length - 1)
                _calculatedTotal.value = firstNumber + operator + secondNumber
            } else if (operator.isNotEmpty() && secondNumber.isEmpty()) {
                operator = ""
                _calculatedTotal.value = firstNumber
            } else if (operator.isEmpty()) {
                firstNumber = firstNumber.substring(0, firstNumber.length - 1)
                _calculatedTotal.value = firstNumber
            }
            isAfterCalculateTotal = false
            _convertedTotal.value = ""
        }
    }

    fun makePercent() {
        if (operator.isEmpty()) {
            if (firstNumber.isNotEmpty()) {
                firstNumber = firstNumber.toBigDecimal()
                    .divide(BigDecimal(100), MathContext.DECIMAL64)
                    .stripTrailingZeros()
                    .toPlainString()
                _calculatedTotal.value = firstNumber
            }
        } else {
            if (secondNumber.isNotEmpty()) {
                secondNumber = secondNumber.toBigDecimal()
                    .divide(BigDecimal(100), MathContext.DECIMAL64)
                    .stripTrailingZeros()
                    .toPlainString()
                _calculatedTotal.value = firstNumber + operator + secondNumber
            }
        }
        _convertedTotal.value = ""
    }

    fun getLatestRates(accessKey: String = "9071cb507d5c2632be6eb8b731a7e1b3") {
        viewModelScope.launch {
            calculatorUiState = CalculatorUiState.Loading(listOf(calculatedTotal))
            calculatorUiState = try {
                val response = currencyRepository.getLatestRates(accessKey = accessKey)
                val allLatestCurrencies: MutableMap<String, BigDecimal> = mutableMapOf()
                response.rates.forEach {
                    allLatestCurrencies[it.key] = it.value
                }
                _allCurrencies.value = allLatestCurrencies.toMap()
                CalculatorUiState.Success(listOf(calculatedTotal, convertedTotal), allCurrencies, chosenCurrencies)
            } catch (e: IOException) {
                CalculatorUiState.Error(listOf(calculatedTotal))
            } catch (e: HttpException) {
                CalculatorUiState.Error(listOf(calculatedTotal))
            }
        }
    }

    fun choseCurrency(id: Int, chosenCurrency: String) {
        _chosenCurrencies.update { currentMap ->
            currentMap.toMutableMap().apply {
                this[id] = chosenCurrency
            }
        }
        _convertedTotal.value = ""
    }

    fun convertCurrencies(
        fromCurrency: String = "EUR",
        toCurrency: String = "USD"
    ) {
        if (firstNumber.isNotEmpty()) {
            try {
                val firstRate = _allCurrencies.value[fromCurrency]
                val secondRate = _allCurrencies.value[toCurrency]
                val ratio = secondRate?.divide(firstRate, MathContext.DECIMAL64)
                val convertedAmount: BigDecimal

                if (secondNumber.isEmpty()) {
                    convertedAmount = firstNumber.toBigDecimal().multiply(ratio, MathContext.DECIMAL64)
                } else {
                    calculateTotal()
                    isAfterCalculateTotal = false
                    convertedAmount = firstNumber.toBigDecimal().multiply(ratio, MathContext.DECIMAL64)
                }
                _convertedTotal.value = convertedAmount.toPlainString()
            } catch (e: NumberFormatException) {
                _calculatedTotal.value = "Error"
                isAfterCalculateTotal = true
                secondNumber = ""
                operator = ""
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CalculatorApplication)
                val currencyRepository = application.container.currencyRepository
                HomeViewModel(currencyRepository = currencyRepository)
            }
        }
    }
}