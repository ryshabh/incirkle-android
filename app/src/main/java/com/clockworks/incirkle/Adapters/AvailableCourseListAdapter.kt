package com.clockworks.incirkle.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.clockworks.incirkle.Interfaces.serialize
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.R

class AvailableCourseListAdapter(context: Context, private var dataSource: ArrayList<Course>) : BaseAdapter()
{
    private class ViewModel
    {
        lateinit var courseCode: TextView
        lateinit var courseName: TextView
        lateinit var teacherName: TextView
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
        val viewModel: ViewModel

        if (convertView == null)
        {
            view = inflater.inflate(R.layout.list_item_available_course, parent, false)
            viewModel = ViewModel()
            viewModel.courseCode = view.findViewById<TextView>(R.id.textView_courseCode)
            viewModel.courseName = view.findViewById<TextView>(R.id.textView_courseName)
            viewModel.teacherName = view.findViewById<TextView>(R.id.textView_teacherName)
            view.tag = viewModel
        }
        else
        {
            view = convertView
            viewModel = convertView.tag as ViewModel
        }

        val course = this.getItem(position) as Course
        viewModel.courseCode.setText(course.code)
        viewModel.courseName.setText(course.name)
        course.teacher.addSnapshotListener()
        {
            task, e ->
            viewModel.teacherName.error = e?.localizedMessage
            viewModel.teacherName.text = task?.serialize(User::class.java)?.fullName() ?: ""
        }

        return view
    }
}