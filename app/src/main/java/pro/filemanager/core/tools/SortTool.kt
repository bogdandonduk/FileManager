package pro.filemanager.core.tools

import android.util.Log
import org.apache.commons.io.comparator.SizeFileComparator
import java.io.File
import java.util.*

object SortTool {
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