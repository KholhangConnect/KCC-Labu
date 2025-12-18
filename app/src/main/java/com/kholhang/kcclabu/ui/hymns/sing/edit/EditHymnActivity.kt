package com.kholhang.kcclabu.ui.hymns.sing.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.databinding.ActivityEditHymnBinding
import com.kholhang.kcclabu.extensions.arch.observeNonNull
import com.kholhang.kcclabu.ui.widget.SimpleTextWatcher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditHymnActivity : AppCompatActivity() {

    private val viewModel: EditHymnViewModel by viewModels()

    private lateinit var binding: ActivityEditHymnBinding

    private val contentWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(editable: Editable?) {
            super.afterTextChanged(editable)
            binding.btnSave.isVisible = editable?.isNotEmpty() == true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHymnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()

        // API 26+ baseline: Use modern typed Parcelable API on API 33+, fallback for API 26-32
        val hymn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ARG_HYMN, Hymn::class.java)
        } else {
            intent.getParcelableExtra<Hymn>(ARG_HYMN)
        }
        if (hymn == null) {
            finish()
            return
        }

        viewModel.statusLiveData.observeNonNull(this) { status ->
            when (status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    setResult(Activity.RESULT_OK)
                    finishAfterTransition()
                }
                Status.ERROR -> {
                    binding.snackbar.show(
                        messageId = R.string.error_invalid_content,
                        longDuration = true
                    )
                }
            }
        }
        viewModel.editContentLiveData.observeNonNull(this) { content ->
            binding.edtHymn.apply {
                removeTextChangedListener(contentWatcher)
                setText(content.first)
                addTextChangedListener(contentWatcher)
            }
            invalidateOptionsMenu()
        }

        viewModel.setHymn(hymn)
    }

    private fun initUi() {
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            btnSave.setOnClickListener {
                MaterialAlertDialogBuilder(this@EditHymnActivity)
                    .setMessage(R.string.confirm_save)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.title_save) { _, _ ->
                        val text = edtHymn.text?.toString() ?: ""
                        viewModel.saveContent(text)
                    }
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_hymn, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val visible = viewModel.editContentLiveData.value?.second ?: false
        menu?.findItem(R.id.action_undo)?.isVisible = visible
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.action_undo -> {
                MaterialAlertDialogBuilder(this@EditHymnActivity)
                    .setMessage(R.string.undo_changes_confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.title_undo) { _, _ ->
                        viewModel.undoChanges()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    companion object {
        private const val ARG_HYMN = "arg:hymn"

        fun editIntent(context: Context, hymn: Hymn): Intent = Intent(
            context, EditHymnActivity::class.java
        ).apply {
            putExtra(ARG_HYMN, hymn)
        }
    }
}
