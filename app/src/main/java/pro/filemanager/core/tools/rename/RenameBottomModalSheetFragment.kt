package pro.filemanager.core.tools.rename

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.generics.BaseBottomSheetDialogFragment
import pro.filemanager.databinding.LayoutRenameBottomModalSheetBinding
import java.io.File

class RenameBottomModalSheetFragment : BaseBottomSheetDialogFragment() {

    lateinit var binding: LayoutRenameBottomModalSheetBinding
    lateinit var invalidNameToast: Toast
    lateinit var alreadyExistsToast: Toast
    var oldPath: String? = null
    var oldNameWithoutExtension = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutRenameBottomModalSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        RenameTool.showingDialogInProgress = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(dialog!!.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!).state = BottomSheetBehavior.STATE_EXPANDED
        }

        (view?.parent as View).run {
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(5, 0, 5, 0)
        }

        invalidNameToast = Toast.makeText(frContext, frContext.resources.getString(R.string.title_rename_enter_valid), Toast.LENGTH_SHORT)
        alreadyExistsToast = Toast.makeText(frContext, frContext.resources.getString(R.string.title_file_already_exists), Toast.LENGTH_SHORT)

        oldPath = requireArguments().getString(RenameTool.KEY_ARGUMENT_PATH_TO_RENAME)
        oldNameWithoutExtension = File(oldPath!!).nameWithoutExtension

        binding.layoutRenameBottomModalSheetRenameEditText.text = Editable.Factory.getInstance().newEditable(oldNameWithoutExtension)

        binding.layoutRenameBottomModalSheetRenameEditText.setOnEditorActionListener { _, actionId: Int, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.layoutRenameBottomModalSheetRenameEditText.text.toString()

                if(text.isNotEmpty() && text.isNotBlank()) {
                    try {
                        RenameTool.rename(frContext, requireArguments().getString(RenameTool.KEY_ARGUMENT_PATH_TO_RENAME)!!, oldPath!!.replace(oldNameWithoutExtension, text))
                        (requireActivity() as HomeActivity).onBackPressed()
                        dismiss()

                        false
                    } catch(thr: Throwable) {
                        if(thr is IllegalArgumentException && thr.message != null && thr.message!!.contains("File already exists", true))
                            alreadyExistsToast.show()

                        true
                    }
                } else {
                    invalidNameToast.show()

                    true
                }
            } else {
                true
            }
        }


        binding.layoutRenameBottomModalSheetConfirmBtn.setOnClickListener {
            binding.layoutRenameBottomModalSheetRenameEditText.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }

        (frContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }
}