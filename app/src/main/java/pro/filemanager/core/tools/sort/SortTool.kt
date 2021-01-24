package pro.filemanager.core.tools.sort

import android.content.Context
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import pro.filemanager.R
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.wrappers.CoroutineWrapper
import java.io.File
import java.util.*

object SortTool {

    const val KEY_SP_IMAGE_LIBRARY_SORT_ORDER = "imageLibrarySortOrder"
    const val KEY_SP_IMAGE_FOLDERS_SORT_ORDER = "imageFoldersSortOrder"

    const val KEY_SP_AUDIO_LIBRARY_SORT_ORDER = "audioLibrarySortOrder"
    const val KEY_SP_AUDIO_FOLDERS_SORT_ORDER = "audioFoldersSortOrder"

    const val KEY_SP_VIDEO_LIBRARY_SORT_ORDER = "videoLibrarySortOrder"
    const val KEY_SP_VIDEO_FOLDERS_SORT_ORDER = "videoFoldersSortOrder"

    const val KEY_SP_DOC_LIBRARY_SORT_ORDER = "docsLibrarySortOrder"
    const val KEY_SP_DOC_FOLDERS_SORT_ORDER = "docsFoldersSortOrder"

    const val KEY_SP_APK_LIBRARY_SORT_ORDER = "apkLibrarySortOrder"
    const val KEY_SP_APK_FOLDERS_SORT_ORDER = "apkFoldersSortOrder"

    const val SORT_ORDER_DATE_RECENT = "dateRecent"
    const val SORT_ORDER_DATE_OLDEST = "dateOldest"

    const val SORT_ORDER_NAME_ALPHABETIC = "nameAlphabetic"
    const val SORT_ORDER_NAME_REVERSED = "nameReversed"

    const val SORT_ORDER_SIZE_LARGEST = "sizeLargest"
    const val SORT_ORDER_SIZE_SMALLEST = "sizeSmallest"

    @Volatile var sheetShown = false

    @Volatile var lastViewModel: BaseViewModel? = null

    fun getImageLibrarySortOptions(context: Context, fragment: SortFragment) : MutableList<SortOptionItem> {
        return mutableListOf<SortOptionItem>().apply {
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_date_recent),
                            SORT_ORDER_DATE_RECENT
                    ) {
                        CoroutineWrapper.globalIOScope.launch {
                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_DATE_RECENT, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if(fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_date_oldest),
                            SORT_ORDER_DATE_OLDEST
                    ) {
                        CoroutineWrapper.globalIOScope.launch {

                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_DATE_OLDEST, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if(fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_name_alphabetic),
                            SORT_ORDER_NAME_ALPHABETIC
                    ) {
                        CoroutineWrapper.globalIOScope.launch {

                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_NAME_ALPHABETIC, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if (fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_name_reversed),
                            SORT_ORDER_NAME_REVERSED
                    ) {
                        CoroutineWrapper.globalIOScope.launch {

                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_NAME_REVERSED, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if(fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_size_largest),
                            SORT_ORDER_SIZE_LARGEST
                    ) {
                        CoroutineWrapper.globalIOScope.launch {
                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_SIZE_LARGEST, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if(fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )
            add(
                    SortOptionItem(
                            context.resources.getString(R.string.sort_option_size_smallest),
                            SORT_ORDER_SIZE_SMALLEST
                    ) {
                        CoroutineWrapper.globalIOScope.launch {

                            lastViewModel?.shouldScrollToTop = true
                            lastViewModel?.setSortOrder(context, SORT_ORDER_SIZE_SMALLEST, true)
                            lastViewModel?.assignItemsLive(context, false)

                            withContext(Dispatchers.Main) {
                                if(fragment.showsDialog)
                                    fragment.dismiss()
                            }
                        }
                    }
            )

        }
    }

    fun showSortBottomModalSheetFragment(fm: FragmentManager, viewModel: BaseViewModel) {
        if(!sheetShown) {
            sheetShown = true

            lastViewModel = viewModel

            SortFragment().show(fm, null)
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