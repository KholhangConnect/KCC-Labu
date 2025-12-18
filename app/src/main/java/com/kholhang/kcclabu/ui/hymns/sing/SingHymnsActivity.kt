package com.kholhang.kcclabu.ui.hymns.sing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.UiPref
import com.kholhang.kcclabu.databinding.ActivitySingBinding
import com.kholhang.kcclabu.extensions.activity.applyMaterialTransform
import com.kholhang.kcclabu.extensions.arch.observeNonNull
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import com.kholhang.kcclabu.ui.collections.add.AddToCollectionFragment
import com.kholhang.kcclabu.ui.hymns.hymnals.HymnalListBottomSheetFragment
import com.kholhang.kcclabu.ui.hymns.sing.edit.EditHymnActivity
import com.kholhang.kcclabu.ui.hymns.sing.player.PlaybackState
import com.kholhang.kcclabu.ui.hymns.sing.player.SimpleTunePlayer
import com.kholhang.kcclabu.ui.hymns.sing.present.PresentHymnActivity
import com.kholhang.kcclabu.ui.hymns.sing.style.TextStyleChanges
import com.kholhang.kcclabu.ui.hymns.sing.style.TextStyleFragment
import com.kholhang.kcclabu.utils.Helper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SingHymnsActivity : AppCompatActivity(), TextStyleChanges {

    @Inject
    lateinit var prefs: HymnalPrefs

    @Inject
    lateinit var tunePlayer: SimpleTunePlayer

    private val viewModel: SingHymnsViewModel by viewModels()

    private var pagerAdapter: SingFragmentsAdapter? = null
    private lateinit var binding: ActivitySingBinding

    private var currentPosition: Int? = null

    private val editHymnLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPosition = binding.viewPager.currentItem
        val collectionId = intent.getIntExtra(ARG_COLLECTION_ID, -1)
        viewModel.loadData(collectionId)
    }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initUi() {
        lifecycle.addObserver(tunePlayer)

        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tunePlayer.stopMedia()
                }
            })

            numberPadView.setOnNumSelectedCallback { number ->
                hideNumPad()
                val position = pagerAdapter?.hymns?.indexOfFirst { it.number == number }
                if (position == null || position < 0) {
                    snackbar.show(messageText = getString(R.string.error_invalid_number, number))
                    return@setOnNumSelectedCallback
                }
                viewPager.setCurrentItem(position, false)
            }

            fabNumber.setOnClickListener {
                numberPadView.onShown()
                fabNumber.isExpanded = true
                scrimOverLay.isVisible = true
            }
            scrimOverLay.setOnTouchListener { _, _ ->
                if (fabNumber.isExpanded) {
                    hideNumPad()
                }
                true
            }

            bottomAppBar.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.action_text_format -> {
                        val fragment = TextStyleFragment.newInstance(
                            prefs.getTextStyleModel(),
                            this@SingHymnsActivity
                        )
                        fragment.show(supportFragmentManager, fragment.tag)
                        true
                    }
                    R.id.action_fullscreen -> {
                        Timber.d("Fullscreen button clicked")
                        if (pagerAdapter?.hymns?.isNotEmpty() == true) {
                            val currentItem = viewPager.currentItem
                            val hymn = pagerAdapter?.hymns?.get(currentItem)
                            if (hymn != null) {
                                Timber.d("Launching fullscreen for hymn: ${hymn.number}")
                                try {
                                    val intent = PresentHymnActivity.launchIntent(this@SingHymnsActivity, hymn)
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error launching fullscreen activity")
                                }
                            } else {
                                Timber.w("Fullscreen: Hymn is null at position $currentItem")
                            }
                        } else {
                            Timber.w("Fullscreen: No hymns available or pagerAdapter is null")
                        }
                        true
                    }
                    R.id.action_add_to_list -> {
                        val hymnId = pagerAdapter?.hymns?.get(
                            viewPager.currentItem
                        )?.hymnId
                            ?: return@setOnMenuItemClickListener false

                        val fragment = AddToCollectionFragment.newInstance(hymnId)
                        fragment.show(supportFragmentManager, fragment.tag)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun hideNumPad() {
        binding.apply {
            scrimOverLay.isVisible = false
            fabNumber.isExpanded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyMaterialTransform(getString(R.string.transition_shared_element))
        super.onCreate(savedInstanceState)
        binding = ActivitySingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
        if (binding.fabNumber.isExpanded) {
            hideNumPad()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        initUi()

        val number = intent.getIntExtra(ARG_SELECTED, 1)

        viewModel.statusLiveData.observeNonNull(this) {
        }
        viewModel.hymnalTitleLiveData.observeNonNull(this) {
            title = it
        }
        viewModel.hymnListLiveData.observeNonNull(this) {
            pagerAdapter = SingFragmentsAdapter(this, it)
            binding.viewPager.apply {
                adapter = pagerAdapter
                val position = currentPosition ?: number.minus(1)
                setCurrentItem(position, false)
                currentPosition = null
            }
        }
        tunePlayer.playbackLiveData.observeNonNull(this) {
            invalidateOptionsMenu()
        }

        val collectionId = intent.getIntExtra(ARG_COLLECTION_ID, -1)
        viewModel.loadData(collectionId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hymn_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.action_tune -> {
                pagerAdapter?.hymns?.get(
                    binding.viewPager.currentItem
                )?.number?.let {
                    tunePlayer.togglePlayTune(it)
                }
                true
            }
            R.id.action_edit -> {
                if (pagerAdapter?.hymns?.isNotEmpty() == true) {
                    val hymn = pagerAdapter?.hymns
                        ?.get(binding.viewPager.currentItem)
                        ?: return false

                    val intent = EditHymnActivity.editIntent(this, hymn)
                    editHymnLauncher.launch(intent)
                }
                true
            }
            R.id.actions_hymnals -> {
                val fragment = HymnalListBottomSheetFragment
                    .newInstance {
                        currentPosition = binding.viewPager.currentItem
                        viewModel.switchHymnal(it)
                    }
                fragment.show(supportFragmentManager, fragment.tag)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val mediaItem = menu?.findItem(R.id.action_tune) ?: return super.onPrepareOptionsMenu(menu)
        when (tunePlayer.playbackLiveData.value) {
            PlaybackState.ON_PLAY -> {
                mediaItem.apply {
                    isVisible = true
                    icon = ContextCompat.getDrawable(
                        this@SingHymnsActivity,
                        R.drawable.ic_stop
                    )
                }
            }
            PlaybackState.ON_COMPLETE,
            PlaybackState.ON_STOP,
            null -> {
                val number = pagerAdapter?.hymns?.get(
                    binding.viewPager.currentItem
                )?.number
                val canPlay = number?.let { tunePlayer.canPlayTune(it) } ?: false
                mediaItem.apply {
                    isVisible = canPlay
                    icon = ContextCompat.getDrawable(
                        this@SingHymnsActivity, R.drawable.ic_play_circle
                    )
                }
            }
        }
        menu.findItem(R.id.actions_hymnals).isVisible = !intent.hasExtra(ARG_COLLECTION_ID)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun updateTheme(pref: UiPref) {
        prefs.setUiPref(pref)
        Helper.switchToTheme(pref)
    }

    override fun updateTypeFace(fontRes: Int) {
        prefs.setFontRes(fontRes)
        updateHymnUi()
    }

    override fun updateTextSize(size: Float) {
        prefs.setFontSize(size)
        updateHymnUi()
    }

    private fun updateHymnUi() {
        lifecycleScope.launch {
            binding.viewPager.apply {
                val pagePosition = currentItem
                adapter?.apply {
                    notifyItemChanged(pagePosition - 1)
                    notifyItemChanged(pagePosition)
                    notifyItemChanged(pagePosition + 1)

                    setCurrentItem(pagePosition + 2, false)
                    delay(200)
                    setCurrentItem(pagePosition, false)
                }
            }
        }
    }

    companion object {
        private const val ARG_SELECTED = "arg:selected_number"
        private const val ARG_COLLECTION_ID = "arg:collection_id"

        fun singIntent(context: Context, number: Int): Intent =
            Intent(context, SingHymnsActivity::class.java).apply {
                putExtra(ARG_SELECTED, number)
            }

        fun singCollectionIntent(context: Context, id: Int): Intent =
            Intent(context, SingHymnsActivity::class.java).apply {
                putExtra(ARG_COLLECTION_ID, id)
            }
    }
}
