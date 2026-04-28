package com.studyflow.ui.decks_screen.add_deck_screen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.studyflow.databinding.ItemImageBinding

class ImageAdapter(
    var iconRes: List<Int>,
    val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.IconsViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IconsViewHolder {
        context = parent.context
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: IconsViewHolder,
        position: Int
    ) {
        val icon = iconRes[position]

        holder.itemView.setOnClickListener {
            onItemClick(icon)
        }

        Glide.with(holder.itemView.context)
            .load(icon)
            .into(holder.binding.viewImage)
    }

    override fun getItemCount(): Int {
        return iconRes.size
    }

    class IconsViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {}
}