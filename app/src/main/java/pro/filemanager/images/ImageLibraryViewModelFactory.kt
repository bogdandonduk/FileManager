package pro.filemanager.images

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.folders.ImageFolderItem

class ImageLibraryViewModelFactory(val context: Context, private var imageRepo: ImageRepo, private var folderItem: ImageFolderItem?) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageLibraryViewModel(context, imageRepo, folderItem) as T

}