package pro.filemanager.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.albums.ImageAlbumItem

class ImageBrowserViewModelFactory(private var imageRepo: ImageRepo, private var albumItem: ImageAlbumItem?) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageBrowserViewModel(imageRepo, albumItem) as T

}