package com.tenge.quicklog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenge.quicklog.QuickLogViewModel
import com.tenge.quicklog.data.Category
import com.tenge.quicklog.data.TxType
import com.tenge.quicklog.ui.components.SelectableChip
import com.tenge.quicklog.ui.components.SummaryCard
import com.tenge.quicklog.ui.components.TransactionCard

/**
 * Everything the
 * person can change lives in [QuickLogViewModel] as [kotlinx.coroutines.flow.StateFlow],
 * so it keeps its values across rotation; only short-lived, purely-visual
 * state (the add-form's draft fields, whether it's expanded) is kept local
 * with `remember`/`rememberSaveable`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogScreen(viewModel: QuickLogViewModel = viewModel()) {
    val transactions by viewModel.visibleTransactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tenge QuickLog") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = innerPadding.calculateTopPadding() + 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("Balance", summary.balance, MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    SummaryCard("Income", summary.income, MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    SummaryCard("Expense", summary.expense, MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                }
            }

            item { AddTransactionForm(onAdd = viewModel::addTransaction) }

            item {
                Column {
                    Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::setQuery,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by title…") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip("All", typeFilter == null) { viewModel.setTypeFilter(null) }
                        SelectableChip("Income", typeFilter == TxType.INCOME) { viewModel.setTypeFilter(TxType.INCOME) }
                        SelectableChip("Expense", typeFilter == TxType.EXPENSE) { viewModel.setTypeFilter(TxType.EXPENSE) }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "Nothing here yet — add your first transaction above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionCard(transaction = tx, onDelete = { viewModel.deleteTransaction(tx.id) })
                }
            }
        }
    }
}

/**
 * Draft form for a new transaction. Its field values are kept with
 * `rememberSaveable` — they're meant to survive rotation (so a half-typed
 * entry isn't lost) but have no reason to live in the ViewModel once
 * submitted, which is exactly the case `rememberSaveable` is for. Whether
 * the form is expanded is purely visual and resets on rotation on purpose,
 * so it uses plain `remember` instead.
 */
@Composable
private fun AddTransactionForm(onAdd: (title: String, amount: Double, type: TxType, category: Category) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(TxType.EXPENSE) }
    var category by rememberSaveable { mutableStateOf(Category.FOOD) }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide" else "Show")
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; error = null },
                    label = { Text("Amount (₸)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip("Income", type == TxType.INCOME) { type = TxType.INCOME }
                    SelectableChip("Expense", type == TxType.EXPENSE) { type = TxType.EXPENSE }
                }

                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CategoryChipRow(selected = category, onSelected = { category = it; type = it.defaultType })

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        when {
                            title.isBlank() -> error = "Enter a title."
                            amount == null || amount <= 0.0 -> error = "Enter a valid amount."
                            else -> {
                                onAdd(title.trim(), amount, type, category)
                                title = ""
                                amountText = ""
                                error = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Transaction")
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(selected: Category, onSelected: (Category) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Category.entries.take(4).forEach { c ->
            SelectableChip(c.label, selected == c, onClick = { onSelected(c) })
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Category.entries.drop(4).forEach { c ->
            SelectableChip(c.label, selected == c, onClick = { onSelected(c) })
        }
    }
}
