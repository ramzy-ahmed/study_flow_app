package com.studyflow.ui.decks_screen.decks_screen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.studyflow.R
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.databinding.ItemDeckBinding

class DeckAdapter(
    private val onDeleteClick: (DeckEntity) -> Unit,
    private val onEditClick: (DeckEntity) -> Unit,
    private val onItemClick: (DeckEntity) -> Unit
) : ListAdapter<DeckWithCards, DeckAdapter.ViewHolder>(Diff()) {
    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ItemDeckBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDeckBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeckWithCards) {
            val deck = item.deck
            val cards = item.cards
            
            binding.tvDeckTitle.text = deck.title

            // تحميل الأيقونة
            val iconRes = if (deck.image != null && deck.image != 0) deck.image else R.drawable.ill_language
            Glide.with(context)
                .load(iconRes)
                .into(binding.ivDeckIcon)

            // عرض عدد الكروت
            val cardCount = cards.size
            binding.tvCardCount.text = context.getString(R.string.cards_count_format, cardCount)

            // حساب التقدم (متوسط الـ masteryScore لجميع الكروت)
            val progress = if (cards.isNotEmpty()) {
                cards.map { it.masteryScore }.average().toInt()
            } else 0
            
            binding.deckProgress.progress = progress
            binding.tvDeckProgress.text = "$progress%"

            binding.btnMenu.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menuInflater.inflate(R.menu.context_menu, popup.menu)
                
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onEditClick(deck)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(deck)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            binding.root.setOnClickListener {
                onItemClick(deck)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<DeckWithCards>() {
        override fun areItemsTheSame(o: DeckWithCards, n: DeckWithCards) = o.deck.id == n.deck.id
        override fun areContentsTheSame(o: DeckWithCards, n: DeckWithCards) = o == n
    }
}
