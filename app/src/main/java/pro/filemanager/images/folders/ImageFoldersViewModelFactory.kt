package pro.filemanager.images.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.ImageRepo

class ImageFoldersViewModelFactory(private var imageRepo: ImageRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageFoldersViewModel(imageRepo) as T

}