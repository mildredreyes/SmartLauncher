package com.smartlauncher.data

import android.content.pm.ApplicationInfo

/**
 * Classifies an installed app into one of our smart categories.
 *
 * Strategy:
 *  1. Collect signals from exact package-prefix match, label keyword match, and
 *     Android system app category.
 *  2. Use the union of those signals to choose a single best category.
 *  3. Prefer consensus first, then package-prefix, then label keyword, then
 *     system category.
 */
object CategoryClassifier {

    private val packageRules: List<Pair<String, AppCategory>> = listOf(
        // Wallets
        "app.phantom" to AppCategory.WALLETS,
        "com.solflare.mobile" to AppCategory.WALLETS,
        "com.backpack" to AppCategory.WALLETS,
        "io.metamask" to AppCategory.WALLETS,
        "com.wallet.crypto.trustapp" to AppCategory.WALLETS,
        "exodusmovement.exodus" to AppCategory.WALLETS,
        "com.coinbase.wallet" to AppCategory.WALLETS,
        "com.ledger.live" to AppCategory.WALLETS,

        // DeFi & Trading
        "com.coinbase.android" to AppCategory.DEFI_TRADING,
        "com.binance.dev" to AppCategory.DEFI_TRADING,
        "com.kraken.invest.app" to AppCategory.DEFI_TRADING,
        "com.okinc.okex.gp" to AppCategory.DEFI_TRADING,
        "com.bybit.app" to AppCategory.DEFI_TRADING,
        "com.robinhood.android" to AppCategory.DEFI_TRADING,
        "com.tradingview.tradingviewapp" to AppCategory.DEFI_TRADING,
        "com.paypal.android.p2pmobile" to AppCategory.DEFI_TRADING,
        "com.venmo" to AppCategory.DEFI_TRADING,
        "com.stripe.dashboard" to AppCategory.DEFI_TRADING,

        // DePIN
        "com.helium.wallet.app" to AppCategory.DEPIN,
        "com.helium.mobile" to AppCategory.DEPIN,
        "com.uprock.mobile" to AppCategory.DEPIN,
        "ai.uprock.mobile" to AppCategory.DEPIN,
        "com.hivemapper.app" to AppCategory.DEPIN,
        "xyz.geodnet" to AppCategory.DEPIN,
        "network.grass" to AppCategory.DEPIN,

        // NFT marketplaces roll into Wallets
        "io.magiceden.mobile" to AppCategory.WALLETS,
        "io.opensea.mobile" to AppCategory.WALLETS,
        "com.fractal" to AppCategory.WALLETS,

        // Privacy & Security
        "com.protonvpn.android" to AppCategory.PRIVACY_SECURITY,
        "com.expressvpn.vpn" to AppCategory.PRIVACY_SECURITY,
        "com.nordvpn.android" to AppCategory.PRIVACY_SECURITY,
        "com.authy.authy" to AppCategory.PRIVACY_SECURITY,
        "com.google.android.apps.authenticator2" to AppCategory.PRIVACY_SECURITY,
        "com.lastpass.lpandroid" to AppCategory.PRIVACY_SECURITY,
        "com.x8bit.bitwarden" to AppCategory.PRIVACY_SECURITY,
        "com.onepassword.android" to AppCategory.PRIVACY_SECURITY,
        "org.torproject.torbrowser" to AppCategory.PRIVACY_SECURITY,

        // Content & Streaming
        "com.netflix" to AppCategory.CONTENT_STREAMING,
        "com.spotify" to AppCategory.CONTENT_STREAMING,
        "com.google.android.youtube" to AppCategory.CONTENT_STREAMING,
        "com.google.android.apps.youtube.music" to AppCategory.CONTENT_STREAMING,
        "com.amazon.avod" to AppCategory.CONTENT_STREAMING,
        "com.disney" to AppCategory.CONTENT_STREAMING,
        "com.hbo" to AppCategory.CONTENT_STREAMING,
        "com.twitch" to AppCategory.CONTENT_STREAMING,
        "com.soundcloud" to AppCategory.CONTENT_STREAMING,
        "com.crunchyroll" to AppCategory.CONTENT_STREAMING,
        "com.hulu" to AppCategory.CONTENT_STREAMING,

        // Productivity
        "com.google.android.gm" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.docs" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.sheets" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.slides" to AppCategory.PRODUCTIVITY,
        "com.google.android.calendar" to AppCategory.PRODUCTIVITY,
        "com.google.android.keep" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office" to AppCategory.PRODUCTIVITY,
        "com.microsoft.teams" to AppCategory.PRODUCTIVITY,
        "com.slack" to AppCategory.PRODUCTIVITY,
        "com.notion" to AppCategory.PRODUCTIVITY,
        "com.todoist" to AppCategory.PRODUCTIVITY,
        "com.trello" to AppCategory.PRODUCTIVITY,
        "com.asana" to AppCategory.PRODUCTIVITY,
        "com.dropbox" to AppCategory.PRODUCTIVITY,
        "com.evernote" to AppCategory.PRODUCTIVITY,
        "com.adobe.reader" to AppCategory.PRODUCTIVITY,
        "us.zoom" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.meetings" to AppCategory.PRODUCTIVITY,

        // Social & Identity
        "com.whatsapp" to AppCategory.SOCIAL_IDENTITY,
        "com.facebook" to AppCategory.SOCIAL_IDENTITY,
        "com.instagram" to AppCategory.SOCIAL_IDENTITY,
        "com.twitter" to AppCategory.SOCIAL_IDENTITY,
        "com.xcorp.android" to AppCategory.SOCIAL_IDENTITY,
        "com.snapchat" to AppCategory.SOCIAL_IDENTITY,
        "com.discord" to AppCategory.SOCIAL_IDENTITY,
        "com.linkedin" to AppCategory.SOCIAL_IDENTITY,
        "com.telegram" to AppCategory.SOCIAL_IDENTITY,
        "com.viber" to AppCategory.SOCIAL_IDENTITY,
        "com.skype" to AppCategory.SOCIAL_IDENTITY,
        "com.tiktok" to AppCategory.SOCIAL_IDENTITY,
        "com.zhiliaoapp" to AppCategory.SOCIAL_IDENTITY,
        "com.reddit" to AppCategory.SOCIAL_IDENTITY,
        "org.thoughtcrime.securesms" to AppCategory.SOCIAL_IDENTITY,

        // AI & Agents
        "com.openai.chatgpt" to AppCategory.AI_AGENTS,
        "com.anthropic.claude" to AppCategory.AI_AGENTS,
        "com.google.android.apps.bard" to AppCategory.AI_AGENTS,
        "com.perplexity.ai" to AppCategory.AI_AGENTS,
        "com.microsoft.copilot" to AppCategory.AI_AGENTS,
        "ai.character.app" to AppCategory.AI_AGENTS,
        "com.quora.poe" to AppCategory.AI_AGENTS,

        // Games
        "com.mojang" to AppCategory.GAMES,
        "com.supercell" to AppCategory.GAMES,
        "com.king" to AppCategory.GAMES,
        "com.rovio" to AppCategory.GAMES,
        "com.ea." to AppCategory.GAMES,
        "com.activision" to AppCategory.GAMES,
        "com.garena" to AppCategory.GAMES,
        "com.mihoyo" to AppCategory.GAMES,
        "com.riotgames" to AppCategory.GAMES,
        "com.epicgames" to AppCategory.GAMES,
        "com.playdemic" to AppCategory.GAMES,
        "com.halfbrick" to AppCategory.GAMES,
        "com.imangi" to AppCategory.GAMES,

        // Lifestyle
        "com.amazon.mShop.android.shopping" to AppCategory.LIFESTYLE,
        "com.shopee" to AppCategory.LIFESTYLE,
        "com.lazada" to AppCategory.LIFESTYLE,
        "com.airbnb.android" to AppCategory.LIFESTYLE,
        "com.ubercab" to AppCategory.LIFESTYLE,
        "com.ubercab.eats" to AppCategory.LIFESTYLE,
        "com.grabtaxi.passenger" to AppCategory.LIFESTYLE,
        "com.zomato" to AppCategory.LIFESTYLE,
        "com.google.android.apps.maps" to AppCategory.LIFESTYLE,
        "com.google.android.apps.photos" to AppCategory.LIFESTYLE,
        "com.google.android.apps.camera" to AppCategory.LIFESTYLE,
        "com.android.camera" to AppCategory.LIFESTYLE,
        "com.google.android.dialer" to AppCategory.LIFESTYLE,
        "com.android.contacts" to AppCategory.LIFESTYLE,
        "com.google.android.clock" to AppCategory.LIFESTYLE
    )

