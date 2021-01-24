package pro.filemanager.apps.all

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.folders.ImageFolderItem

class AllAppsViewModelFactory(val context: Context, private var imageRepo: ImageRepo, private var folderItem: ImageFolderItem?) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = AllAppsViewModel(context, imageRepo, folderItem) as T

}