package com.smartlauncher.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartlauncher.R
import com.smartlauncher.data.AppItem
import com.smartlauncher.databinding.ItemAppBinding

class AppGridAdapter : ListAdapter<AppItem, AppGridAdapter.AppViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppItem) {
            binding.appIcon.setImageDrawable(app.icon)
            binding.appLabel.text = app.label
            binding.categoryBadge.text = app.category.badgeLabel
            binding.categoryBadge.setBackgroundResource(categoryBackground(app.category.colorRes))

            binding.root.setOnClickListener {
                val pm = it.context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    it.context.startActivity(launchIntent)
                } else {
                    Toast.makeText(
                        it.context,
                        it.context.getString(R.string.cannot_launch_app, app.label),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.root.setOnLongClickListener {
                val intent = Intent(Intent.ACTION_SHOW_APP_INFO).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, app.packageName)
                }
                it.context.startActivity(intent)
                true
            }
        }

        private fun categoryBackground(colorIndex: Int) = when (colorIndex) {
            0 -> com.smartlauncher.R.drawable.badge_social
            1 -> com.smartlauncher.R.drawable.badge_work
            2 -> com.smartlauncher.R.drawable.badge_entertainment
            3 -> com.smartlauncher.R.drawable.badge_games
            4 -> com.smartlauncher.R.drawable.badge_shopping
            5 -> com.smartlauncher.R.drawable.badge_utilities
            else -> com.smartlauncher.R.drawable.badge_other
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppItem>() {
            override fun areItemsTheSame(old: AppItem, new: AppItem) =
                old.packageName == new.packageName
            override fun areContentsTheSame(old: AppItem, new: AppItem) =
                old.rankScore == new.rankScore && old.category == new.category
        }
    }
}
