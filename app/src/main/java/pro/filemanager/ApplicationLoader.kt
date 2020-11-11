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

        fun load() {

            ApplicationIOScope.launch {
                VideoManager.loadVideos(context)
            }

            ApplicationIOScope.launch {
                ImageManager.loadImages(context)
            }

            ApplicationIOScope.launch {
                FileManager.findExternalRoot(context)
            }

            ApplicationIOScope.launch {
                DocManager.loadDocs(context)
            }

            ApplicationIOScope.launch {
                AudioManager.loadAudios(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        context = this

        load()

//        fileSystemObserver.startWatching()

    }

}