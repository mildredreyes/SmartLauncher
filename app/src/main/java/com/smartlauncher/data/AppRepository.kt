package com.smartlauncher.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.ln
import kotlin.math.max

class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    suspend fun loadRankedApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val usageMap = fetchUsageStats()
        val maxUsage = usageMap.values.maxOrNull()?.toFloat() ?: 1f

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackage = context.packageName

        val items = activities
            .filter { it.activityInfo.packageName != selfPackage }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val systemCategory = resolveInfo.activityInfo.applicationInfo.category
                val category = CategoryClassifier.classify(pkg, label, systemCategory)

                // Usage score: log-normalised so the top app doesn't dominate
                val rawUsage = usageMap[pkg]?.toFloat() ?: 0f
                val usageScore = if (maxUsage > 0) {
                    (ln(rawUsage + 1f) / ln(maxUsage + 1f)).coerceIn(0f, 1f)
                } else 0f

                // Blended rank: 70% usage + 30% category priority
                val rankScore = (usageScore * 0.70f) + (category.basePriority * 0.30f)

                AppItem(
                    packageName = pkg,
                    label = label,
                    icon = icon,
                    category = category,
                    usageScore = usageScore,
                    rankScore = rankScore
                )
            }
            .sortedByDescending { it.rankScore }

        items
    }

    /**
     * Returns a map of packageName → total foreground time (ms) over the last 30 days.
     * Requires PACKAGE_USAGE_STATS permission (granted via Settings > Special app access).
     */
    private fun fetchUsageStats(): Map<String, Long> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 30L * 24 * 60 * 60 * 1000

        val stats: List<UsageStats> = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_MONTHLY, start, end
        ) ?: return emptyMap()

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .associate { it.packageName to it.totalTimeInForeground }
    }
}
