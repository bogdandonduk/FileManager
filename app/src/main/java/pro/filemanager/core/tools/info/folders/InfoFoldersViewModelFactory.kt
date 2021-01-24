package pro.filemanager.core.tools.info.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseLibraryItem

class InfoFoldersViewModelFactory(val items: MutableList<BaseFolderItem>) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T = InfoFoldersViewModel(items) as T
}