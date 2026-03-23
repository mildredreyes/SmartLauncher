package com.smartlauncher.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.smartlauncher.R
import com.smartlauncher.data.AppCategory
import com.smartlauncher.data.WalletSessionManager
import com.smartlauncher.databinding.ActivityLauncherBinding
import com.smartlauncher.viewmodel.LauncherViewModel

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val viewModel: LauncherViewModel by viewModels()
    private val adapter = AppGridAdapter()
    private lateinit var walletSessionManager: WalletSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        walletSessionManager = WalletSessionManager(this)
        if (!walletSessionManager.isConnected()) {
            startActivity(Intent(this, WalletConnectActivity::class.java))
            finish()
            return
        }

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUsagePermission()
        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApps()
    }

    // ── Permission check ──────────────────────────────────────────────────

    private fun checkUsagePermission() {
        if (!hasUsageStatsPermission()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_body))
                .setPositiveButton(getString(R.string.btn_grant)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNegativeButton(getString(R.string.btn_skip)) { _, _ -> }
                .show()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.appsGrid.layoutManager = GridLayoutManager(this, 4)
        binding.appsGrid.adapter = adapter
        binding.appsGrid.setHasFixedSize(true)
    }

    // ── Category chip filter bar ──────────────────────────────────────────

    private fun setupCategoryChips() {
        // "All" chip
        val allChip = createChip(getString(R.string.filter_all), null)
        allChip.isChecked = true
        binding.categoryChipGroup.addView(allChip)

        // One chip per category, including Others for uncategorized apps
        AppCategory.entries
            .forEach { category ->
                binding.categoryChipGroup.addView(createChip(category.chipLabel, category))
            }
    }

    private fun createChip(label: String, category: AppCategory?): Chip {
        return Chip(this).apply {
            text = label
            isCheckable = true
            isClickable = true
            setOnClickListener {
                viewModel.setFilter(if (isChecked) category else null)
            }
        }
    }

    // ── Search bar ────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearch(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ── Observe ───────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
            binding.appsGrid.visibility = if (loading) View.GONE else View.VISIBLE
        }

        viewModel.filteredApps.observe(this) { apps ->
            adapter.submitList(apps)
            binding.appCountLabel.text = getString(R.string.launcher_count, apps.size)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = it
            }
        }
    }
}
