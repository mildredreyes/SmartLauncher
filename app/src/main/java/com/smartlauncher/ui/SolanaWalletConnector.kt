package com.smartlauncher.ui

import android.content.ActivityNotFoundException
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class WalletConnectionResult(
    val authToken: String,
    val publicKey: String?
)

data class WalletOption(
    val packageName: String,
    val displayName: String
)

object SolanaWalletConnector {
    fun getInstalledWallets(activity: Activity): List<WalletOption> {
        val pm = activity.packageManager
        return SUPPORTED_WALLETS.mapNotNull { candidate ->
            val appInfo = try {
                pm.getApplicationInfo(candidate.packageName, 0)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }

            appInfo?.let {
                val label = pm.getApplicationLabel(it).toString().ifBlank { candidate.fallbackName }
                WalletOption(candidate.packageName, label)
            }
        }
    }

    suspend fun connect(activity: Activity, walletPackageName: String): WalletConnectionResult {
        return withContext(Dispatchers.IO) {
            if (!LocalAssociationIntentCreator.isWalletEndpointAvailable(activity.packageManager)) {
                throw ActivityNotFoundException("No Solana wallet endpoint found")
            }

            val scenario = LocalAssociationScenario(90_000)
            try {
                val clientFuture = scenario.start()
                val associationIntent = LocalAssociationIntentCreator.createAssociationIntent(
                    null,
                    scenario.port,
                    scenario.session
                ).setPackage(walletPackageName)

                withContext(Main) {
                    activity.startActivity(associationIntent)
                }

                val client = clientFuture.get(60, TimeUnit.SECONDS)
                val authResult = client.authorize(
                    IDENTITY_URI,
                    ICON_URI,
                    APP_NAME,
                    SOLANA_CHAIN,
                    null,
                    null,
                    null,
                    null
                ).get(60, TimeUnit.SECONDS)

                val primaryPublicKey = authResult.accounts
                    ?.firstOrNull()
                    ?.publicKey
                    ?.let(::encodePublicKey)
                    ?: authResult.publicKey?.let(::encodePublicKey)

                WalletConnectionResult(
                    authToken = authResult.authToken,
                    publicKey = primaryPublicKey
                )
            } finally {
                scenario.close()
            }
        }
    }

    private fun encodePublicKey(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private const val APP_NAME = "SmartLauncher"
    private const val SOLANA_CHAIN = "solana:mainnet"
    private const val SOLANA_MOBILE_PACKAGE = "com.solanamobile.wallet"
    private val IDENTITY_URI: Uri = Uri.parse("https://smartlauncher.app")
    private val ICON_URI: Uri = Uri.parse("/icon.png")

    private data class SupportedWallet(val packageName: String, val fallbackName: String)

    private val SUPPORTED_WALLETS = listOf(
        SupportedWallet("app.phantom", "Phantom"),
        SupportedWallet("app.backpack.mobile.standalone", "Backpack"),
        SupportedWallet("com.solflare.mobile", "Solflare"),
        SupportedWallet(SOLANA_MOBILE_PACKAGE, "Solana Mobile"),
        SupportedWallet("com.gemwallet.android", "Gem Wallet")
    )
}
