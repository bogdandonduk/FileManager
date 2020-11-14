package pro.filemanager.audios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioBrowserViewModelFactory(private var audioRepo: AudioRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = AudioBrowserViewModel(audioRepo) as T

}