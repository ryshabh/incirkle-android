package com.clockworks.incirkle.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class DetailedListAdapter(private val context: Context, private var dataSource: List<Pair<String, String>>) : BaseAdapter()
{
    private class ViewModel
    {
        lateinit var text1: TextView
        lateinit var text2: TextView
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int
    {
        return this.dataSource.size
    }

    override fun getItem(p0: Int): Any
    {
        return this.dataSource[p0]
    }

    override fun getItemId(p0: Int): Long
    {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val view: View
        val viewModel: ViewModel

        if (convertView == null)
        {
            view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
            viewModel = ViewModel()
            viewModel.text1 = view.findViewById<TextView>(android.R.id.text1)
            viewModel.text2 = view.findViewById<TextView>(android.R.id.text2)
            view.tag = viewModel
        }
        else
        {
            view = convertView
            viewModel = convertView.tag as ViewModel
        }

        val tuple = this.getItem(position) as Pair<String, String>
        viewModel.text1.setText(tuple.first)
        viewModel.text2.setText(tuple.second)

        return view
    }
}