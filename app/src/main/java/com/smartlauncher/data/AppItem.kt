package com.smartlauncher.data

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val category: AppCategory,
    val usageScore: Float,   // 0..1 normalised from UsageStats
    val rankScore: Float     // final blended score used for ordering
)
