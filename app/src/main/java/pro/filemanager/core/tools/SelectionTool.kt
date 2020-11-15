package pro.filemanager.core.tools

import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
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
    val selectedPositions = mutableListOf<Int>()

    fun handleClickInViewHolder(@ClickType clickType: Int, position: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, activity: HomeActivity, offAction: Runnable = Runnable {}) {
        if(clickType == CLICK_SHORT) {

            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                } else {
                    selectedPositions.remove(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        overrideOnBackBehavior(activity, adapter)
                    }
                }

                adapter.notifyItemChanged(position)
            }
        } else if(clickType == CLICK_LONG){
            if(!selectionMode) {
                selectionMode = true
                selectedPositions.add(position)

                overrideOnBackBehavior(activity, adapter)
            } else {
                if(!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                } else {
                    selectedPositions.remove(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        overrideOnBackBehavior(activity, adapter)
                    }
                }
            }

            adapter.notifyItemChanged(position)
        }
    }

    fun overrideOnBackBehavior(activity: HomeActivity, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        activity.onBackBehavior = if(selectionMode) {

            Runnable {
                val copy = mutableListOf<Int>()

                copy.addAll(selectedPositions)

                selectedPositions.clear()
                selectionMode = false

                copy.forEach {
                    adapter.notifyItemChanged(it)
                }

                activity.onBackBehavior = null
            }

        } else {
            null
        }
    }
}