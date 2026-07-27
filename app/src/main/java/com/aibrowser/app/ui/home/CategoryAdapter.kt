package com.aibrowser.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aibrowser.app.databinding.ItemBookmarkBinding

/**
 * Data class representing a curated speed-dial site option.
 */
data class CuratedSite(val title: String, val url: String)

/**
 * Adapter for presenting the curated Category-based sites.
 */
class CategoryAdapter(
    private val onSiteClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val sites = mutableListOf<CuratedSite>()

    fun setSites(newSites: List<CuratedSite>) {
        sites.clear()
        sites.addAll(newSites)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(sites[position])
    }

    override fun getItemCount(): Int = sites.size

    inner class CategoryViewHolder(private val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(site: CuratedSite) {
            binding.bookmarkTitleText.text = site.title
            binding.bookmarkUrlText.text = site.url
            binding.btnDeleteBookmark.visibility = android.view.View.GONE

            binding.root.setOnClickListener {
                onSiteClick(site.url)
            }
        }
    }
}
