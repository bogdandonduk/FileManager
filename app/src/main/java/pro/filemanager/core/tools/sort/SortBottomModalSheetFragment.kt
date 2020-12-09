package pro.filemanager.core.tools.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.filemanager.databinding.LayoutSortBottomModalSheetBinding
import pro.filemanager.images.ImageCore

class SortBottomModalSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding: LayoutSortBottomModalSheetBinding
    val optionItems = mutableListOf<OptionItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutSortBottomModalSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        optionItems.addAll(SortTool.getSortOptions(requireContext(), requireArguments().getParcelable(SortTool.KEY_ARGUMENT_SORTING_VIEW_MODEL)!!, this))

        binding.layoutSortBottomModalSheetList.adapter = SortBottomModalSheetAdapter(requireContext(), optionItems, layoutInflater)
        binding.layoutSortBottomModalSheetList.layoutManager = LinearLayoutManager(requireContext())
    }

}