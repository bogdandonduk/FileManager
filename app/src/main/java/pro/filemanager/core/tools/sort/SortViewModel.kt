package pro.filemanager.core.tools.sort

import androidx.lifecycle.ViewModel

class SortViewModel() : ViewModel() {

    override fun onCleared() {
        SortTool.sheetShown = false
        SortTool.lastViewModel = null
    }
}