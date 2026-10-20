package com.tenge.quicklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenge.quicklog.data.Category
import com.tenge.quicklog.data.Summary
import com.tenge.quicklog.data.Transaction
import com.tenge.quicklog.data.TxType
import com.tenge.quicklog.data.sampleTransactions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for the whole screen. All state lives here as
 * [StateFlow], so it survives configuration changes (rotation) without any
 * extra work — the Activity/Compose tree gets recreated, this ViewModel does
 * not. Filtering and totals are plain Kotlin [combine]/[map] pipelines over
 * the underlying list, recomputed reactively instead of stored separately.
 */
class QuickLogViewModel : ViewModel() {

    private val _transactions = MutableStateFlow(sampleTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _typeFilter = MutableStateFlow<TxType?>(null)
    val typeFilter: StateFlow<TxType?> = _typeFilter

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    /** What the list actually shows: the full list narrowed by the active type filter and search text. */
    val visibleTransactions: StateFlow<List<Transaction>> =
        combine(_transactions, _typeFilter, _query) { all, type, query ->
            all.filter { tx ->
                (type == null || tx.type == type) &&
                    (query.isBlank() || tx.title.contains(query, ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), sampleTransactions())

    /** Income/expense/balance over *all* transactions, regardless of the active filter. */
    val summary: StateFlow<Summary> = _transactions
        .map { list ->
            Summary(
                income = list.filter { it.type == TxType.INCOME }.sumOf { it.amount },
                expense = list.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Summary(0.0, 0.0))

    fun addTransaction(title: String, amount: Double, type: TxType, category: Category) {
        _transactions.update { it + Transaction(title = title, amount = amount, type = type, category = category) }
    }

    fun deleteTransaction(id: String) {
        _transactions.update { list -> list.filterNot { it.id == id } }
    }

    fun setTypeFilter(type: TxType?) {
        _typeFilter.value = type
    }

    fun setQuery(text: String) {
        _query.value = text
    }
}
