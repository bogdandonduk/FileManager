package pro.filemanager.core

//import pro.filemanager.audio.AudioBrowserViewModelFactory
//import pro.filemanager.audio.AudioRepo
//import pro.filemanager.audio.albums.AudioAlbumItem
//import pro.filemanager.audio.albums.AudioAlbumsViewModelFactory
//import pro.filemanager.docs.DocBrowserViewModelFactory
//import pro.filemanager.docs.DocRepo
import pro.filemanager.files.FileBrowserViewModelFactory
import pro.filemanager.images.ImageBrowserViewModelFactory
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.albums.ImageAlbumItem
import pro.filemanager.images.albums.ImageAlbumsViewModelFactory
//import pro.filemanager.video.VideoBrowserViewModelFactory
//import pro.filemanager.video.VideoRepo

object SimpleInjector {

//    fun provideVideoBrowserViewModelFactory() : VideoBrowserViewModelFactory = VideoBrowserViewModelFactory(VideoRepo.getInstance())

    fun provideImageBrowserViewModelFactory(albumItem: ImageAlbumItem?) : ImageBrowserViewModelFactory = ImageBrowserViewModelFactory(ImageRepo.getSingleton(), albumItem)

    fun provideImageAlbumsViewModelFactory() : ImageAlbumsViewModelFactory = ImageAlbumsViewModelFactory(ImageRepo.getSingleton())

//    fun provideDocBrowserViewModelFactory() : DocBrowserViewModelFactory = DocBrowserViewModelFactory(DocRepo.getInstance())
//    fun provideAudioBrowserViewModelFactory(albumItem: AudioAlbumItem?) : AudioBrowserViewModelFactory = AudioBrowserViewModelFactory(AudioRepo.getInstance(), albumItem)

//    fun provideAudioAlbumsViewModelFactory() : AudioAlbumsViewModelFactory = AudioAlbumsViewModelFactory(AudioRepo.getInstance())

    fun provideFileBrowserViewModelFactory(path: String) : FileBrowserViewModelFactory = FileBrowserViewModelFactory(path)

}