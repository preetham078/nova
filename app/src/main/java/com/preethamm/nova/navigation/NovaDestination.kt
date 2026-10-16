package com.preethamm.nova.navigation

enum class NovaDestination(
    val route: String,
    val label: String
) {
    Home("home", "Home"),
    Accounts("accounts", "Accounts"),
    Settings("settings", "Settings")
}
