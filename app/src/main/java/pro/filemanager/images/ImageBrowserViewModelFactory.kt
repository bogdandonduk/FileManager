package pro.filemanager.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageBrowserViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageBrowserViewModel() as T

}