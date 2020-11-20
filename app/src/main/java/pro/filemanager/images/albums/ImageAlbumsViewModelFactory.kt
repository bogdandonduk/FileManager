package pro.filemanager.images.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.ImageRepo

class ImageAlbumsViewModelFactory(private var imageRepo: ImageRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageAlbumsViewModel(imageRepo) as T

}