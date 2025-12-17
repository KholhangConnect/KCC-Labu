package com.kholhang.kcclabu.ui.hymns.sing.present

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.databinding.ActivityPresentHymnBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PresentHymnActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresentHymnBinding

    private val pagerAdapter: PresentPagerAdapter by lazy { PresentPagerAdapter(this) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get hymn from intent first, before any UI operations
        val hymn = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(ARG_HYMN, Hymn::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Hymn>(ARG_HYMN)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting hymn from intent")
            null
        }
        
        if (hymn == null) {
            Timber.e("PresentHymnActivity: Hymn is null, finishing activity")
            finish()
            return
        }
        
        try {
            binding = ActivityPresentHymnBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up exit button
            binding.btnExit.setOnClickListener { 
                finish() 
            }
            
            // Set up ViewPager and adapter
            binding.viewPager.adapter = pagerAdapter
            
            // Load hymn data
            Timber.d("PresentHymnActivity: Loading hymn ${hymn.number} - ${hymn.title}")
            pagerAdapter.presentHymn(hymn)
            
            // Simple fullscreen - just hide action bar
            try {
                supportActionBar?.hide()
            } catch (e: Exception) {
                Timber.e(e, "Error hiding action bar")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error in PresentHymnActivity.onCreate")
            finish()
        }
    }
    

    companion object {
        private const val ARG_HYMN = "arg:hymn"

        fun launchIntent(context: Context, hymn: Hymn): Intent =
            Intent(context, PresentHymnActivity::class.java).apply {
                putExtra(ARG_HYMN, hymn)
            }
    }
}
