package com.kholhang.kcclabu.ui.hymns.sing.hymn

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.databinding.FragmentHymnBinding
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HymnFragment : Fragment() {

    @Inject
    lateinit var prefs: HymnalPrefs

    private var binding: FragmentHymnBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHymnBinding.inflate(inflater, container, false).also {
            binding = it
            binding?.apply {
                // Apply font settings
                hymnText.apply {
                    try {
                        textSize = prefs.getFontSize()
                        typeface = ResourcesCompat.getFont(context, prefs.getFontRes())
                    } catch (ex: Exception) {
                        // Some devices failing to resolve resources files
                        Timber.e(ex)
                    }
                }
                
                // Apply background color to the ConstraintLayout inside ZoomableNestedScrollView
                val backgroundColor = prefs.getBackgroundColor()
                val constraintLayout = root.getChildAt(0) as? ViewGroup
                constraintLayout?.setBackgroundColor(backgroundColor)
                // Also set on root as fallback
                root.setBackgroundColor(backgroundColor)
                
                // Calculate and apply appropriate text color based on background
                val textColor = calculateTextColor(backgroundColor)
                hymnText.setTextColor(textColor)
                
                // Apply line spacing
                val lineSpacing = prefs.getLineSpacing()
                hymnText.setLineSpacing(0f, lineSpacing)
                
                // Apply text indent (add to existing horizontal padding)
                val textIndent = prefs.getTextIndent().toInt()
                val originalPadding = resources.getDimensionPixelSize(com.kholhang.kcclabu.R.dimen.spacing_medium)
                val topBottomPadding = resources.getDimensionPixelSize(com.kholhang.kcclabu.R.dimen.spacing_medium)
                hymnText.setPadding(
                    originalPadding + textIndent,
                    topBottomPadding,
                    originalPadding,
                    topBottomPadding
                )
                
                // Improve touch interaction
                hymnText.isFocusable = true
                hymnText.isFocusableInTouchMode = true
                hymnText.isClickable = true
                hymnText.isLongClickable = true
                
                // Better text rendering for improved readability (API 26+ supports this)
                hymnText.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_BALANCED)
                hymnText.hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NORMAL
                
                // Improve text selection and interaction
                hymnText.setMovementMethod(android.text.method.ArrowKeyMovementMethod.getInstance())
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // API 26+ baseline: Use modern typed Parcelable API on API 33+, fallback for API 26-32
        val hymn: Hymn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_HYMN, Hymn::class.java)
        } else {
            arguments?.getParcelable<Hymn>(ARG_HYMN)
        } ?: return
        loadHymnContent(hymn)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh settings when fragment resumes (in case user changed settings)
        binding?.apply {
            // Reapply background color
            val backgroundColor = prefs.getBackgroundColor()
            val constraintLayout = root.getChildAt(0) as? ViewGroup
            constraintLayout?.setBackgroundColor(backgroundColor)
            root.setBackgroundColor(backgroundColor)
            
            // Calculate and apply appropriate text color based on background
            val textColor = calculateTextColor(backgroundColor)
            hymnText.setTextColor(textColor)
            
            // Reapply font settings
            hymnText.apply {
                try {
                    textSize = prefs.getFontSize()
                    typeface = ResourcesCompat.getFont(context, prefs.getFontRes())
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
            
            // Reapply chorus color
            val hymn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(ARG_HYMN, Hymn::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable<Hymn>(ARG_HYMN)
            }
            hymn?.let { loadHymnContent(it) }
        }
    }
    
    private fun loadHymnContent(hymn: Hymn) {
        val htmlContent = if (hymn.editedContent.isNullOrEmpty()) {
            hymn.content
        } else {
            hymn.editedContent
        }
        
        // Parse HTML and apply chorus color
        binding?.hymnText?.apply {
            val spannable = applyChorusColor(htmlContent ?: "")
            this.text = spannable
        }
    }
    
    private fun applyChorusColor(htmlContent: String): Spannable {
        // First parse HTML to get plain text
        val plainText = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableString(plainText)
        val chorusColor = prefs.getChorusColor()
        
        if (!prefs.isHighlightsEnabled()) {
            return spannable
        }
        
        // Find chorus sections in HTML - they're typically wrapped in <i><b>CHORUS</b>...content...</i>
        // Pattern to match: <i>...<b>CHORUS</b>...content...</i>
        val chorusPattern = "(?i)<i>\\s*<b>\\s*CHORUS\\s*</b>.*?</i>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matches = chorusPattern.findAll(htmlContent)
        
        matches.forEach { matchResult ->
            // Extract the HTML chorus section
            val htmlChorus = matchResult.value
            // Convert to plain text to find position in spannable
            val plainChorus = HtmlCompat.fromHtml(htmlChorus, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
            
            // Find all occurrences of this chorus text in the plain text
            var searchIndex = 0
            while (true) {
                val index = plainText.indexOf(plainChorus, searchIndex, ignoreCase = true)
                if (index == -1) break
                
                val endIndex = index + plainChorus.length
                if (endIndex <= spannable.length) {
                    spannable.setSpan(
                        ForegroundColorSpan(chorusColor),
                        index,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                searchIndex = index + 1
            }
        }
        
        // Also check for lines that contain "CHORUS" keyword (fallback)
        val lines = plainText.split("\n")
        var currentIndex = 0
        
        lines.forEach { line ->
            val lineUpper = line.trim().uppercase()
            if (lineUpper.contains("CHORUS") || lineUpper.contains("REFRAIN")) {
                val lineStart = currentIndex
                val lineEnd = kotlin.math.min(currentIndex + line.length, spannable.length)
                
                if (lineStart < spannable.length && lineEnd > lineStart) {
                    spannable.setSpan(
                        ForegroundColorSpan(chorusColor),
                        lineStart,
                        lineEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            currentIndex += line.length + 1
            if (currentIndex > spannable.length) currentIndex = spannable.length
        }
        
        return spannable
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
        private const val ARG_HYMN = "arg:hymn"

        fun newInstance(hymn: Hymn): HymnFragment = HymnFragment().apply {
            arguments = bundleOf(ARG_HYMN to hymn)
        }
    }
}
