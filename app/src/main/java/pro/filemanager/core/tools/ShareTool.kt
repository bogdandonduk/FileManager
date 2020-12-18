package pro.filemanager.core.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import pro.filemanager.R
import pro.filemanager.images.ImageCore
import java.io.File
import java.util.*

object ShareTool {

    var outerSharingIntentInProgress = false

    fun shareImages(context: Context, paths: MutableList<String>) {
        if(!outerSharingIntentInProgress) {
            outerSharingIntentInProgress = true

            if(paths.size == 1) {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            context,
                            context.packageName + ".fileProvider",
                            File(paths[0])
                        ))
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(paths[0]).toLowerCase(Locale.ROOT)) ?: ImageCore.MIME_TYPE
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, context.resources.getString(R.string.toolbar_share)))
            } else if(paths.size > 1) {
                var totalSize: Long = 0

                paths.forEach {
                    totalSize += File(it).length()
                }

                if(paths.size <= 500 && totalSize <= 1073741824) {
                    try {
                        context.startActivity(
                                Intent.createChooser(
                                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                            putParcelableArrayListExtra(
                                                    Intent.EXTRA_STREAM,
                                                    arrayListOf<Uri>().apply {
                                                        paths.forEach {
                                                            add(
                                                                    FileProvider.getUriForFile(
                                                                            context,
                                                                            context.packageName + ".fileProvider",
                                                                            File(it)
                                                                    )
                                                            )
                                                        }
                                                    })
                                            type = ImageCore.MIME_TYPE
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }, context.resources.getString(R.string.toolbar_share)
                                )
                        )
                    } catch(thr: Throwable) {
                        Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                }
            } else {
                throw IllegalArgumentException("Illegal filepaths")
            }
        }
    }

    fun shareAudios(context: Context, paths: MutableList<String>) {
        if(!outerSharingIntentInProgress) {
            outerSharingIntentInProgress = true

            if(paths.size == 1) {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            context,
                            context.packageName + ".fileProvider",
                            File(paths[0])
                        ))
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(paths[0]).toLowerCase(Locale.ROOT)) ?: "audio/*"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, context.resources.getString(R.string.toolbar_share)))
            } else if(paths.size > 1) {
                var totalSize: Long = 0

                paths.forEach {
                    totalSize += File(it).length()
                }

                if(paths.size <= 500 && totalSize <= 1073741824) {
                    try {
                        context.startActivity(
                                Intent.createChooser(
                                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                            putParcelableArrayListExtra(
                                                    Intent.EXTRA_STREAM,
                                                    arrayListOf<Uri>().apply {
                                                        paths.forEach {
                                                            add(
                                                                    FileProvider.getUriForFile(
                                                                            context,
                                                                            context.packageName + ".fileProvider",
                                                                            File(it)
                                                                    )
                                                            )
                                                        }
                                                    })
                                            type = ImageCore.MIME_TYPE
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }, context.resources.getString(R.string.toolbar_share)
                                )
                        )
                    } catch(thr: Throwable) {
                        Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                }
            } else {
                throw IllegalArgumentException("Illegal filepaths")
            }
        }
    }

    fun shareVideos(context: Context, paths: MutableList<String>) {
        if(!outerSharingIntentInProgress) {
            outerSharingIntentInProgress = true

            if(paths.size == 1) {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            context,
                            context.packageName + ".fileProvider",
                            File(paths[0])
                        ))
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(paths[0]).toLowerCase(Locale.ROOT)) ?: "video/*"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, context.resources.getString(R.string.toolbar_share)))
            } else if(paths.size > 1) {
                var totalSize: Long = 0

                paths.forEach {
                    totalSize += File(it).length()
                }

                if(paths.size <= 500 && totalSize <= 1073741824) {
                    try {
                        context.startActivity(
                                Intent.createChooser(
                                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                            putParcelableArrayListExtra(
                                                    Intent.EXTRA_STREAM,
                                                    arrayListOf<Uri>().apply {
                                                        paths.forEach {
                                                            add(
                                                                    FileProvider.getUriForFile(
                                                                            context,
                                                                            context.packageName + ".fileProvider",
                                                                            File(it)
                                                                    )
                                                            )
                                                        }
                                                    })
                                            type = ImageCore.MIME_TYPE
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }, context.resources.getString(R.string.toolbar_share)
                                )
                        )
                    } catch(thr: Throwable) {
                        Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                }
            } else {
                throw IllegalArgumentException("Illegal filepaths")
            }
        }
    }
}