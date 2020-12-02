package pro.filemanager.core.tools.sort

import org.apache.commons.io.comparator.SizeFileComparator
import java.io.File
import java.util.*

object SortTool {

    const val KEY_ARGUMENT_SORTING_VIEW_MODEL = "sortingViewModel"

    const val KEY_SP_IMAGE_BROWSER_SORT_ORDER = "imageBrowserSortOrder"

    const val SORT_ORDER_DATE_RECENT = "dateRecent"
    const val SORT_ORDER_DATE_OLDEST = "dateOldest"

    const val SORT_ORDER_NAME_ALPHABET = "nameAlphabet"
    const val SORT_ORDER_NAME_REVERSED = "nameReversed"

    const val SORT_ORDER_SIZE_LARGEST = "sizeLargest"
    const val SORT_ORDER_SIZE_SMALLEST = "sizeSmallest"

    fun sortFilesBySizeMin(files: MutableList<File>) : MutableList<File> {
        return files.apply {
            Collections.sort(files, SizeFileComparator.SIZE_COMPARATOR)
        }
    }

    fun sortFilesBySizeMax(files: MutableList<File>) : MutableList<File> {
        return files.apply {
            Collections.sort(files, SizeFileComparator.SIZE_REVERSE)
        }
    }

}