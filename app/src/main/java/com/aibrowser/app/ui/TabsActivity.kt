package com.aibrowser.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aibrowser.app.data.Tab
import com.aibrowser.app.data.TabManager
import com.aibrowser.app.databinding.ActivityTabsBinding
import com.aibrowser.app.databinding.ItemTabCardBinding

/**
 * Tab switcher activity. Displays all active tabs in a grid.
 * Allows users to switch active tabs, close individual tabs, or spin up new tabs.
 */
class TabsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabsBinding
    private lateinit var adapter: TabsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupHeaderActions()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        binding.tabsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = TabsAdapter(
            onTabClick = { tab ->
                TabManager.switchTab(tab.id)
                finish()
            },
            onTabClose = { tab ->
                TabManager.closeTab(tab.id, this)
                adapter.setTabs(TabManager.getAllTabs())
                updateEmptyState()
            }
        )
        binding.tabsRecyclerView.adapter = adapter
        adapter.setTabs(TabManager.getAllTabs())
    }

    private fun setupHeaderActions() {
        binding.btnCloseTabs.setOnClickListener {
            finish()
        }

        binding.btnNewTab.setOnClickListener {
            TabManager.createTab("New Tab", "about:blank")
            TabManager.persist(this)
            finish()
        }
    }

    private fun updateEmptyState() {
        val allTabs = TabManager.getAllTabs()
        if (allTabs.isEmpty()) {
            binding.emptyTabsTextView.visibility = View.VISIBLE
            binding.tabsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTabsTextView.visibility = View.GONE
            binding.tabsRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Inner RecyclerView Adapter for binding [Tab] models to tab grid cards.
     */
    private class TabsAdapter(
        private val onTabClick: (Tab) -> Unit,
        private val onTabClose: (Tab) -> Unit
    ) : RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {

        private val tabs = mutableListOf<Tab>()

        fun setTabs(newTabs: List<Tab>) {
            tabs.clear()
            tabs.addAll(newTabs)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val itemBinding = ItemTabCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TabViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            holder.bind(tabs[position])
        }

        override fun getItemCount(): Int = tabs.size

        inner class TabViewHolder(private val itemBinding: ItemTabCardBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(tab: Tab) {
                itemBinding.tabTitleText.text = tab.title
                itemBinding.tabUrlText.text = tab.url

                itemBinding.root.setOnClickListener {
                    onTabClick(tab)
                }

                itemBinding.btnCloseTab.setOnClickListener {
                    onTabClose(tab)
                }
            }
        }
    }
}
