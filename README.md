# Tenge QuickLog

A one-screen expense/income logger for Android, built for **Task 2 (Android Application with Jetpack Compose)**. It adapts the data model and purpose of the larger Tenge Tracker project (Pet project) into a small, focused Compose app: log a transaction, see your running balance, and find things again with a search bar and a type filter.

## Features

- **Live summary** — Balance / Income / Expense tiles recomputed from the transaction list every time it changes.
- **Add a transaction** — title, amount, Income/Expense, and one of 7 categories, with inline validation (blank title / non-numeric or zero amount are rejected with a message).
- **Search and filter** — a search field narrows the list by title, and three chips (All / Income / Expense) narrow it by type. Both can be combined.
- **Delete** — remove any entry directly from its row.

## Architecture

- `data/Models.kt` — plain Kotlin (`Transaction`, `Category`, `TxType`, `Summary`).
- `QuickLogViewModel.kt` — owns all state as `StateFlow`. The visible list and the summary totals are derived with `combine`/`map`/`stateIn` instead of being stored and kept in sync by hand.
- `ui/components/Components.kt` — 3 reusable, stateless composables: `SummaryCard`, `SelectableChip`, `TransactionCard`.
- `ui/QuickLogScreen.kt` — the single screen, plus the add-form.
- `MainActivity.kt` — hosts the Compose tree.

### `remember` vs `rememberSaveable` vs `ViewModel`, in this app

| Tool | Used for | Why |
|---|---|---|
| `remember` | Whether the add-form is expanded; the current validation error message | Purely visual / transient — fine to reset on rotation |
| `rememberSaveable` | The add-form's draft fields (title, amount, type, category) | Should survive rotation so a half-typed entry isn't lost, but has no reason to outlive the screen once submitted |
| `ViewModel` (`StateFlow`) | The transaction list, the search text, the type filter | The actual application data — must survive both rotation and (if this screen were ever split up) navigation |

## Running the project

1. Open the project folder in Android Studio (Koala or newer).
2. Let Gradle sync (needs network access to `google()` and `mavenCentral()`).
3. Run the `app` configuration on an emulator or device with API 26+.

No backend, no network permissions, no API keys needed — everything is in-memory for this assignment.

## Screenshots

> Added screenshots after running the app.
