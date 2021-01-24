package pro.filemanager.core.tools.info.library

import androidx.lifecycle.ViewModel
import pro.filemanager.core.base.BaseLibraryItem
import pro.filemanager.core.tools.info.InfoTool

class InfoLibraryViewModel(val items: MutableList<BaseLibraryItem>) : ViewModel() {
    override fun onCleared() {
        InfoTool.sheetShown = false
    }
}