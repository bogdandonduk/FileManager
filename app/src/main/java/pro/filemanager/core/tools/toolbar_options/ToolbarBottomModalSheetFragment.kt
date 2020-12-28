package pro.filemanager.core.tools.toolbar_options

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pro.filemanager.R
import pro.filemanager.core.generics.BaseBottomSheetDialogFragment
import pro.filemanager.core.ui.UIManager
import pro.filemanager.databinding.LayoutToolbarBottomModalSheetBinding

class ToolbarBottomModalSheetFragment : BaseBottomSheetDialogFragment() {

    lateinit var binding: LayoutToolbarBottomModalSheetBinding
    val optionItems = mutableListOf<ToolBarOptionItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutToolbarBottomModalSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        ToolbarOptionsTool.showingDialogInProgress = false
        ToolbarOptionsTool.currentViewModel = null
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

        optionItems.addAll(ToolbarOptionsTool.getToolbarOptions(this))

        binding.layoutToolBarBottomModalSheetList.adapter = ToolBarBottomModalSheetAdapter(requireContext(), optionItems, layoutInflater)
        binding.layoutToolBarBottomModalSheetList.layoutManager = GridLayoutManager(frContext, UIManager.getImageLibraryGridSpanNumber(activity))

    }

}