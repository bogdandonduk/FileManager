package pro.filemanager

import android.app.Application
import android.content.Context
import android.os.FileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import pro.filemanager.images.ImageRepo
import pro.filemanager.videos.VideoRepo
import kotlinx.coroutines.launch
import pro.filemanager.audios.AudioRepo
import pro.filemanager.core.FileSystemObserver
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.docs.DocRepo
import pro.filemanager.files.FileManager

class ApplicationLoader : Application() {

    companion object {
        lateinit var appContext: Context

        val ApplicationIOScope = CoroutineScope(IO)

        val fileSystemObserver: FileSystemObserver = FileSystemObserver(FileManager.getInternalDownMostRootPath(), FileObserver.ALL_EVENTS)

        fun loadAll() {
            loadVideos()
            loadImages()

            findExternalRoot()
            loadDocs()
            loadAudios()
        }

        fun loadVideos(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    VideoRepo.getInstance().loadLive(context)
                }
            }
        }

        fun loadImages(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    ImageRepo.getInstance().loadLive(context)
                }
            }
        }

        fun findExternalRoot(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    FileManager.findExternalRoot(context)
                }
            }
        }

        fun loadDocs(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    DocRepo.getInstance().loadLive(context)
                }
            }
        }

        fun loadAudios(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    AudioRepo.getInstance().loadLive(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        appContext = this

        loadAll()

//        fileSystemObserver.startWatching()

    }

}