package pro.filemanager.core

import androidx.lifecycle.ViewModelProvider
import pro.filemanager.audios.AudioBrowserViewModelFactory
import pro.filemanager.audios.AudioRepo
import pro.filemanager.docs.DocBrowserViewModelFactory
import pro.filemanager.docs.DocRepo
import pro.filemanager.files.FileBrowserViewModelFactory
import pro.filemanager.images.ImageBrowserViewModelFactory
import pro.filemanager.images.ImageItem
import pro.filemanager.images.gallery.ImageGalleryViewModelFactory
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.albums.ImageAlbumItem
import pro.filemanager.images.albums.ImageAlbumsViewModelFactory
import pro.filemanager.videos.VideoBrowserViewModelFactory
import pro.filemanager.videos.VideoRepo
import kotlin.IllegalArgumentException

object SimpleInjector {

    fun provideVideoBrowserViewModelFactory() : VideoBrowserViewModelFactory = VideoBrowserViewModelFactory(VideoRepo.getInstance())

    fun provideImageBrowserViewModelFactory() : ImageBrowserViewModelFactory = ImageBrowserViewModelFactory()
    fun provideImageGalleryViewModelFactory(albumItem: ImageAlbumItem? = null) : ImageGalleryViewModelFactory = ImageGalleryViewModelFactory(ImageRepo.getInstance(), albumItem)
    fun provideImageAlbumsViewModelFactory() : ImageAlbumsViewModelFactory = ImageAlbumsViewModelFactory(ImageRepo.getInstance())

    fun provideDocBrowserViewModelFactory() : DocBrowserViewModelFactory = DocBrowserViewModelFactory(DocRepo.getInstance())
    fun provideAudioBrowserViewModelFactory() : AudioBrowserViewModelFactory = AudioBrowserViewModelFactory(AudioRepo.getInstance())

    fun provideFileBrowserViewModelFactory() : FileBrowserViewModelFactory = FileBrowserViewModelFactory()

}