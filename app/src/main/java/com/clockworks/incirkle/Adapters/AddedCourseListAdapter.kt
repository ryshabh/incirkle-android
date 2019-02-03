package com.clockworks.incirkle.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.R

class AddedCourseListAdapter(private val context: Context, private var dataSource: ArrayList<Course>) : BaseAdapter()
{
    private class ViewModel
    {
        lateinit var codeTextView: TextView
        lateinit var nameTextView: TextView
        lateinit var lastestUpdateTextView: TextView
    }
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
        val courseView: ViewModel

        if (convertView == null)
        {
            view = inflater.inflate(R.layout.list_item_added_course, parent, false)
            courseView = ViewModel()
            courseView.codeTextView = view.findViewById<TextView>(R.id.textView_course_code)
            courseView.nameTextView= view.findViewById<TextView>(R.id.textView_course_name)
            courseView.lastestUpdateTextView = view.findViewById<TextView>(R.id.textView_latest_update)
            view.tag = courseView
        }
        else
        {
            view = convertView
            courseView = convertView.tag as ViewModel
        }

        val course = this.getItem(position) as Course
        courseView.codeTextView.setText(course.code)
        courseView.nameTextView.setText(course.name)

        return view
    }
}