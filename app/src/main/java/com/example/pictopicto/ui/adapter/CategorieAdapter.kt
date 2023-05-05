package com.example.pictopicto.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pictopicto.databinding.FragmentItemBinding
import com.example.pictopicto.model.Categorie


class CategorieAdapter(
    private val values: ArrayList<Categorie>
) : RecyclerView.Adapter<CategorieAdapter.ViewHolder>() {
    var onItemClick: ((Categorie) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.itemView) {
            //donne les image en fonction d'un string
            holder.imageView.setImageResource(
                context.resources.getIdentifier(
                    item.categorieImgfile.replace(".png", "").lowercase(),
                    "drawable",
                    context.packageName
                )
            )

        }
    }

    override fun getItemCount(): Int {

        return values.size
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(values[adapterPosition])
            }
        }
    }

    //ajouter une image
    fun addItem(item: Categorie) {
        values.add(item)
        notifyItemInserted(itemCount - 1)
    }

    //enlever une image
    fun rmItem(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }
}

