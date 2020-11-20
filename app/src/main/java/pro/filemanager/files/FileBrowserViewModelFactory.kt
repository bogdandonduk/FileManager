package pro.filemanager.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FileBrowserViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = FileBrowserViewModel() as T

}