package pro.filemanager.core.tools.delete

import androidx.lifecycle.ViewModel
import pro.filemanager.core.base.BaseLibraryItem

class DeleteViewModel(val items: MutableList<BaseLibraryItem>) : ViewModel() {
    override fun onCleared() {
        DeleteTool.dialogShown = false
    }
}