    private val labelKeywords: List<Pair<String, AppCategory>> = listOf(
        // Wallets
        "wallet" to AppCategory.WALLETS,
        "phantom" to AppCategory.WALLETS,
        "solflare" to AppCategory.WALLETS,
        "backpack" to AppCategory.WALLETS,
        "metamask" to AppCategory.WALLETS,
        "trust wallet" to AppCategory.WALLETS,
        "ledger" to AppCategory.WALLETS,

        // DeFi & Trading
        "defi" to AppCategory.DEFI_TRADING,
        "trade" to AppCategory.DEFI_TRADING,
        "trading" to AppCategory.DEFI_TRADING,
        "exchange" to AppCategory.DEFI_TRADING,
        "swap" to AppCategory.DEFI_TRADING,
        "staking" to AppCategory.DEFI_TRADING,
        "yield" to AppCategory.DEFI_TRADING,
        "futures" to AppCategory.DEFI_TRADING,
        "perps" to AppCategory.DEFI_TRADING,
        "crypto" to AppCategory.DEFI_TRADING,

        // DePIN
        "depin" to AppCategory.DEPIN,
        "miner" to AppCategory.DEPIN,
        "mining" to AppCategory.DEPIN,
        "node" to AppCategory.DEPIN,
        "hotspot" to AppCategory.DEPIN,
        "helium" to AppCategory.DEPIN,
        "hivemapper" to AppCategory.DEPIN,
        "uprock" to AppCategory.DEPIN,
        "grass" to AppCategory.DEPIN,

        // NFT-related apps roll into Wallets
        "nft" to AppCategory.WALLETS,
        "collectible" to AppCategory.WALLETS,
        "mint" to AppCategory.WALLETS,

        // Privacy & Security
        "vpn" to AppCategory.PRIVACY_SECURITY,
        "authenticator" to AppCategory.PRIVACY_SECURITY,
        "security" to AppCategory.PRIVACY_SECURITY,
        "private" to AppCategory.PRIVACY_SECURITY,
        "password" to AppCategory.PRIVACY_SECURITY,
        "vault" to AppCategory.PRIVACY_SECURITY,

        // Content & Streaming
        "music" to AppCategory.CONTENT_STREAMING,
        "video" to AppCategory.CONTENT_STREAMING,
        "stream" to AppCategory.CONTENT_STREAMING,
        "podcast" to AppCategory.CONTENT_STREAMING,
        "radio" to AppCategory.CONTENT_STREAMING,
        "movie" to AppCategory.CONTENT_STREAMING,
        "tv" to AppCategory.CONTENT_STREAMING,
        "player" to AppCategory.CONTENT_STREAMING,

        // Productivity
        "office" to AppCategory.PRODUCTIVITY,
        "email" to AppCategory.PRODUCTIVITY,
        "mail" to AppCategory.PRODUCTIVITY,
        "calendar" to AppCategory.PRODUCTIVITY,
        "notes" to AppCategory.PRODUCTIVITY,
        "task" to AppCategory.PRODUCTIVITY,
        "todo" to AppCategory.PRODUCTIVITY,
        "pdf" to AppCategory.PRODUCTIVITY,
        "scanner" to AppCategory.PRODUCTIVITY,
        "document" to AppCategory.PRODUCTIVITY,

        // Social & Identity
        "chat" to AppCategory.SOCIAL_IDENTITY,
        "message" to AppCategory.SOCIAL_IDENTITY,
        "messenger" to AppCategory.SOCIAL_IDENTITY,
        "social" to AppCategory.SOCIAL_IDENTITY,
        "dating" to AppCategory.SOCIAL_IDENTITY,
        "community" to AppCategory.SOCIAL_IDENTITY,
        "identity" to AppCategory.SOCIAL_IDENTITY,

        // AI & Agents
        "ai" to AppCategory.AI_AGENTS,
        "agent" to AppCategory.AI_AGENTS,
        "assistant" to AppCategory.AI_AGENTS,
        "copilot" to AppCategory.AI_AGENTS,
        "gpt" to AppCategory.AI_AGENTS,
        "claude" to AppCategory.AI_AGENTS,
        "gemini" to AppCategory.AI_AGENTS,

        // Games
        "game" to AppCategory.GAMES,
        "puzzle" to AppCategory.GAMES,
        "chess" to AppCategory.GAMES,
        "rpg" to AppCategory.GAMES,
        "arena" to AppCategory.GAMES,
        "battle" to AppCategory.GAMES,
        "quest" to AppCategory.GAMES,
        "racing" to AppCategory.GAMES,

        // Lifestyle
        "shop" to AppCategory.LIFESTYLE,
        "store" to AppCategory.LIFESTYLE,
        "food" to AppCategory.LIFESTYLE,
        "travel" to AppCategory.LIFESTYLE,
        "camera" to AppCategory.LIFESTYLE,
        "photo" to AppCategory.LIFESTYLE,
        "maps" to AppCategory.LIFESTYLE,
        "health" to AppCategory.LIFESTYLE,
        "fitness" to AppCategory.LIFESTYLE,
        "lifestyle" to AppCategory.LIFESTYLE
    )

