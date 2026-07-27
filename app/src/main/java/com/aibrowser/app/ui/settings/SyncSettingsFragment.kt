package com.aibrowser.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aibrowser.app.data.sync.FirebaseSyncManager
import com.aibrowser.app.databinding.FragmentSettingsSyncBinding
import kotlinx.coroutines.launch
import java.text.DateFormat

/**
 * Fragment containing Cloud Sync settings.
 * Authenticates users anonymously/via Google and trigger cloud sync operations.
 */
class SyncSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsSyncBinding? = null
    private val binding get() = _binding!!
    private lateinit var syncManager: FirebaseSyncManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsSyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        syncManager = FirebaseSyncManager(requireContext())

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        if (syncManager.isUserSignedIn()) {
            binding.syncStatusTextView.text = "Account: Logged In Successfully"
            binding.btnSignInGoogle.text = "Sign Out"
        } else {
            binding.syncStatusTextView.text = "Account: Not Signed In"
            binding.btnSignInGoogle.text = "Sign In Anonymously / Google"
        }

        val lastSync = syncManager.getLastSyncTimestamp()
        if (lastSync > 0L) {
            val dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastSync)
            binding.lastSyncTextView.text = "Last Synced: $dateStr"
        } else {
            binding.lastSyncTextView.text = "Last Synced: Never"
        }
    }

    private fun setupListeners() {
        binding.btnSignInGoogle.setOnClickListener {
            if (syncManager.isUserSignedIn()) {
                syncManager.signOut()
                Toast.makeText(context, "Signed Out Successfully", Toast.LENGTH_SHORT).show()
                setupUI()
            } else {
                binding.syncStatusTextView.text = "Signing in..."
                syncManager.signInAnonymously { success ->
                    if (success) {
                        Toast.makeText(context, "Anonymous login successful for cloud backup!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                    setupUI()
                }
            }
        }

        binding.btnSyncNow.setOnClickListener {
            if (!syncManager.isUserSignedIn()) {
                Toast.makeText(context, "Please sign in to sync cloud data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.lastSyncTextView.text = "Syncing in background..."
            lifecycleScope.launch {
                syncManager.syncAllData()
                setupUI()
                Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
