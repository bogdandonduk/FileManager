package pro.filemanager.core.tools.toolbar_options

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.generics.BaseViewModel
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.tools.rename.RenameTool
import pro.filemanager.images.ImageRepo
import java.io.File
import java.util.*

object ToolbarOptionsTool {

    @Volatile var showingDialogInProgress = false

    @Volatile var currentViewModel: BaseViewModel? = null
    val currentToolBarOptions = mutableListOf<ToolBarOptionItem>()
    @Volatile var currentBottomModalSheetFragment: ToolbarBottomModalSheetFragment? = null

    fun setToolBarOptions(viewModel: BaseViewModel, options: MutableList<ToolBarOptionItem>) {
        currentViewModel = viewModel
        currentToolBarOptions.clear()
        currentToolBarOptions.addAll(options)
    }

    fun getToolbarOptions(bottomModalSheetFragment: ToolbarBottomModalSheetFragment) : MutableList<ToolBarOptionItem> {
        currentBottomModalSheetFragment = bottomModalSheetFragment

        return currentToolBarOptions
    }

    fun showToolBarBottomModalSheetFragment(fm: FragmentManager) {
        if(!showingDialogInProgress) {
            showingDialogInProgress = true

            if(currentViewModel != null) ToolbarBottomModalSheetFragment().show(fm, null)
        }
    }

}