package pro.filemanager.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageBrowserViewModelFactory(private var imageRepo: ImageRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageBrowserViewModel(imageRepo) as T

}