package com.smartlauncher.data

enum class AppCategory(
    val chipLabel: String,
    val badgeLabel: String,
    val colorRes: Int,      // used as index into our palette
    val basePriority: Float // slight weight boost for the category itself
) {
    DEFI_TRADING("DeFi & Trading", "DeFi", 4, 0.95f),
    GAMES("Games", "Games", 3, 0.90f),
    DEPIN("DePIN", "DePIN", 5, 0.88f),
    PRIVACY_SECURITY("Privacy & Security", "Secure", 6, 0.82f),
    CONTENT_STREAMING("Content & Streaming", "Media", 2, 0.80f),
    WALLETS("Wallets", "Wallet", 4, 0.93f),
    PRODUCTIVITY("Productivity", "Work", 1, 0.78f),
    SOCIAL_IDENTITY("Social & Identity", "Social", 0, 0.76f),
    AI_AGENTS("AI & Agents", "AI", 5, 0.86f),
    LIFESTYLE("Lifestyle", "Life", 2, 0.72f),
    OTHER("Others", "Other", 6, 0.50f)
}
