package pro.filemanager

import android.app.Application
import android.content.Context
import android.os.FileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import pro.filemanager.images.ImageManager
import pro.filemanager.videos.VideoManager
import kotlinx.coroutines.launch
import pro.filemanager.audios.AudioManager
import pro.filemanager.core.FileSystemObserver
import pro.filemanager.docs.DocManager
import pro.filemanager.files.FileManager

class ApplicationLoader : Application() {

    companion object {
        lateinit var context: Context

        val ApplicationIOScope = CoroutineScope(IO)

        val fileSystemObserver: FileSystemObserver = FileSystemObserver(FileManager.getInternalDownMostRootPath(), FileObserver.ALL_EVENTS)

        fun loadAll() {
            loadVideos()
            loadImages()
            findExternalRoot()
            loadDocs()
            loadAudios()
        }

        fun loadVideos() {
            ApplicationIOScope.launch {
                VideoManager.loadVideos(context)
            }
        }

        fun loadImages() {
            ApplicationIOScope.launch {
                ImageManager.loadImages(context)
            }
        }

        fun findExternalRoot() {
            ApplicationIOScope.launch {
                FileManager.findExternalRoot(context)
            }
        }

        fun loadDocs() {
            ApplicationIOScope.launch {
                DocManager.loadDocs(context)
            }
        }

        fun loadAudios() {
            ApplicationIOScope.launch {
                AudioManager.loadAudios(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        context = this

        loadAll()

//        fileSystemObserver.startWatching()

    }

}