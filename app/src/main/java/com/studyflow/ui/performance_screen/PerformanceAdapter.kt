package com.studyflow.ui.performance_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflow.databinding.ItemDeckPerformanceBinding
import java.util.Locale
class PerformanceAdapter : ListAdapter<PerformanceStat, PerformanceAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemDeckPerformanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stat: PerformanceStat) {
            binding.tvDeckName.text = stat.title
            binding.tvPercent.text = String.format(Locale.getDefault(), "%d%%", stat.masteryPercentage)
            binding.progressBar.progress = stat.masteryPercentage
            binding.tvSubStat.text = String.format(
                Locale.getDefault(), 
                "%d cards mastered / %d total", 
                stat.masteredCards, 
                stat.totalCards
            )
            // Note: colorTag background could be set if color is provided
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeckPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PerformanceStat>() {
        override fun areItemsTheSame(oldItem: PerformanceStat, newItem: PerformanceStat): Boolean =
            oldItem.deckId == newItem.deckId
        override fun areContentsTheSame(oldItem: PerformanceStat, newItem: PerformanceStat): Boolean =
            oldItem == newItem
    }
}
