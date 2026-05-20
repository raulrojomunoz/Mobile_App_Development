package com.example.raulrojoworkingwithlists

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.raulrojoworkingwithlists.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {

    private val listOfFlowers = ArrayList<String>()
    private val listOfFlowerImages = ArrayList<Int>()

    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFlowerList()
        initFlowerImagesList()

//        var adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_expandable_list_item_1,
//            listOfFlowers
//        )
//
//        binding.flowerList.adapter = adapter

        var flowerAdapter = FlowerNameAdapter(
            this,
            listOfFlowers,
            listOfFlowerImages
        )

        binding.flowerList.adapter = flowerAdapter
    }

    private fun initFlowerList() {
        listOfFlowers.add("Rose")
        listOfFlowers.add("Tulip")
        listOfFlowers.add("Daisy")
    }

    private fun initFlowerImagesList() {
        listOfFlowerImages.add(R.drawable.rose)
        listOfFlowerImages.add(R.drawable.tulip)
        listOfFlowerImages.add(R.drawable.daisy)
    }
}

