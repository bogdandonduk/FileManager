package pro.filemanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.databinding.LayoutHomeSectionItemBinding

class HomeSectionsAdapter(val sections: MutableList<HomeSectionItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<HomeSectionsAdapter.HomeSectionItemViewHolder>() {

    class HomeSectionItemViewHolder(val binding: LayoutHomeSectionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: HomeSectionItem

        init {
            binding.layoutHomeSectionItemRootLayout.setOnClickListener {
                item.action.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeSectionItemViewHolder =
            HomeSectionItemViewHolder(LayoutHomeSectionItemBinding.inflate(layoutInflater))

    override fun onBindViewHolder(holder: HomeSectionItemViewHolder, position: Int) {
        holder.item = sections[position]

        holder.binding.layoutHomeSectionItemIcon.setImageResource(holder.item.iconRes)

        holder.binding.layoutHomeSectionItemTitle.text = holder.item.title
    }

    override fun getItemCount(): Int = sections.size
}