package com.clockworks.incirkle.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.clockworks.incirkle.R

class DetailedListAdapter(private val context: Context, private var dataSource: List<Pair<String, String>>, private val deleteListener: DeleteListener? = null) : BaseAdapter()
{
    interface DeleteListener
    {
        fun onItemDelete(position: Int)
    }

    private class ViewModel
    {
        lateinit var topTextView: TextView
        lateinit var bottomTextView: TextView
        lateinit var deleteButton: ImageButton
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
            view = inflater.inflate(R.layout.list_item_detailed_deletable, parent, false)
            viewModel = ViewModel()
            viewModel.topTextView = view.findViewById<TextView>(R.id.textView_top)
            viewModel.bottomTextView = view.findViewById<TextView>(R.id.textView_bottom)
            viewModel.deleteButton = view.findViewById<ImageButton>(R.id.button_delete)
            view.tag = viewModel
        }
        else
        {
            view = convertView
            viewModel = convertView.tag as ViewModel
        }

        val tuple = this.getItem(position) as Pair<String, String>
        viewModel.topTextView.setText(tuple.first)
        viewModel.bottomTextView.setText(tuple.second)
        viewModel.deleteButton.visibility = if (this.deleteListener == null) View.GONE else View.VISIBLE
        viewModel.deleteButton.setOnClickListener() { this.deleteListener?.onItemDelete(position) }

        return view
    }
}