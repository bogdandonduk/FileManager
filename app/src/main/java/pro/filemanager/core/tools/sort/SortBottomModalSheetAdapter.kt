package pro.filemanager.core.tools.sort

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.R
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.databinding.LayoutSortBottomModalSheetItemBinding

class SortBottomModalSheetAdapter(val context: Context, val sortOptionItems: MutableList<SortOptionItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<SortBottomModalSheetAdapter.SortBottomModalSheetItemViewHolder>() {
    class SortBottomModalSheetItemViewHolder(val binding: LayoutSortBottomModalSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var sortOptionItem: SortOptionItem

        init {
            binding.layoutSortBottomModalSheetItemRootLayout.setOnClickListener {
                if(this::sortOptionItem.isInitialized)
                    sortOptionItem.action.run()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortBottomModalSheetItemViewHolder =
            SortBottomModalSheetItemViewHolder(LayoutSortBottomModalSheetItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: SortBottomModalSheetItemViewHolder, position: Int) {
        holder.sortOptionItem = sortOptionItems[position]

        holder.binding.layoutSortBottomModalSheetItemRootLayout.post {
            holder.binding.layoutSortBottomModalSheetItemTitle.textSize = (holder.binding.layoutSortBottomModalSheetItemRootLayout.height / 8).toFloat()
        }

        holder.binding.layoutSortBottomModalSheetItemTitle.text = holder.sortOptionItem.title

        if(SortTool.sortingViewModel != null && holder.sortOptionItem.value == SortTool.sortingViewModel!!.currentSortOrder)
            holder.binding.layoutSortBottomModalSheetItemCheckIcon.visibility = View.VISIBLE
        else
            holder.binding.layoutSortBottomModalSheetItemCheckIcon.visibility = View.INVISIBLE

    }

    override fun getItemCount(): Int = sortOptionItems.size
}