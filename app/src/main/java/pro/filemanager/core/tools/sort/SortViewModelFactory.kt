package pro.filemanager.core.tools.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.core.base.BaseLibraryItem

class SortViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T = SortViewModel() as T

}