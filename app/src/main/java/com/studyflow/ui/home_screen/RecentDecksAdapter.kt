package com.studyflow.ui.home_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.databinding.ItemRecentDeckBinding

class RecentDecksAdapter(private val onDeckClick: (Int) -> Unit) :
    ListAdapter<DeckWithCards, RecentDecksAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecentDeckBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecentDeckBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(deckWithCards: DeckWithCards) {
            binding.apply {
                tvDeckName.text = deckWithCards.deck.title
                tvLastReview.text = deckWithCards.deck.category
                tvCardCount.text = "${deckWithCards.cards.size} Cards"
                
                deckWithCards.deck.image?.let {
                    // logic for setting image based on resource ID
                }

                root.setOnClickListener {
                    onDeckClick(deckWithCards.deck.id)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DeckWithCards>() {
        override fun areItemsTheSame(oldItem: DeckWithCards, newItem: DeckWithCards): Boolean {
            return oldItem.deck.id == newItem.deck.id
        }

        override fun areContentsTheSame(oldItem: DeckWithCards, newItem: DeckWithCards): Boolean {
            return oldItem == newItem
        }
    }
}
