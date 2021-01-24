package pro.filemanager.core.tools.info.library

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.core.base.BaseLibraryItem

class InfoLibraryViewModelFactory(val items: MutableList<BaseLibraryItem>) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T = InfoLibraryViewModel(items) as T
}