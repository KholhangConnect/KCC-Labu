package com.kholhang.kcclabu.ui.hymns.sing.present

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.kholhang.kcclabu.databinding.FragmentHymnVerseBinding
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HymnVerseFragment : Fragment() {

    @Inject
    lateinit var prefs: HymnalPrefs

    private var binding: FragmentHymnVerseBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHymnVerseBinding.inflate(inflater, container, false).also {
            binding = it
            binding?.apply {
                // Apply font settings
                verseText.apply {
                    try {
                        textSize = prefs.getFontSize()
                        typeface = ResourcesCompat.getFont(context, prefs.getFontRes())
                    } catch (ex: Exception) {
                        Timber.e(ex)
                    }
                }
                
                // Apply background color to the ZoomableNestedScrollView
                val backgroundColor = prefs.getBackgroundColor()
                root.setBackgroundColor(backgroundColor)
                
                // Calculate and apply appropriate text color based on background
                val textColor = calculateTextColor(backgroundColor)
                verseText.setTextColor(textColor)
                
                // Apply line spacing
                val lineSpacing = prefs.getLineSpacing()
                verseText.setLineSpacing(0f, lineSpacing)
                
                // Apply text indent (add to existing horizontal padding)
                val textIndent = prefs.getTextIndent().toInt()
                val originalPadding = resources.getDimensionPixelSize(com.kholhang.kcclabu.R.dimen.spacing_medium)
                verseText.setPadding(
                    originalPadding + textIndent,
                    verseText.paddingTop,
                    originalPadding,
                    verseText.paddingBottom
                )
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val verse = arguments?.getString(ARG_VERSE)
        binding?.verseText?.text = verse
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh settings when fragment resumes (in case user changed settings)
        binding?.apply {
            // Reapply background color
            val backgroundColor = prefs.getBackgroundColor()
            root.setBackgroundColor(backgroundColor)
            
            // Calculate and apply appropriate text color based on background
            val textColor = calculateTextColor(backgroundColor)
            verseText.setTextColor(textColor)
            
            // Reapply font settings
            verseText.apply {
                try {
                    textSize = prefs.getFontSize()
                    typeface = ResourcesCompat.getFont(context, prefs.getFontRes())
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
            
            // Reapply line spacing
            val lineSpacing = prefs.getLineSpacing()
            verseText.setLineSpacing(0f, lineSpacing)
        }
    }
    
    /**
     * Calculate appropriate text color based on background color brightness
     * Returns black for light backgrounds, white for dark backgrounds
     */
    private fun calculateTextColor(backgroundColor: Int): Int {
        // Extract RGB components
        val r = (backgroundColor shr 16) and 0xFF
        val g = (backgroundColor shr 8) and 0xFF
        val b = backgroundColor and 0xFF
        
        // Calculate relative luminance (perceived brightness)
        // Using the formula: 0.299*R + 0.587*G + 0.114*B
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        
        // If background is light (luminance > 0.5), use dark text, otherwise use light text
        return if (luminance > 0.5) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.WHITE
        }
    }

    companion object {
        private const val ARG_VERSE = "arg:verse"

        fun newInstance(verse: String): HymnVerseFragment = HymnVerseFragment().apply {
            arguments = bundleOf(ARG_VERSE to verse)
        }
    }
}
