package com.aibrowser.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.domain.HistoryEntry
import com.aibrowser.app.R
import com.aibrowser.app.databinding.ActivityHistoryBinding
import com.aibrowser.app.databinding.ItemHistoryBinding
import java.text.DateFormat

/**
 * Screen displaying the user's browsing history entries.
 * Allows searching, opening history records, and clearing all history.
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Manual DI
        val database = AppDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())
        val factory = HistoryViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(
            onEntryClick = { entry ->
                val intent = Intent(this, BrowserActivity::class.java).apply {
                    putExtra(BrowserActivity.EXTRA_URL, entry.url)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            },
            onDeleteClick = { entry ->
                viewModel.deleteHistoryEntry(entry)
            }
        )
        binding.historyRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnClearAllHistory.setOnClickListener {
            showClearConfirmDialog()
        }
    }

    private fun showClearConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_clear_confirm_title))
            .setMessage(getString(R.string.dialog_clear_confirm_msg))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                viewModel.clearAllHistory()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setupObservers() {
        viewModel.historyEntries.observe(this) { list ->
            adapter.setEntries(list)
            if (list.isNullOrEmpty()) {
                binding.emptyHistoryTextView.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
            } else {
                binding.emptyHistoryTextView.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Inner RecyclerView Adapter for binding [HistoryEntry] objects.
     */
    private class HistoryAdapter(
        private val onEntryClick: (HistoryEntry) -> Unit,
        private val onDeleteClick: (HistoryEntry) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        private val list = mutableListOf<HistoryEntry>()

        fun setEntries(newList: List<HistoryEntry>) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val itemBinding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HistoryViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        inner class HistoryViewHolder(private val itemBinding: ItemHistoryBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(entry: HistoryEntry) {
                itemBinding.historyTitleText.text = entry.title
                itemBinding.historyUrlText.text = entry.url

                val countLabel = if (entry.visitCount > 1) "${entry.visitCount} visits" else "1 visit"
                val dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(entry.visitedAt)
                itemBinding.historyVisitCountText.text = "$countLabel - $dateString"

                itemBinding.root.setOnClickListener {
                    onEntryClick(entry)
                }

                itemBinding.btnDeleteHistoryEntry.setOnClickListener {
                    onDeleteClick(entry)
                }
            }
        }
    }
}
