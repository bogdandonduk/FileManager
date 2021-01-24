package pro.filemanager.core.tools.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pro.filemanager.core.base.BaseBottomModalSheetFragment
import pro.filemanager.databinding.FragmentSheetSortBinding

class SortFragment : BaseBottomModalSheetFragment() {

    lateinit var binding: FragmentSheetSortBinding
    lateinit var viewModel: SortViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSheetSortBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        transparifyBackground()

        expandSheet()

        viewModel = ViewModelProvider(this, SortViewModelFactory()).get(SortViewModel::class.java)

        initList(SortTool.getImageLibrarySortOptions(frContext, this))
    }

    private fun initList(items: MutableList<SortOptionItem>) {
        binding.layoutSortBottomModalSheetList.adapter = SortAdapter(frContext, items, layoutInflater)
        binding.layoutSortBottomModalSheetList.layoutManager = LinearLayoutManager(frContext)
    }
}