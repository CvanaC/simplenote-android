package com.automattic.simplenote

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.automattic.simplenote.databinding.AddCollaboratorBinding
import com.automattic.simplenote.viewmodels.AddCollaboratorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCollaboratorFragment(private val noteId: String) : AppCompatDialogFragment(), DialogInterface.OnShowListener {
    private val viewModel: AddCollaboratorViewModel by viewModels()

    private var _dialogEditTag: AlertDialog? = null
    private val dialogEditTag get() = _dialogEditTag!!

    private var _binding: AddCollaboratorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddCollaboratorBinding.inflate(LayoutInflater.from(context))
        return buildDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _dialogEditTag = null
    }

    private fun buildDialog(): Dialog {
        val context: Context = ContextThemeWrapper(requireContext(), R.style.Dialog)
        _dialogEditTag = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle(R.string.add_collaborator)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.accept, null)
            .create()

        dialogEditTag.setOnShowListener(this)
        binding.collaboratorInput.editText?.doAfterTextChanged {
            // Clean error message when the user types something new in the field
            if (binding.collaboratorInput.error != null) {
                binding.collaboratorInput.error = null
            }
        }

        return dialogEditTag
    }

    override fun onShow(dialog: DialogInterface?) {
        setObservers()
        setupViews()
    }

    private fun setupViews() {
        val positiveButton = dialogEditTag.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val collaborator = binding.collaboratorText.text.toString()
            viewModel.addCollaborator(noteId, collaborator)
        }
    }

    private fun setObservers() {
        viewModel.event.observe(this, { event ->
            when (event) {
                AddCollaboratorViewModel.Event.Close,
                    // In case the note is deleted or trashed, we let the CollaboratorsActivity to handle it
                AddCollaboratorViewModel.Event.NoteDeleted,
                AddCollaboratorViewModel.Event.NoteInTrash,
                AddCollaboratorViewModel.Event.CollaboratorAdded -> dismiss()
                AddCollaboratorViewModel.Event.InvalidCollaborator -> setErrorInputField()
            }
        })
    }

    private fun setErrorInputField() {
        binding.collaboratorInput.error = getString(R.string.invalid_collaborator)
    }
}
