package pro.filemanager.core.tools.sort

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.filemanager.R
import pro.filemanager.databinding.LayoutSortBottomModalSheetBinding

class SortBottomModalSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding: LayoutSortBottomModalSheetBinding
    val optionItems = mutableListOf<OptionItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutSortBottomModalSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        SortTool.showingDialogInProgress = false
        SortTool.sortingViewModel = null
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

        optionItems.addAll(SortTool.getSortOptions(requireContext(), this@SortBottomModalSheetFragment))

        binding.layoutSortBottomModalSheetList.adapter = SortBottomModalSheetAdapter(requireContext(), optionItems, layoutInflater)
        binding.layoutSortBottomModalSheetList.layoutManager = LinearLayoutManager(requireContext())

    }

}