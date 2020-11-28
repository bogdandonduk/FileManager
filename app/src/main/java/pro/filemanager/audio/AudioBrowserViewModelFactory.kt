package pro.filemanager.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.filemanager.audio.albums.AudioAlbumItem

class AudioBrowserViewModelFactory(private var audioRepo: AudioRepo, private var albumItem: AudioAlbumItem?) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = AudioBrowserViewModel(audioRepo, albumItem) as T

}