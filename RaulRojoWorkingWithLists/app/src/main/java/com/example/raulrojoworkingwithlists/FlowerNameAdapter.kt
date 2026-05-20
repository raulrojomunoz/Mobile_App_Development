package com.example.raulrojoworkingwithlists

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.raulrojoworkingwithlists.databinding.FlowerListItem1Binding

class FlowerNameAdapter : BaseAdapter {

    private lateinit var context: Context
    private lateinit var flowerNames: ArrayList<String>
    private lateinit var flowerImages: ArrayList<Int>

    constructor(
        context: Context,
        flowerNames: ArrayList<String>,
        flowerImages: ArrayList<Int>
    ) : super() {
        this.context = context
        this.flowerNames = flowerNames
        this.flowerImages = flowerImages
    }

    override fun getCount(): Int {
        return flowerNames.size
    }

    override fun getItem(position: Int): Any {
        return flowerNames[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        lateinit var binding: FlowerListItem1Binding

        val itemView = if (convertView == null) {
            binding = FlowerListItem1Binding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )

            binding.root.tag = binding
            binding.root
        } else {
            binding = convertView.tag as FlowerListItem1Binding
            convertView
        }

        binding.flowerName.text = getItem(position) as CharSequence?
        binding.flowerName.setTextColor(Color.BLACK)
        binding.flowerImage.setImageResource(flowerImages[position])

        if (position % 2 == 0) {
            binding.flowerName.setBackgroundColor(Color.rgb(153, 204, 0))
        } else {
            binding.flowerName.setBackgroundColor(Color.LTGRAY)
        }

        return itemView
    }
}