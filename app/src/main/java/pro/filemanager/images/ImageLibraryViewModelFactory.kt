package pro.filemanager.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.folders.ImageFolderItem

class ImageLibraryViewModelFactory(private var imageRepo: ImageRepo, private var folderItem: ImageFolderItem?) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageLibraryViewModel(imageRepo, folderItem) as T

}