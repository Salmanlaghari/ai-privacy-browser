package com.aibrowser.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.BookmarkRepository
import com.aibrowser.app.domain.Bookmark
import com.aibrowser.app.databinding.ActivityBookmarksBinding
import com.aibrowser.app.databinding.ItemBookmarkBinding

/**
 * Screen displaying user-saved [Bookmark] items.
 * Allows launching sites and deletion of items.
 */
class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var viewModel: BookmarksViewModel
    private lateinit var adapter: BookmarksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Manual DI
        val database = AppDatabase.getDatabase(this)
        val repository = BookmarkRepository(database.bookmarkDao())
        val factory = BookmarksViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[BookmarksViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.bookmarksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BookmarksAdapter(
            onBookmarkClick = { bookmark ->
                val intent = Intent(this, BrowserActivity::class.java).apply {
                    putExtra(BrowserActivity.EXTRA_URL, bookmark.url)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            },
            onDeleteClick = { bookmark ->
                viewModel.deleteBookmark(bookmark)
            }
        )
        binding.bookmarksRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.bookmarks.observe(this) { list ->
            adapter.setBookmarks(list)
            if (list.isNullOrEmpty()) {
                binding.emptyBookmarksTextView.visibility = View.VISIBLE
                binding.bookmarksRecyclerView.visibility = View.GONE
            } else {
                binding.emptyBookmarksTextView.visibility = View.GONE
                binding.bookmarksRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Inner RecyclerView Adapter for binding [Bookmark] objects.
     */
    private class BookmarksAdapter(
        private val onBookmarkClick: (Bookmark) -> Unit,
        private val onDeleteClick: (Bookmark) -> Unit
    ) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

        private val list = mutableListOf<Bookmark>()

        fun setBookmarks(newList: List<Bookmark>) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
            val itemBinding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BookmarkViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        inner class BookmarkViewHolder(private val itemBinding: ItemBookmarkBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(bookmark: Bookmark) {
                itemBinding.bookmarkTitleText.text = bookmark.title
                itemBinding.bookmarkUrlText.text = bookmark.url

                itemBinding.root.setOnClickListener {
                    onBookmarkClick(bookmark)
                }

                itemBinding.btnDeleteBookmark.setOnClickListener {
                    onDeleteClick(bookmark)
                }
            }
        }
    }
}
