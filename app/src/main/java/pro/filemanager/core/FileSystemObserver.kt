package pro.filemanager.core

import android.os.FileObserver
import android.util.Log
import java.io.File
import java.util.*

/**
 * A FileObserver that observes all the files/folders within given directory
 * recursively. It automatically starts/stops monitoring new folders/files
 * created after starting the watch.
 */

class FileSystemObserver : FileObserver {

    private val mObservers: MutableMap<String?, FileObserver> = HashMap()
    private var mPath: String? = null
    private var mMask = 0
    private var mListener: EventListener? = null

    constructor(path: String?, mask: Int) : super(path, mask) {

        // TODO: 12.11.20 MEND
    }

    interface EventListener {
        fun onEvent(event: Int, file: File?)
    }

    constructor(path: String?, listener: EventListener?) : this(path, ALL_EVENTS, listener) {}

    constructor(path: String?, mask: Int, listener: EventListener?) : super(path, mask) {
        mPath = path
        mMask = mask or CREATE or DELETE_SELF
        mListener = listener
    }

    private fun startWatching(path: String?) {
        synchronized(mObservers) {
            var observer = mObservers.remove(path)
            observer?.stopWatching()
            observer = SingleFileObserver(path, mMask)
            observer.startWatching()
            mObservers.put(path, observer)
        }
    }

    override fun startWatching() {
        val stack = Stack<String?>()
        stack.push(mPath)

        // Recursively watch all child directories
        while (!stack.empty()) {
            val parent = stack.pop()
            startWatching(parent)
            val path = File(parent)
            val files = path.listFiles()
            if (files != null) {
                for (file in files) {
                    if (watch(file)) {
                        stack.push(file.absolutePath)
                    }
                }
            }
        }
    }

    private fun watch(file: File): Boolean {
        return file.isDirectory && file.name != "." && file.name != ".."
    }

    private fun stopWatching(path: String?) {
        synchronized(mObservers) {
            val observer = mObservers.remove(path)
            observer?.stopWatching()
        }
    }

    override fun stopWatching() {
        synchronized(mObservers) {
            for (observer in mObservers.values) {
                observer.stopWatching()
            }
            mObservers.clear()
        }
    }

    override fun onEvent(event: Int, path: String?) {
        val file: File
        file = if (path == null) {
            File(mPath)
        } else {
            File(mPath, path)
        }
        Log.d("TAG", "onEvent: SMTH")
        notify(event, file)
    }

    private fun notify(event: Int, file: File) {
        if (mListener != null) {
            mListener!!.onEvent(event and ALL_EVENTS, file)
        }
    }

    private inner class SingleFileObserver(private val filePath: String?, mask: Int) : FileObserver(filePath, mask) {
        override fun onEvent(event: Int, path: String?) {
            val file: File
            file = if (path == null) {
                File(filePath)
            } else {
                File(filePath, path)
            }
            when (event and ALL_EVENTS) {
                DELETE_SELF -> this@FileSystemObserver.stopWatching(filePath)
                CREATE -> if (watch(file)) {
                    this@FileSystemObserver.startWatching(file.absolutePath)
                }
            }
            notify(event, file)
        }
    }
}