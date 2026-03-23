package com.smartlauncher.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.smartlauncher.R
import com.smartlauncher.data.WalletSessionManager
import com.smartlauncher.databinding.ActivityWalletConnectBinding
import kotlinx.coroutines.launch

class WalletConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletConnectBinding
    private lateinit var walletSessionManager: WalletSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walletSessionManager = WalletSessionManager(this)

        if (walletSessionManager.isConnected()) {
            openLauncher()
            return
        }

        binding.connectWalletButton.setOnClickListener {
            showWalletSelector()
        }
    }

    private fun showWalletSelector() {
        val wallets = SolanaWalletConnector.getInstalledWallets(this)
        if (wallets.isEmpty()) {
            Toast.makeText(this, getString(R.string.wallet_not_found), Toast.LENGTH_LONG).show()
            return
        }

        val labels = wallets.map { it.displayName }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_wallet_title))
            .setItems(labels) { _, which ->
                connectWallet(wallets[which].packageName, wallets[which].displayName)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun connectWallet(walletPackageName: String, walletName: String) {
        binding.connectWalletButton.isEnabled = false
        binding.walletStatus.text = getString(R.string.wallet_connecting_named, walletName)

        lifecycleScope.launch {
            try {
                val result = SolanaWalletConnector.connect(this@WalletConnectActivity, walletPackageName)
                walletSessionManager.save(result.authToken, result.publicKey)
                binding.walletStatus.text = getString(R.string.wallet_connected, result.publicKey)
                openLauncher()
            } catch (e: ActivityNotFoundException) {
                binding.connectWalletButton.isEnabled = true
                binding.walletStatus.text = getString(R.string.wallet_required_message)
                Toast.makeText(
                    this@WalletConnectActivity,
                    getString(R.string.wallet_not_found),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                binding.connectWalletButton.isEnabled = true
                binding.walletStatus.text = getString(R.string.wallet_required_message)
                Toast.makeText(
                    this@WalletConnectActivity,
                    getString(R.string.wallet_connect_failed, e.message ?: "Unknown error"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openLauncher() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }
}
