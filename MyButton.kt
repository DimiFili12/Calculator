package com.example.calculator.models

import androidx.annotation.DrawableRes
import com.example.calculator.R
import com.example.calculator.ui.screens.CalculatorUiState
import com.example.calculator.ui.screens.HomeViewModel

data class MyButton (
    val label: String? = null,
    val isNumber: Boolean,
    val onButtonClick: () -> Unit,
    @DrawableRes val icon: Int? = null
)

object MyButtons {
    fun getButtons(
        viewModel: HomeViewModel,
        calculatorUiState: CalculatorUiState,
        chosenCurrencies: Map<Int, String>? = null
    ): List<List<MyButton>> {
        val convertButtonClick: () -> Unit
        val convertIcon: Int

        when (calculatorUiState) {
            is CalculatorUiState.Loading -> {
                convertButtonClick = {}
                convertIcon = R.drawable.baseline_timelapse_24
            }
            is CalculatorUiState.Success -> {
                convertButtonClick = {
                    chosenCurrencies?.get(0)?.let { from ->
                        chosenCurrencies[1]?.let { to ->
                            viewModel.convertCurrencies(from, to)
                        }
                    }
                }
                convertIcon = R.drawable.baseline_currency_exchange_24
            }
            is CalculatorUiState.Error -> {
                convertButtonClick = { viewModel.getLatestRates() }
                convertIcon = R.drawable.baseline_change_circle_24
            }
        }

        return listOf(
            listOf(
                MyButton(
                    label = "C",
                    isNumber = false,
                    onButtonClick = { viewModel.deleteAll() }
                ),
                MyButton(
                    icon = R.drawable.outline_backspace_24,
                    isNumber = false,
                    onButtonClick = { viewModel.deleteLastChar() }
                ),
                MyButton(
                    icon = R.drawable.baseline_percent_24,
                    isNumber = false,
                    onButtonClick = { viewModel.makePercent() }
                ),
                MyButton(
                    label = "/",
                    isNumber = false,
                    onButtonClick = { viewModel.putOperator("/") }
                )
            ),
            listOf(
                MyButton(
                    label = "7",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(7) }
                ),
                MyButton(
                    label = "8",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(8) }
                ),
                MyButton(
                    label = "9",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(9) }
                ),
                MyButton(
                    label = "x",
                    isNumber = false,
                    onButtonClick = { viewModel.putOperator("x") }
                )
            ),
            listOf(
                MyButton(
                    label = "4",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(4) }
                ),
                MyButton(
                    label = "5",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(5) }
                ),
                MyButton(
                    label = "6",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(6) }
                ),
                MyButton(
                    label = "-",
                    isNumber = false,
                    onButtonClick = { viewModel.putOperator("-") }
                )
            ),
            listOf(
                MyButton(
                    label = "1",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(1) }
                ),
                MyButton(
                    label = "2",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(2) }
                ),
                MyButton(
                    label = "3",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(3) }
                ),
                MyButton(
                    label = "+",
                    isNumber = false,
                    onButtonClick = { viewModel.putOperator("+") }
                )
            ),
            listOf(
                MyButton(
                    icon = convertIcon,
                    isNumber = false,
                    onButtonClick = convertButtonClick
                ),
                MyButton(
                    label = "0",
                    isNumber = true,
                    onButtonClick = { viewModel.putNumber(0) }
                ),
                MyButton(
                    label = ".",
                    isNumber = false,
                    onButtonClick = { viewModel.putDecimal() }
                ),
                MyButton(
                    icon = R.drawable.outline_drag_handle_24,
                    isNumber = false,
                    onButtonClick = { viewModel.calculateTotal() }
                )
            )
        )
    }
}