    fun classify(packageName: String, label: String, systemCategory: Int? = null): AppCategory {
        val pkg = packageName.lowercase()
        val lbl = label.lowercase()

        val packageSignal = packageRules.firstOrNull { (prefix, _) ->
            pkg.startsWith(prefix.lowercase())
        }?.second

        val labelSignal = labelKeywords.firstOrNull { (keyword, _) ->
            lbl.contains(keyword)
        }?.second

        val systemSignal = systemCategory?.let(::mapSystemCategory)

        val signals = listOfNotNull(packageSignal, labelSignal, systemSignal)
        if (signals.isEmpty()) return AppCategory.OTHER

        signals
            .groupingBy { it }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<AppCategory, Int>> { it.value }.thenBy { priorityOf(it.key) })
            ?.takeIf { it.value >= 2 }
            ?.let { return it.key }

        packageSignal?.let { return it }
        labelSignal?.let { return it }
        systemSignal?.let { return it }

        return AppCategory.OTHER
    }

    private fun mapSystemCategory(systemCategory: Int): AppCategory? = when (systemCategory) {
        ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
        ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL_IDENTITY
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_NEWS,
        ApplicationInfo.CATEGORY_IMAGE -> AppCategory.CONTENT_STREAMING
        ApplicationInfo.CATEGORY_MAPS -> AppCategory.LIFESTYLE
        else -> null
    }

    private fun priorityOf(category: AppCategory): Int = when (category) {
        AppCategory.DEFI_TRADING -> 11
        AppCategory.WALLETS -> 10
        AppCategory.DEPIN -> 9
        AppCategory.PRIVACY_SECURITY -> 8
        AppCategory.GAMES -> 7
        AppCategory.CONTENT_STREAMING -> 6
        AppCategory.PRODUCTIVITY -> 5
        AppCategory.SOCIAL_IDENTITY -> 4
        AppCategory.AI_AGENTS -> 3
        AppCategory.LIFESTYLE -> 2
        AppCategory.OTHER -> 0
    }
}
