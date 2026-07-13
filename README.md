# Personal Expense Tracker

A small Android app for logging expenses — add one, see the list, watch the total update. Built for the take-home assignment, using Kotlin + Jetpack Compose.

## What it does

- Add an expense with a title, amount, and category (date is stamped automatically)
- See all expenses in a list, newest first, with a running total (in RM) at the top
- Filter the list by category, with a distinct empty-state message when a filter has zero matches
- Delete an expense from the list
- Data survives an app restart (Room/SQLite under the hood)
- Handles the empty state, a loading spinner on first launch, and inline form validation
- Smooth transitions between loading/empty/error/content, and animated insert/remove in the list

## Why I built it this way

I had a few real decisions to make here, so instead of just listing the tech stack, here's the reasoning behind each one.

**Kotlin + Compose, not XML views.** The assignment listed Compose as the preferred option for Android, and honestly it's also just less code for a two-screen app — no XML layouts, no `findViewById`, no separate adapter classes for the list. `LazyColumn` handles the expense list in a few lines.

**MVVM, but simple.** There's one `ExpenseViewModel` shared across both screens (passed in through the nav graph), rather than one ViewModel per screen. For an app this small, two ViewModels would mean either duplicating the expense list state or wiring up a way to share it anyway, which felt like more machinery than the problem needed. If this app grew — more screens, more independent pieces of state — I'd split it.

**Room for persistence.** DataStore would've been fine too, but a list of records with a "give me the total" query is exactly what a real database is for, and Room's Flow support means the UI just reacts to the table changing — no manual "refresh the list" calls anywhere.

**A repository interface, not just a class.** `ExpenseRepository` is an interface with one real implementation (`RoomExpenseRepository`). This is a little bit of extra ceremony for an app this size, but it's what let me write the ViewModel unit tests against an in-memory fake instead of spinning up an actual database — and it's the seam where this could later talk to a backend instead of a local DB without the ViewModel knowing the difference.

**No Hilt/Koin.** I used a plain `ViewModelFactory` and built the repository by hand in `MainActivity` instead of pulling in a DI framework. With one repository and one ViewModel, Hilt would mostly be boilerplate and an extra thing to explain. I'd reach for it the moment there's a second dependency graph to manage.

**Validation lives in the ViewModel, not the UI.** The Add Expense screen doesn't know what "valid" means — it just shows whatever error string the ViewModel hands it. Title can't be blank, amount has to parse as a positive number. That logic is what's actually under test (see below), because it's the part most likely to have an off-by-one bug, and it's not something you can accidentally break by rearranging a Composable.

**Loading/empty/error are explicit state, not inferred.** It would've been shorter to just check `if (list.isEmpty())` and call it the empty state. I didn't do that because on first launch the list starts empty *before* Room has actually loaded anything — checking size alone would flash the "no expenses" message for a split second even when data is on its way. So the UI state has `isLoading`, `errorMessage`, and the list as three separate, explicit fields.

**The category filter lives in the ViewModel, not as local Compose state.** It would've been quicker to just do `var selectedCategory by remember { mutableStateOf<String?>(null) }` inside the screen and filter the list there. I didn't, for the same reason validation isn't handled in the UI: the filter needs to survive configuration changes and interact with the same `combine()` pipeline that already merges the expense list and total, so it's one more `StateFlow` feeding that pipeline rather than a second, UI-owned source of truth.

**Currency is hardcoded to MYR, not locale-derived.** `NumberFormat.getCurrencyInstance()` without an explicit locale follows whatever region the device is set to — which meant it silently showed EUR on my first run because that's what the emulator's locale resolved to. Since this app isn't meant to be multi-currency, I pinned the formatter to `Locale("ms", "MY")` explicitly in one place (`util/CurrencyFormat.kt`) rather than leaving it locale-dependent.



```
app/src/main/java/com/expensetracker/app/
├── MainActivity.kt              # wires up the DB, repository, ViewModel, nav
├── data/
│   ├── Expense.kt                # Room entity
│   ├── ExpenseDao.kt
│   ├── ExpenseDatabase.kt
│   └── ExpenseRepository.kt      # interface + Room-backed implementation
├── ui/
│   ├── navigation/AppNavigation.kt
│   ├── screens/
│   │   ├── ExpenseListScreen.kt
│   │   └── AddExpenseScreen.kt
│   ├── viewmodel/
│   │   ├── ExpenseViewModel.kt
│   │   ├── ExpenseViewModelFactory.kt
│   │   └── ExpenseUiState.kt
│   └── theme/                    # colors, type, Material 3 theme
└── util/
    ├── DateFormat.kt
    └── CurrencyFormat.kt          # pins display currency to MYR
```

## Testing

There's a small suite of ViewModel unit tests (`app/src/test/.../ExpenseViewModelTest.kt`) covering the validation rules — blank title, non-numeric amount, zero/negative amount, and the happy path — plus a test that the category filter narrows the visible list without touching the overall count. They run against a fake in-memory repository, so no emulator or database is needed; `./gradlew test` is enough.

I didn't add UI/instrumentation tests. Given the 4–6 hour budget, I'd rather have a few solid tests on the logic that's easy to get subtly wrong than a shallow test on a Composable that just checks a screen renders.

## Running it

1. Clone the repo and open it in Android Studio (Ladybug or newer).
2. Let Gradle sync — it'll pull the Compose BOM, Room, and Navigation Compose automatically.
3. Run on an emulator or device (min SDK 24).

If you're building from the command line instead of Android Studio, run `gradle wrapper` once inside the project to generate `gradlew`/`gradlew.bat`, since those wrapper binaries aren't checked into this repo.

## Optional enhancements

The assignment listed five optional items. I ended up adding two of them (categories were already part of the core build; filtering and animations were later additions once I had time to spare):

- **Expense categories** — done (category field + filter chips)
- **Summary (total expenses)** — done (the total card)
- **Filtering** — done (category filter row, with a distinct empty state for "no matches in this category" vs "no expenses at all")
- **Simple animations** — done: list items animate in/out (`Modifier.animateItem()`), the loading/empty/error/content states crossfade instead of snapping, and the save-error banner on the Add screen eases in/out
- **Offline-first considerations** — not really applicable here in the "sync conflict resolution" sense, since there's no remote backend at all; the app is local-only by construction, which is its own answer to "does this work offline"

Not included, on purpose:

- **Search** — filtering by category covers the same underlying need (narrowing the list) with far less UI, given the data model this app has. A text search box would be the natural next step if expenses grew into the hundreds.
- **Edit an existing expense** — add/delete are covered; edit is a quick addition (reuse the Add screen with pre-filled state) but wasn't in the original core requirements and I didn't want to stretch scope further than the optional list already did.
- **A proper category table** — categories are still a hardcoded list of strings rather than their own Room entity. Worth doing if users needed to define custom categories.

## A note on AI usage

I used AI (Claude) as a research and learning tool while building this — mainly to get a clear picture of current Compose/Room/MVVM patterns before writing anything, and as a reference to check my implementation against once I had a first pass working. The architecture, the ViewModel logic, the validation rules, and the actual code are things I wrote and typed myself; I used AI the way I'd use documentation or a senior engineer's code review, not as a code generator I copy-pasted from.

Happy to walk through any part of this in more detail — including why I made a given choice, or what I'd change if I had more time.
