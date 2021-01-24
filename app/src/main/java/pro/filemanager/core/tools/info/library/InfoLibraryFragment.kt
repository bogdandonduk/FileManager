package pro.filemanager.core.tools.info.library

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.R
import pro.filemanager.core.base.BaseBottomModalSheetFragment
import pro.filemanager.core.base.BaseLibraryItem
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.databinding.FragmentSheetInfoLibraryBinding
import java.text.SimpleDateFormat
import java.util.*

class InfoLibraryFragment : BaseBottomModalSheetFragment() {

    lateinit var binding: FragmentSheetInfoLibraryBinding
    lateinit var viewModel: InfoLibraryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSheetInfoLibraryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        transparifyBackground()

        expandSheet()

        viewModel = ViewModelProvider(this, InfoLibraryViewModelFactory(mutableListOf<BaseLibraryItem>().apply {
            addAll(InfoTool.lastLibraryItems)
        })).get(InfoLibraryViewModel::class.java)

        InfoTool.lastLibraryItems.clear()

        calculateAndDisplayProperties(viewModel.items)
    }

    private fun calculateAndDisplayProperties(items: MutableList<BaseLibraryItem>) {
        items.apply {
            if(this.size == 1) {
                binding.fragmentSheetInfoLibraryName.text = this[0].displayName
                binding.fragmentSheetInfoLibraryPath.text = this[0].data
                binding.fragmentSheetInfoLibrarySize.text = Formatter.formatFileSize(frContext, this[0].size)
                binding.fragmentSheetInfoLibraryDateAdded.text = SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.ROOT).format(Date(this[0].dateModified * 1000))
            } else {
                CoroutineWrapper.globalDefaultScope.launch {
                    var totalSize = 0L

                    forEach {
                        totalSize += it.size
                    }

                    withContext(Main) {
                        binding.fragmentSheetInfoLibrarySize.text = Formatter.formatFileSize(frContext, totalSize)
                    }
                }

                binding.fragmentSheetInfoLibraryNameTitle.visibility = View.GONE
                binding.fragmentSheetInfoLibraryName.visibility = View.GONE
                binding.fragmentSheetInfoLibraryPathTitle.visibility = View.GONE
                binding.fragmentSheetInfoLibraryPath.visibility = View.GONE
                binding.fragmentSheetInfoLibraryDateAddedTitle.visibility = View.GONE
                binding.fragmentSheetInfoLibraryDateAdded.visibility = View.GONE

                binding.fragmentSheetInfoLibraryTotalQuantityTitle.visibility = View.VISIBLE
                binding.fragmentSheetInfoLibraryTotalQuantity.text = String.format(frContext.resources.getString(R.string.info_total_quantity_files_multiple), this.size)
                binding.fragmentSheetInfoLibraryTotalQuantity.visibility = View.VISIBLE
                binding.fragmentSheetInfoLibrarySizeTitle.text = frContext.resources.getString(R.string.info_total_size)
            }
        }
    }
}