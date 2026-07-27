package com.aibrowser.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aibrowser.app.databinding.ItemBookmarkBinding // We can reuse item_bookmark or a simple text/icon card layout!
import com.aibrowser.app.domain.HistoryEntry

/**
 * Adapter for binding the top 8 most-visited personalized shortcuts.
 */
class ShortcutAdapter(
    private val onShortcutClick: (String) -> Unit,
    private val onShortcutLongClick: (HistoryEntry) -> Unit
) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {

    private val list = mutableListOf<HistoryEntry>()

    fun setShortcuts(newShortcuts: List<HistoryEntry>) {
        list.clear()
        // Take top 8 as per specification
        list.addAll(newShortcuts.take(8))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShortcutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ShortcutViewHolder(private val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HistoryEntry) {
            // Unify layouts beautifully
            binding.bookmarkTitleText.text = entry.title
            binding.bookmarkUrlText.text = entry.url

            // Re-label action button for clarity
            binding.btnDeleteBookmark.visibility = android.view.View.GONE

            binding.root.setOnClickListener {
                onShortcutClick(entry.url)
            }

            binding.root.setOnLongClickListener {
                onShortcutLongClick(entry)
                true
            }
        }
    }
}
