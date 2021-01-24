package pro.filemanager.core.tools.info.folders

import android.annotation.SuppressLint
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
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.tools.info.InfoTool
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.databinding.FragmentSheetInfoFoldersBinding
import java.util.*

class InfoFoldersFragment : BaseBottomModalSheetFragment() {

    lateinit var binding: FragmentSheetInfoFoldersBinding
    lateinit var viewModel: InfoFoldersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSheetInfoFoldersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        transparifyBackground()

        expandSheet()

        viewModel = ViewModelProvider(this, InfoFoldersViewModelFactory(mutableListOf<BaseFolderItem>().apply {
            addAll(InfoTool.lastFolderItems)
        })).get(InfoFoldersViewModel::class.java)

        InfoTool.lastFolderItems.clear()

        calculateAndDisplayProperties(viewModel.items)
    }

    @SuppressLint("SetTextI18n")
    private fun calculateAndDisplayProperties(items: MutableList<BaseFolderItem>) {
        items.let { folderItems ->
            if(folderItems.size == 1) {
                CoroutineWrapper.globalDefaultScope.launch {
                    var totalSize = 0L

                    folderItems[0].containedLibraryItems.forEach {
                        totalSize += it.size
                    }

                    withContext(Main) {
                        binding.fragmentSheetInfoFoldersSize.text = Formatter.formatFileSize(frContext, folderItems[0].totalSize)
                    }
                }

                folderItems[0].containedLibraryItems.size.run {
                    binding.fragmentSheetInfoFoldersTotalQuantity.text =
                            if(this == 1) {
                                String.format(Locale.ROOT, frContext.getString(R.string.info_total_quantity_files_single), 1)
                            } else {
                                String.format(Locale.ROOT, frContext.getString(R.string.info_total_quantity_files_multiple), this)
                            } + " " + String.format(Locale.ROOT, frContext.getString(R.string.info_total_quantity_folders_single), 1)
                }

                binding.fragmentSheetInfoFoldersName.text = folderItems[0].displayName
                binding.fragmentSheetInfoFoldersPath.text = folderItems[0].data
            } else {
                CoroutineWrapper.globalDefaultScope.launch {
                    var totalQuantity = 0
                    var totalSize = 0L

                    folderItems.forEach { album ->
                        totalQuantity += album.containedLibraryItems.size
                        album.containedLibraryItems.forEach {
                            totalSize += it.size
                        }
                    }

                    withContext(Main) {
                        binding.fragmentSheetInfoFoldersTotalQuantity.text = String.format(Locale.ROOT, frContext.getString(R.string.info_total_quantity_files_multiple), totalQuantity) + " " + String.format(Locale.ROOT, frContext.getString(R.string.info_total_quantity_folders_multiple), folderItems.size)

                        binding.fragmentSheetInfoFoldersSize.text = Formatter.formatFileSize(frContext, totalSize)
                    }
                }

                binding.fragmentSheetInfoFoldersNameTitle.visibility = View.GONE
                binding.fragmentSheetInfoFoldersName.visibility = View.GONE
                binding.fragmentSheetInfoFoldersPathTitle.visibility = View.GONE
                binding.fragmentSheetInfoFoldersPath.visibility = View.GONE

                binding.fragmentSheetInfoFoldersSizeTitle.text = frContext.resources.getString(R.string.info_total_size)
            }
        }
    }
}