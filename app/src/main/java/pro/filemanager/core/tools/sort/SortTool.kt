package pro.filemanager.core.tools.sort

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.base.BaseViewModel
import java.io.File
import java.util.*

object SortTool {

    const val KEY_ARGUMENT_SORTING_VIEW_MODEL = "sortingViewModel"

    const val KEY_SP_IMAGE_BROWSER_SORT_ORDER = "imageBrowserSortOrder"
    const val KEY_SP_IMAGE_ALBUMS_SORT_ORDER = "imageAlbumsSortOrder"

    const val SORT_ORDER_DATE_RECENT = "dateRecent"
    const val SORT_ORDER_DATE_OLDEST = "dateOldest"

    const val SORT_ORDER_NAME_ALPHABETIC = "nameAlphabetic"
    const val SORT_ORDER_NAME_REVERSED = "nameReversed"

    const val SORT_ORDER_SIZE_LARGEST = "sizeLargest"
    const val SORT_ORDER_SIZE_SMALLEST = "sizeSmallest"

    var sortingInProgress = false

    fun getSortOptions(context: Context, viewModel: BaseViewModel, bottomModalSheetFragment: SortBottomModalSheetFragment) : MutableList<OptionItem> {
        return mutableListOf<OptionItem>().apply {
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_date_recent)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_DATE_RECENT, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }

                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_date_oldest)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_DATE_OLDEST, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }

                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_name_alphabetic)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_NAME_ALPHABETIC, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }

                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_name_reversed)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_NAME_REVERSED, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }

                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_size_largest)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_SIZE_LARGEST, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_size_smallest)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            viewModel.setSortOrder(context, SortTool.SORT_ORDER_SIZE_SMALLEST, true)
                            viewModel.assignItemsLive(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }
                        }
                    }
            )
        }
    }

    fun sortFilesBySizeLargest(files: MutableList<File>) : MutableList<File> {
        return files.apply {
            Collections.sort(files, SizeFileComparator.SIZE_REVERSE)
        }
    }

    fun sortFilesByNameReversed(files: MutableList<File>) : MutableList<File> {
        return files.apply {
            Collections.sort(files, NameFileComparator.NAME_REVERSE)
        }
    }

}