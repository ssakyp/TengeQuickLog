package com.tenge.quicklog.data

import java.util.UUID

/** Whether an entry adds money to the balance or takes money away from it. */
enum class TxType { INCOME, EXPENSE }

/**
 * A short, fixed list of categories is enough for a quick logger — no need
 * for a full category-management screen here. Each one pairs a label with
 * the type of money movement it usually represents, which lets the "Add"
 * form default sensibly when a category is tapped.
 */
enum class Category(val label: String, val defaultType: TxType) {
    SALARY("Salary", TxType.INCOME),
    FREELANCE("Freelance", TxType.INCOME),
    FOOD("Food", TxType.EXPENSE),
    TRANSPORT("Transport", TxType.EXPENSE),
    SHOPPING("Shopping", TxType.EXPENSE),
    BILLS("Bills", TxType.EXPENSE),
    OTHER("Other", TxType.EXPENSE)
}

/** One logged transaction. Plain Kotlin data class — no Android/Compose dependency here on purpose. */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TxType,
    val category: Category
)

/** Totals derived from the current transaction list — recomputed, never stored. */
data class Summary(val income: Double, val expense: Double) {
    val balance: Double get() = income - expense
}

/** A few starter entries so the list isn't empty on first launch. */
fun sampleTransactions(): List<Transaction> = listOf(
    Transaction(title = "October salary", amount = 350000.0, type = TxType.INCOME, category = Category.SALARY),
    Transaction(title = "Grocery run", amount = 18500.0, type = TxType.EXPENSE, category = Category.FOOD),
    Transaction(title = "Taxi to campus", amount = 2200.0, type = TxType.EXPENSE, category = Category.TRANSPORT),
    Transaction(title = "Logo design gig", amount = 60000.0, type = TxType.INCOME, category = Category.FREELANCE)
)
