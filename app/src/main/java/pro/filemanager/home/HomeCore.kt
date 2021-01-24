package pro.filemanager.home

import android.content.Context
import androidx.fragment.app.FragmentManager
import pro.filemanager.R
import pro.filemanager.core.ui.FragmentWrapper

object HomeCore {
    fun getHomeSectionItems(context: Context, fm: FragmentManager, containerId: Int) : MutableList<HomeSectionItem> =
            mutableListOf<HomeSectionItem>().apply {
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_internal_storage),
                                R.drawable.ic_baseline_music_note_24
                        ) {

                        }

                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_external_storage),
                                R.drawable.ic_baseline_music_note_24
                        ) {

                        }

                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_images),
                                R.drawable.ic_baseline_image_24
                        ) {
                            fm.beginTransaction().replace(containerId, pro.filemanager.apps.all.AllAppsFragment()).addToBackStack(FragmentWrapper.NAME_IMAGE_LIBRARY_FRAGMENT).commit()
                        }
                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_video),
                                R.drawable.ic_baseline_play_circle_24
                        ) {

                        }

                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_audio),
                                R.drawable.ic_baseline_music_note_24
                        ) {

                        }

                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_docs),
                                R.drawable.ic_baseline_insert_drive_file_24
                        ) {

                        }

                )
                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_apks),
                                R.drawable.ic_baseline_android_24
                        ) {

                        }

                )

                add(
                        HomeSectionItem(
                                context.resources.getString(R.string.title_apps),
                                R.drawable.ic_baseline_android_24
                        ) {
                            fm.beginTransaction().replace(containerId, AllAppsFragment()).addToBackStack(FragmentWrapper.NAME_IMAGE_LIBRARY_FRAGMENT).commit()
                        }

                )
            }
}