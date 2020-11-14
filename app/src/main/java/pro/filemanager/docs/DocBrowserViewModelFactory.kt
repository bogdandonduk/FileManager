package pro.filemanager.docs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocBrowserViewModelFactory(private var docRepo: DocRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = DocBrowserViewModel(docRepo) as T

}