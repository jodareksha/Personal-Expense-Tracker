# Personal Expense Tracker

A small Android app for logging expenses — add one, see the list, watch the total update. Built for the take-home assignment, using Kotlin + Jetpack Compose.

## What it does

- Add an expense with a title, amount, and category (date is stamped automatically)
- See all expenses in a list, newest first, with a running total at the top
- Delete an expense from the list
- Data survives an app restart (Room/SQLite under the hood)
- Handles the empty state, a loading spinner on first launch, and inline form validation

## Why I built it this way

I had a few real decisions to make here, so instead of just listing the tech stack, here's the reasoning behind each one.

**Kotlin + Compose, not XML views.** The assignment listed Compose as the preferred option for Android, and honestly it's also just less code for a two-screen app — no XML layouts, no `findViewById`, no separate adapter classes for the list. `LazyColumn` handles the expense list in a few lines.

**MVVM, but simple.** There's one `ExpenseViewModel` shared across both screens (passed in through the nav graph), rather than one ViewModel per screen. For an app this small, two ViewModels would mean either duplicating the expense list state or wiring up a way to share it anyway, which felt like more machinery than the problem needed. If this app grew — more screens, more independent pieces of state — I'd split it.

**Room for persistence.** DataStore would've been fine too, but a list of records with a "give me the total" query is exactly what a real database is for, and Room's Flow support means the UI just reacts to the table changing — no manual "refresh the list" calls anywhere.

**A repository interface, not just a class.** `ExpenseRepository` is an interface with one real implementation (`RoomExpenseRepository`). This is a little bit of extra ceremony for an app this size, but it's what let me write the ViewModel unit tests against an in-memory fake instead of spinning up an actual database — and it's the seam where this could later talk to a backend instead of a local DB without the ViewModel knowing the difference.

**No Hilt/Koin.** I used a plain `ViewModelFactory` and built the repository by hand in `MainActivity` instead of pulling in a DI framework. With one repository and one ViewModel, Hilt would mostly be boilerplate and an extra thing to explain. I'd reach for it the moment there's a second dependency graph to manage.

**Validation lives in the ViewModel, not the UI.** The Add Expense screen doesn't know what "valid" means — it just shows whatever error string the ViewModel hands it. Title can't be blank, amount has to parse as a positive number. That logic is what's actually under test (see below), because it's the part most likely to have an off-by-one bug, and it's not something you can accidentally break by rearranging a Composable.

**Loading/empty/error are explicit state, not inferred.** It would've been shorter to just check `if (list.isEmpty())` and call it the empty state. I didn't do that because on first launch the list starts empty *before* Room has actually loaded anything — checking size alone would flash the "no expenses" message for a split second even when data is on its way. So the UI state has `isLoading`, `errorMessage`, and the list as three separate, explicit fields.

## Project structure

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
└── util/DateFormat.kt
```

## Testing

There's a small suite of ViewModel unit tests (`app/src/test/.../ExpenseViewModelTest.kt`) covering the validation rules — blank title, non-numeric amount, zero/negative amount, and the happy path. They run against a fake in-memory repository, so no emulator or database is needed; `./gradlew test` is enough.

I didn't add UI/instrumentation tests. Given the 4–6 hour budget, I'd rather have a few solid tests on the logic that's easy to get subtly wrong than a shallow test on a Composable that just checks a screen renders.

## Running it

1. Clone the repo and open it in Android Studio (Ladybug or newer).
2. Let Gradle sync — it'll pull the Compose BOM, Room, and Navigation Compose automatically.
3. Run on an emulator or device (min SDK 24).

If you're building from the command line instead of Android Studio, run `gradle wrapper` once inside the project to generate `gradlew`/`gradlew.bat`, since those wrapper binaries aren't checked into this repo.

## What I'd add with more time

These were left out on purpose, not forgotten — the assignment explicitly calls them optional and I'd rather ship a solid core than a half-done pile of extras:

- **Filtering/search** — by category or date range, once there's enough data for it to matter
- **Edit an existing expense** — right now you can add and delete, not edit; that's a quick addition (reuse the Add screen with a pre-filled state) but I ran out of scope budget
- **A proper category model** — categories are currently a hardcoded list of strings; a small `Category` table would let users add their own
- **Instrumentation tests** for the Compose screens themselves
- **A monthly summary / chart view**, since the total is already tracked at the DB level

## A note on AI usage

I used AI (Claude) as a research and learning tool while building this — mainly to get a clear picture of current Compose/Room/MVVM patterns before writing anything, and as a reference to check my implementation against once I had a first pass working. The architecture, the ViewModel logic, the validation rules, and the actual code are things I wrote and typed myself; I used AI the way I'd use documentation or a senior engineer's code review, not as a code generator I copy-pasted from.

Happy to walk through any part of this in more detail — including why I made a given choice, or what I'd change if I had more time.
