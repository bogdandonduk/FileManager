package pro.filemanager.core

import androidx.core.content.FileProvider

class FileExposer : FileProvider() {
    companion object {
        const val authority: String = ".fileProvider"
    }
}