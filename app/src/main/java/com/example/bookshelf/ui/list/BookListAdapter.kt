package com.example.bookshelf.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.R
import com.example.bookshelf.data.Book
import com.google.android.material.chip.Chip

class BookListAdapter(
    private val onClick: (Book) -> Unit,
    private val onToggleRead: (Book, Boolean) -> Unit
) : ListAdapter<Book, BookListAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return VH(v, onClick, onToggleRead)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        itemView: View,
        val onClick: (Book) -> Unit,
        val onToggleRead: (Book, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        // inside VH:
        private val title: TextView = itemView.findViewById(R.id.title)
        private val author: TextView = itemView.findViewById(R.id.author)
        private val rating: RatingBar = itemView.findViewById(R.id.rating)
        private val chip: Chip = itemView.findViewById(R.id.chipRead)
        private val tvGenre: TextView = itemView.findViewById(R.id.tvGenre) // NEW

        private fun decorateGenre(value: String): String? {
            if (value.isBlank()) return null
            val emoji = mapOf(
                "General" to "📖", "Fantasy" to "🐉", "Sci-Fi" to "🚀", "Mystery" to "🕵️",
                "Romance" to "💘", "Horror" to "👻", "Non-fiction" to "🧠", "Computing" to "💻",
                "History" to "📜", "Classics" to "🎭", "Science" to "🧪", "Other" to "📚"
            )[value] ?: "📚"
            return "$emoji  $value"
        }

        fun bind(b: Book) {
            title.text = b.title
            author.text = b.author
            rating.rating = b.rating.toFloat()

            // Genre line
            val g = decorateGenre(b.genre)
            if (g == null) {
                tvGenre.visibility = View.GONE
            } else {
                tvGenre.visibility = View.VISIBLE
                tvGenre.text = g
            }

            chip.isChecked = b.read
            chip.text = if (b.read) itemView.context.getString(R.string.read)
            else itemView.context.getString(R.string.unread)

            itemView.setOnClickListener { onClick(b) }
            chip.setOnClickListener {
                val newState = chip.isChecked
                chip.text = if (newState) itemView.context.getString(R.string.read)
                else itemView.context.getString(R.string.unread)
                onToggleRead(b, newState)
            }
        }
    }

        companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(old: Book, new: Book) = old.id == new.id
            override fun areContentsTheSame(old: Book, new: Book) = old == new
        }
    }
}
