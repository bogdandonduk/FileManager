package pro.filemanager.core.tools

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IntDef
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.images.ImageCore

class SelectionTool {
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ClickType
    }

    var selectionMode = false
    val selectedPositions = mutableListOf<Int>()

    fun handleClickInViewHolder(@ClickType clickType: Int, position: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, activity: HomeActivity, offAction: Runnable = Runnable {}) {
        if(clickType == CLICK_SHORT) {
            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                    adapter.notifyItemChanged(position)
                } else {
                    selectedPositions.remove(position)
                    adapter.notifyItemChanged(position)

                    if(selectedPositions.isNotEmpty()) {
                        adapter.notifyItemChanged(position)
                    } else {
                        selectionMode = false

                        for (i in 0 until adapter.itemCount) {
                            adapter.notifyItemChanged(i)
                        }

                        initOnBackCallback(activity, adapter)
                    }
                }


            }
        } else if(clickType == CLICK_LONG){
            if(!selectionMode) {
                selectionMode = true
                selectedPositions.add(position)

                for (i in 0 until adapter.itemCount) {
                    adapter.notifyItemChanged(i)
                }

                initOnBackCallback(activity, adapter)
            } else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                    adapter.notifyItemChanged(position)
                } else {
                    selectedPositions.remove(position)

                    if(selectedPositions.isNotEmpty()) {
                        adapter.notifyItemChanged(position)
                    } else {
                        selectionMode = false

                        for (i in 0 until adapter.itemCount) {
                            adapter.notifyItemChanged(i)
                        }

                        initOnBackCallback(activity, adapter)
                    }
                }
            }

        }
    }

    fun initOnBackCallback(activity: HomeActivity, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
         activity.currentOnBackBehavior = if(selectionMode)
             Runnable {

                 selectedPositions.clear()
                 selectionMode = false

                 for (i in 0 until adapter.itemCount) {
                     adapter.notifyItemChanged(i)
                 }

                 activity.currentOnBackBehavior = null
             }
         else null
    }

    fun differentiateItem(position: Int, thumbnail: ImageView, checkMark: ImageView, uncheckedMark: ImageView) {
        if(selectionMode) {
            if(selectedPositions.contains(position)) {
                checkMark.visibility = View.VISIBLE
                uncheckedMark.visibility = View.INVISIBLE

                thumbnail.setColorFilter(Color.argb(120, 0, 0, 0))

                ImageCore.glideSimpleRequestBuilder
                        .load(R.drawable.ic_baseline_check_circle_24)
                        .into(checkMark)

                checkMark.scaleX = 0f
                checkMark.scaleY = 0f

                checkMark.animate().scaleX(1f).setDuration(150).start()
                checkMark.animate().scaleY(1f).setDuration(150).start()

            } else {
                thumbnail.colorFilter = null

                checkMark.visibility = View.INVISIBLE
                uncheckedMark.visibility = View.VISIBLE
            }
        } else {
            thumbnail.colorFilter = null

            checkMark.visibility = View.INVISIBLE
            uncheckedMark.visibility = View.INVISIBLE
        }
    }
}