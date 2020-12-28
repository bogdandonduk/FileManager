package pro.filemanager.core.tools.sort

import android.content.Context
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.generics.BaseViewModel
import java.io.File
import java.util.*

object SortTool {

    const val KEY_SP_IMAGE_LIBRARY_SORT_ORDER = "imageBrowserSortOrder"
    const val KEY_SP_IMAGE_FOLDERS_SORT_ORDER = "imageAlbumsSortOrder"

    const val KEY_SP_AUDIO_BROWSER_SORT_ORDER = "audioBrowserSortOrder"
    const val KEY_SP_AUDIO_ALBUMS_SORT_ORDER = "audioAlbumsSortOrder"

    const val KEY_SP_VIDEO_BROWSER_SORT_ORDER = "videoBrowserSortOrder"
    const val KEY_SP_VIDEO_ALBUMS_SORT_ORDER = "videoAlbumsSortOrder"

    const val SORT_ORDER_DATE_RECENT = "dateRecent"
    const val SORT_ORDER_DATE_OLDEST = "dateOldest"

    const val SORT_ORDER_NAME_ALPHABETIC = "nameAlphabetic"
    const val SORT_ORDER_NAME_REVERSED = "nameReversed"

    const val SORT_ORDER_SIZE_LARGEST = "sizeLargest"
    const val SORT_ORDER_SIZE_SMALLEST = "sizeSmallest"

    @Volatile var showingDialogInProgress = false

    @Volatile var sortingViewModel: BaseViewModel? = null

    fun getSortOptions(context: Context, bottomModalSheetFragment: SortBottomModalSheetFragment) : MutableList<SortOptionItem> {
        return mutableListOf<SortOptionItem>().apply {
            add(
                    SortOptionItem(context.resources.getString(R.string.sort_option_date_recent)) {
                        ApplicationLoader.ApplicationIOScope.launch {

                            sortingViewModel?.shouldScrollToTop = true
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
                    SortOptionItem(context.resources.getString(R.string.sort_option_date_oldest)) {
                        ApplicationLoader.ApplicationIOScope.launch {

                            sortingViewModel?.shouldScrollToTop = true
                            sortingViewModel?.setSortOrder(context, SORT_ORDER_DATE_OLDEST, true)
                            sortingViewModel?.assignItemsLive(context, false)

//                            sortingViewModel?.startUpdatePulsation(context)
                            withContext(Dispatchers.Main) {
                                if(bottomModalSheetFragment.showsDialog)
                                    bottomModalSheetFragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(context.resources.getString(R.string.sort_option_name_alphabetic)) {
                        ApplicationLoader.ApplicationIOScope.launch {

                            sortingViewModel?.shouldScrollToTop = true
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
                    SortOptionItem(context.resources.getString(R.string.sort_option_name_reversed)) {
                        ApplicationLoader.ApplicationIOScope.launch {

                            sortingViewModel?.shouldScrollToTop = true
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
                    SortOptionItem(context.resources.getString(R.string.sort_option_size_largest)) {
                        ApplicationLoader.ApplicationIOScope.launch {
                            sortingViewModel?.shouldScrollToTop = true
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
                    SortOptionItem(context.resources.getString(R.string.sort_option_size_smallest)) {
                        ApplicationLoader.ApplicationIOScope.launch {

                            sortingViewModel?.shouldScrollToTop = true
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
        if(!showingDialogInProgress) {
            showingDialogInProgress = true

            sortingViewModel = viewModel

            SortBottomModalSheetFragment().show(fm, null)
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