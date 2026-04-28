package com.studyflow.ui.decks_screen.deck_details_screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyflow.R
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.databinding.ItemPreviewCardBinding

class CardAdapter(
    private val onEditClick: (CardEntity) -> Unit,
    private val onDeleteClick: (CardEntity) -> Unit
) : ListAdapter<CardEntity, CardAdapter.CardViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemPreviewCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(
        private val binding: ItemPreviewCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardEntity) {
            binding.tvCardQuestion.text = card.question
            binding.tvCardAnswer.text = card.answer
            
            val colorRes = when (card.difficultyLevel) {
                "Easy" -> R.color.green
                "Hard" -> R.color.red
                else -> R.color.orange
            }
            val color = ContextCompat.getColor(binding.root.context, colorRes)

            binding.viewAccent.setBackgroundColor(color)
            binding.btnMenu.setOnClickListener { view ->
                showPopupMenu(view, card)
            }
        }

        private fun showPopupMenu(view: View, card: CardEntity) {
            val popupContext = ContextThemeWrapper(view.context, R.style.CustomPopupMenu)
            val popupMenu = PopupMenu(popupContext, binding.btnMenu)
            popupMenu.inflate(R.menu.context_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onEditClick(card)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(card)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    class Diff : DiffUtil.ItemCallback<CardEntity>() {
        override fun areItemsTheSame(o: CardEntity, n: CardEntity) = o.id == n.id
        override fun areContentsTheSame(o: CardEntity, n: CardEntity) = o == n
    }
}
