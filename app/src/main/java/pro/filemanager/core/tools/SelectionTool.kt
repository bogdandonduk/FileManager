package pro.filemanager.core.tools

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity

class SelectionTool {
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ClickType
    }

    var selectionMode = false
    val selectedPaths = mutableListOf<String>()

    fun handleClickInViewHolder(
            @ClickType clickType: Int,
            position: Int,
            path: String,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            refreshAction: Runnable = Runnable {},
            offAction: Runnable = Runnable {}
    ) {
        if(clickType == CLICK_SHORT) {
            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                adapter.notifyItemChanged(position)
            }
        } else if(clickType == CLICK_LONG){
            if(!selectionMode) {
                selectionMode = true
                selectedPaths.add(path)

                refreshAction.run()

                for (i in 0 until adapter.itemCount) {
                   adapter.notifyItemChanged(i)
                }
            } else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                adapter.notifyItemChanged(position)
            }
        }
    }

    fun updateAll(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        for (i in 0 until adapter.itemCount) {
            adapter.notifyItemChanged(i)
        }
    }

    fun selectAll(items: MutableList<String>, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        selectedPaths.clear()
        selectedPaths.addAll(items)

        updateAll(adapter)
    }

    fun unselectAll(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        selectedPaths.clear()

        updateAll(adapter)
    }

    fun initOnBackCallback(
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            selectionCheckBox: CheckBox,
            selectionCheckBoxLayout: ViewGroup,
            toolbarLayout: ViewGroup,
            tabsBarLayout: ViewGroup
    ) {
         activity.currentOnBackBehavior = if(selectionMode)
             Runnable {
                 selectionMode = false

                 unselectAll(adapter)

                 if(selectionCheckBox.isChecked)
                     selectionCheckBox.toggle()

                 selectionCheckBoxLayout.visibility = View.GONE
                 activity.supportActionBar?.show()

                 toolbarLayout.visibility = View.GONE
                 tabsBarLayout.visibility = View.VISIBLE

                 activity.currentOnBackBehavior = null
             }

         else null
    }

    fun differentiateItem(
            path: String,
            thumbnail: ImageView,
            checkMark: ImageView,
            uncheckedMark: ImageView,
    ) {
        if(selectionMode) {
            if(selectedPaths.contains(path)) {
                checkMark.visibility = View.VISIBLE
                uncheckedMark.visibility = View.INVISIBLE

                thumbnail.setColorFilter(Color.argb(100, 0, 0, 0))

                checkMark.scaleX = 0f
                checkMark.scaleY = 0f

                checkMark.visibility = View.VISIBLE
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

    fun initSelectionState(
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            toolbarLayout: ViewGroup,
            tabsBarLayout: ViewGroup,
            selectionCheckBoxLayout: ViewGroup,
            selectionCheckBox: CheckBox,
            selectionMode: Boolean,
            selectedItemCount: Int
    ) {
        if(selectionMode) {
            initOnBackCallback(activity, adapter, selectionCheckBox, selectionCheckBoxLayout, toolbarLayout, tabsBarLayout)

            activity.supportActionBar!!.hide()

            selectionCheckBoxLayout.visibility = View.VISIBLE

            selectionCheckBox.text = selectedItemCount.toString()

            tabsBarLayout.visibility = View.GONE
            toolbarLayout.visibility = View.VISIBLE
        } else {
            activity.supportActionBar!!.show()

            selectionCheckBoxLayout.visibility = View.GONE

            tabsBarLayout.visibility = View.VISIBLE
            toolbarLayout.visibility = View.GONE
        }
    }
}