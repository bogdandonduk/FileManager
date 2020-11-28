package pro.filemanager.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoBrowserViewModelFactory(private var imageRepo: VideoRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = VideoBrowserViewModel(imageRepo) as T

}