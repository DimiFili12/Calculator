package com.example.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.models.MyButton
import com.example.calculator.models.MyButtons
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    when(val state = viewModel.calculatorUiState) {
        is CalculatorUiState.Loading -> {
            val buttons = MyButtons.getButtons(viewModel, state)

            MainScreen(
                buttons = buttons,
                calculationResults = state.calculations,
                modifier = modifier
            )
        }
        is CalculatorUiState.Success -> {
            val currencies by state.currencies.collectAsState()
            val chosenCurrencies by state.chosenCurrencies.collectAsState()
            val buttons = MyButtons.getButtons(viewModel, state, chosenCurrencies)

            MainScreen(
                buttons = buttons,
                currencies = currencies,
                chosenCurrency = chosenCurrencies,
                numberOfResultRows = state.calculations.size.toFloat(),
                onCurrencyChosen =  viewModel::choseCurrency,
                calculationResults = state.calculations,
                modifier = modifier
            )
        }
        is CalculatorUiState.Error -> {
            val buttons = MyButtons.getButtons(viewModel, state)

            MainScreen(
                buttons = buttons,
                calculationResults = state.calculations,
                modifier = modifier
            )
        }
    }
}

@Composable
fun MainScreen(
    buttons: List<List<MyButton>>,
    currencies: List<String>? = null,
    chosenCurrency: Map<Int, String>? = null,
    numberOfResultRows: Float = 1F,
    onCurrencyChosen: ((Int, String) -> Unit)? = null,
    calculationResults: List<StateFlow<String>>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            calculationResults.forEachIndexed() { index, calculationResult ->
                val result by calculationResult.collectAsState()
                ResultRow(
                    id = index,
                    result = result,
                    chosenCurrencies = chosenCurrency,
                    allCurrencies = currencies,
                    onCurrencyChosen = { rowId, chosenItem ->
                        if (onCurrencyChosen != null) {
                            onCurrencyChosen(rowId, chosenItem)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .weight(1f / numberOfResultRows)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            CustomKeyboard(buttons = buttons)
        }
    }
}

@Composable
fun ResultRow(
    id: Int,
    result: String,
    chosenCurrencies: Map<Int, String>? = null,
    allCurrencies: List<String>? = listOf(),
    onCurrencyChosen: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        var showDialog by rememberSaveable { mutableStateOf(false) }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (chosenCurrencies != null) {
                    Button(
                        onClick = { showDialog = true },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = chosenCurrencies.values.elementAt(id),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(align = Alignment.End)
                        .padding(horizontal = 4.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = result,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        if (showDialog && allCurrencies != null) {
            CurrencySelectionDialog(
                allCurrencies = allCurrencies,
                onCurrencyChosen = { chosenCurrency ->
                    onCurrencyChosen(id, chosenCurrency)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    allCurrencies: List<String>,
    onCurrencyChosen: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select a currency:")
        },
        text = {
            LazyColumn {
                items(allCurrencies) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencyChosen(item) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomKeyboard(
    buttons: List<List<MyButton>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        buttons.forEach { row ->
            ButtonsRow(buttons = row)
        }
    }
}

@Composable
fun ButtonsRow(buttons: List<MyButton>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        buttons.forEach { myButton ->
            if (myButton.icon != null) {
                IconOutlinedButton(
                    icon = myButton.icon,
                    onButtonClick = myButton.onButtonClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            } else {
                myButton.label?.let {
                    LabelOutlinedButton(
                        label = it,
                        isNumber = myButton.isNumber,
                        onButtonClick = myButton.onButtonClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IconOutlinedButton(
    icon: Int,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onButtonClick,
        colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.secondary),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.Red
        )
    }
}

@Composable
fun LabelOutlinedButton(
    label: String,
    isNumber: Boolean,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onButtonClick,
        colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.secondary),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = if (isNumber) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                Color.Red
            },
            fontSize = 16.sp,
            fontWeight = if (isNumber) {
                FontWeight.SemiBold
            } else {
                FontWeight.ExtraBold
            }
        )
    }
}