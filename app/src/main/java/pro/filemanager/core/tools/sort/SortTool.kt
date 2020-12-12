package pro.filemanager.core.tools.sort

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
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

    const val KEY_SP_IMAGE_BROWSER_SORT_ORDER = "imageBrowserSortOrder"
    const val KEY_SP_IMAGE_ALBUMS_SORT_ORDER = "imageAlbumsSortOrder"

    const val SORT_ORDER_DATE_RECENT = "dateRecent"
    const val SORT_ORDER_DATE_OLDEST = "dateOldest"

    const val SORT_ORDER_NAME_ALPHABETIC = "nameAlphabetic"
    const val SORT_ORDER_NAME_REVERSED = "nameReversed"

    const val SORT_ORDER_SIZE_LARGEST = "sizeLargest"
    const val SORT_ORDER_SIZE_SMALLEST = "sizeSmallest"

    @Volatile var showingDialogInProgress = false

    @Volatile var sortingViewModel: BaseViewModel? = null

    fun getSortOptions(context: Context, bottomModalSheetFragment: SortBottomModalSheetFragment) : MutableList<OptionItem> {
        return mutableListOf<OptionItem>().apply {
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_date_recent)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_DATE_RECENT, true)
                            sortingViewModel?.assignItemsLive(context, false)
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
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_DATE_OLDEST, true)
                            sortingViewModel?.assignItemsLive(context, false)
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
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_NAME_ALPHABETIC, true)
                            sortingViewModel?.assignItemsLive(context, false)
                            withContext(Dispatchers.Main) {
                                if (bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    OptionItem(context.resources.getString(R.string.sort_option_name_reversed)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_NAME_REVERSED, true)
                            sortingViewModel?.assignItemsLive(context, false)
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
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_SIZE_LARGEST, true)
                            sortingViewModel?.assignItemsLive(context, false)
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
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_SIZE_SMALLEST, true)
                            sortingViewModel?.assignItemsLive(context, false)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }
                        }
                    }
            )

        }
    }

    fun showSortBottomModalSheetFragment(fm: FragmentManager, viewModel: BaseViewModel) {
        showingDialogInProgress = true

        sortingViewModel = viewModel

        SortBottomModalSheetFragment().show(fm, null)
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