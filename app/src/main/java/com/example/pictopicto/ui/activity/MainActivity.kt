package com.example.pictopicto.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.pictopicto.ItemMoveCallback
import com.example.pictopicto.MyDragListener
import com.example.pictopicto.RecyclerItemClickListener
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.databinding.ActivityMainBinding
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.repository.CategorieRepository
import com.example.pictopicto.repository.PictogrammeRepository
import com.example.pictopicto.ui.adapter.CategorieAdapter
import com.example.pictopicto.ui.adapter.PictoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var pictoAdapter: PictoAdapter
    lateinit var categorieAdapter: CategorieAdapter
    var pictogrammes = arrayListOf<Pictogramme>()
    var categories = arrayListOf<Categorie>()
    lateinit var categorieRepository: CategorieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categorieRepository = CategorieRepository.getInstance(application)!!
        categorieAdapter = CategorieAdapter(categories)


        categorieRepository.categories.observe(this) {
            println(it)
            if(it.isEmpty().not()) {
                categories.clear()
                categories.addAll(it)
                categorieAdapter.notifyDataSetChanged()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            categorieRepository.updateDatabase(this@MainActivity)

        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        pictoAdapter = PictoAdapter(ArrayList())
        pictoAdapter.clicklistener = true


        val callback: ItemTouchHelper.Callback = ItemMoveCallback(pictoAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recycler)

        binding.recycler.adapter = pictoAdapter
        binding.recycler.setOnDragListener(MyDragListener())

        binding.recycler2.adapter = PictoAdapter(pictogrammes)


        binding.recycler3.adapter = categorieAdapter
        binding.recycler3.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                binding.recycler3,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        pictogrammes.clear()
                        pictogrammes.addAll(categories[position].pictogrammes)
                        binding.recycler2.adapter = PictoAdapter(pictogrammes)
                        pictoAdapter.notifyDataSetChanged()
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        TODO("do nothing")
                    }
                })
        )


    }

}