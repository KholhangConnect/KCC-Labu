package com.kholhang.kcclabu.ui.hymns.sing.style

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.TextStyleModel
import com.kholhang.kcclabu.data.model.constants.UiPref
import com.kholhang.kcclabu.databinding.FragmentTextOptionsBinding

class TextStyleFragment : BottomSheetDialogFragment() {

    private var styleChanges: TextStyleChanges? = null

    private var binding: FragmentTextOptionsBinding? = null

    override fun getTheme(): Int = R.style.ThemeOverlay_MTL_BottomSheetDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentTextOptionsBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // API 26+ baseline: Use modern typed Parcelable API on API 33+, fallback for API 26-32
        val model = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_MODEL, TextStyleModel::class.java)
        } else {
            arguments?.getParcelable<TextStyleModel>(ARG_MODEL)
        } ?: return

        binding?.apply {
            // Use individual chip listeners instead of deprecated ChipGroup listener
            // Get chips from ChipGroup using findViewById
            chipGroupTheme.findViewById<com.google.android.material.chip.Chip>(R.id.chipThemeSystem)?.setOnClickListener {
                chipGroupTheme.check(R.id.chipThemeSystem)
                styleChanges?.updateTheme(UiPref.FOLLOW_SYSTEM)
                dismiss()
            }
            chipGroupTheme.findViewById<com.google.android.material.chip.Chip>(R.id.chipThemeLight)?.setOnClickListener {
                chipGroupTheme.check(R.id.chipThemeLight)
                styleChanges?.updateTheme(UiPref.DAY)
                dismiss()
            }
            chipGroupTheme.findViewById<com.google.android.material.chip.Chip>(R.id.chipThemeDark)?.setOnClickListener {
                chipGroupTheme.check(R.id.chipThemeDark)
                styleChanges?.updateTheme(UiPref.NIGHT)
                dismiss()
            }
            chipGroupTheme.check(
                when (model.pref) {
                    UiPref.DAY -> R.id.chipThemeLight
                    UiPref.NIGHT -> R.id.chipThemeDark
                    else -> R.id.chipThemeSystem
                }
            )

            // Use individual chip listeners instead of deprecated ChipGroup listener
            chipGroupTypeface.findViewById<com.google.android.material.chip.Chip>(R.id.chipTypefaceProxima)?.setOnClickListener {
                chipGroupTypeface.check(R.id.chipTypefaceProxima)
                styleChanges?.updateTypeFace(R.font.proxima_nova_soft_regular)
            }
            chipGroupTypeface.findViewById<com.google.android.material.chip.Chip>(R.id.chipTypefaceLato)?.setOnClickListener {
                chipGroupTypeface.check(R.id.chipTypefaceLato)
                styleChanges?.updateTypeFace(R.font.lato)
            }
            chipGroupTypeface.findViewById<com.google.android.material.chip.Chip>(R.id.chipTypefaceAndada)?.setOnClickListener {
                chipGroupTypeface.check(R.id.chipTypefaceAndada)
                styleChanges?.updateTypeFace(R.font.andada)
            }
            chipGroupTypeface.findViewById<com.google.android.material.chip.Chip>(R.id.chipTypefaceRoboto)?.setOnClickListener {
                chipGroupTypeface.check(R.id.chipTypefaceRoboto)
                styleChanges?.updateTypeFace(R.font.roboto)
            }
            chipGroupTypeface.findViewById<com.google.android.material.chip.Chip>(R.id.chipTypefaceGentium)?.setOnClickListener {
                chipGroupTypeface.check(R.id.chipTypefaceGentium)
                styleChanges?.updateTypeFace(R.font.gentium_basic)
            }
            chipGroupTypeface.check(
                when (model.fontRes) {
                    R.font.lato -> R.id.chipTypefaceLato
                    R.font.andada -> R.id.chipTypefaceAndada
                    R.font.roboto -> R.id.chipTypefaceRoboto
                    R.font.gentium_basic -> R.id.chipTypefaceGentium
                    else -> R.id.chipTypefaceProxima
                }
            )

            sizeSlider.apply {
                value = model.textSize
                addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        styleChanges?.updateTextSize(value)
                    }
                }
                setLabelFormatter {
                    when (it) {
                        14f -> "xSmall"
                        18f -> "Small"
                        22f -> "Medium"
                        26f -> "Large"
                        30f -> "xLarge"
                        else -> ""
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_MODEL = "arg:model"

        fun newInstance(model: TextStyleModel, callback: TextStyleChanges): TextStyleFragment =
            TextStyleFragment().apply {
                styleChanges = callback
                arguments = bundleOf(ARG_MODEL to model)
            }
    }
}
