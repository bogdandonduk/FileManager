package pro.filemanager.core

import android.content.Context
import pro.filemanager.files.FileBrowserViewModelFactory
import pro.filemanager.images.ImageLibraryViewModelFactory
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.folders.ImageFolderItem
import pro.filemanager.images.folders.ImageFoldersViewModelFactory

object SimpleInjector {

//    fun provideVideoBrowserViewModelFactory(albumItem: VideoAlbumItem?) : VideoBrowserViewModelFactory = VideoBrowserViewModelFactory(VideoRepo.getSingleton(), albumItem)
//    fun provideVideoAlbumsViewModelFactory() : VideoAlbumsViewModelFactory = VideoAlbumsViewModelFactory(VideoRepo.getSingleton())

    fun provideImageLibraryViewModelFactory(context: Context, folderItem: ImageFolderItem?) : ImageLibraryViewModelFactory = ImageLibraryViewModelFactory(context, ImageRepo.getSingleton(), folderItem)
    fun provideImageFoldersViewModelFactory(context: Context) : ImageFoldersViewModelFactory = ImageFoldersViewModelFactory(context, ImageRepo.getSingleton())

//    fun provideDocBrowserViewModelFactory() : DocBrowserViewModelFactory = DocBrowserViewModelFactory(DocRepo.getInstance())
//    fun provideAudioBrowserViewModelFactory(albumItem: AudioAlbumItem?) : AudioBrowserViewModelFactory = AudioBrowserViewModelFactory(AudioRepo.getSingleton(), albumItem)
//
//    fun provideAudioAlbumsViewModelFactory() : AudioAlbumsViewModelFactory = AudioAlbumsViewModelFactory(AudioRepo.getSingleton())

    fun provideFileBrowserViewModelFactory(path: String) : FileBrowserViewModelFactory = FileBrowserViewModelFactory(path)

}