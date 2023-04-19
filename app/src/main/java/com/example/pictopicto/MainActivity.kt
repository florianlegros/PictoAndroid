package com.example.pictopicto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.pictopicto.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var adapter: MyItemRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MyItemRecyclerViewAdapter(ArrayList())
        adapter.clicklistener = true
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recycler)

        binding.recycler.adapter = adapter
        binding.recycler.setOnDragListener(MyDragListener())
        binding.recycler2.adapter = MyItemRecyclerViewAdapter(arrayListOf("cereales","dessert","gateaux","glace","riz","beurre","chocolat"))


    }
}