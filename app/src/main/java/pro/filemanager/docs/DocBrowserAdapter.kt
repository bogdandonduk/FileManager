package pro.filemanager.docs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.R
import pro.filemanager.databinding.LayoutDocItemBinding
import pro.filemanager.files.FileCore

class DocBrowserAdapter(val context: Context, val docItems: MutableList<DocItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<DocBrowserAdapter.DocItemViewHolder>() {

    class DocItemViewHolder(val context: Context, val binding: LayoutDocItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: DocItem

        init {
            binding.layoutDocItemRootLayout.setOnClickListener {
                FileCore.openFileOut(context, item.data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocItemViewHolder {
        return DocItemViewHolder(context, DataBindingUtil.inflate(layoutInflater, R.layout.layout_doc_item, parent, false))
    }

    override fun onBindViewHolder(holder: DocItemViewHolder, position: Int) {

        holder.item = docItems[position]
        holder.binding.docItem = holder.item
    }

    override fun getItemCount(): Int {
        return docItems.size
    }
}