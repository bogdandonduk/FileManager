package pro.filemanager.images.folders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.ImageRepo

class ImageFoldersViewModelFactory(val context: Context, private var imageRepo: ImageRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ImageFoldersViewModel(context, imageRepo) as T

}