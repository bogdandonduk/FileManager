package pro.filemanager.core.tools.info.folders

import androidx.lifecycle.ViewModel
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseLibraryItem
import pro.filemanager.core.tools.info.InfoTool

class InfoFoldersViewModel(val items: MutableList<BaseFolderItem>) : ViewModel() {
    override fun onCleared() {
        InfoTool.sheetShown = false
    }
}