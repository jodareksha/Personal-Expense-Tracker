package com.expensetracker.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

fun Long.toDisplayDate(): String = displayFormat.format(Date(this))
