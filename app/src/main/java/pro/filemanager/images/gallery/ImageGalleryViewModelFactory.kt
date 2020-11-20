package pro.filemanager.images.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.ImageItem
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.albums.ImageAlbumItem

class ImageGalleryViewModelFactory(private var imageRepo: ImageRepo, private var albumItem: ImageAlbumItem? = null) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageGalleryViewModel(imageRepo, albumItem) as T

}