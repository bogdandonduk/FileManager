package pro.filemanager.audio.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.audio.AudioRepo

class AudioAlbumsViewModelFactory(private var audioRepo: AudioRepo) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = AudioAlbumsViewModel(audioRepo) as T

}