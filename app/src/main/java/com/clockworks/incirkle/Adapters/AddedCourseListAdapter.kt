package com.clockworks.incirkle.Adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage

class AddedCourseListAdapter(private val context: Context, private var dataSource: ArrayList<Course>) : BaseAdapter()
{
    private class ViewModel
    {
        lateinit var codeTextView: TextView
        lateinit var nameTextView: TextView
        lateinit var lastestUpdateTextView: TextView
        lateinit var actcount: TextView
        lateinit var assngcount: TextView
        lateinit var forumcount: TextView
        lateinit var doccount: TextView
        lateinit var ivRemoveCourse: ImageView

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
        val courseView: ViewModel

        if (convertView == null)
        {
            view = inflater.inflate(R.layout.list_item_added_course, parent, false)
            courseView = ViewModel()
            courseView.codeTextView = view.findViewById<TextView>(R.id.textView_course_code)
            courseView.nameTextView = view.findViewById<TextView>(R.id.textView_course_name)
            courseView.lastestUpdateTextView = view.findViewById<TextView>(R.id.textView_latest_update)
            courseView.doccount = view.findViewById<TextView>(R.id.doccount)
            courseView.assngcount = view.findViewById<TextView>(R.id.assngcount)
            courseView.forumcount = view.findViewById<TextView>(R.id.forumcount)
            courseView.actcount = view.findViewById<TextView>(R.id.actcount)
            courseView.ivRemoveCourse = view.findViewById<ImageView>(R.id.ivRemoveCourse)
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
        if (course.documentpostsize.toString().trim().equals("0"))
        {
            courseView.doccount.isEnabled = false
            courseView.doccount.text = "Doc"
        }
        else
        {
            courseView.doccount.isEnabled = true
            courseView.doccount.text = "Doc - " + course.documentpostsize.toString()
        }
        if (course.assignmentpostsize.toString().trim().equals("0"))
        {
            courseView.assngcount.isEnabled = false;
            courseView.assngcount.text = "Assng "
        }
        else
        {
            courseView.assngcount.isEnabled = true;
            courseView.assngcount.text = "Assng - " + course.assignmentpostsize.toString()
        }
        if (course.forumpostsize.toString().trim().equals("0"))
        {
            courseView.forumcount.isEnabled = false
            courseView.forumcount.text = "Forum"
        }
        else
        {
            courseView.forumcount.isEnabled = true;
            courseView.forumcount.text = "Forum - " + course.forumpostsize.toString()
        }


        if (course.activitypostsize.toString().trim().equals("0"))
        {
            courseView.actcount.isEnabled = false
            courseView.actcount.text = "Act"
        }
        else
        {
            courseView.actcount.isEnabled = true;
            courseView.actcount.text = "Act - " + course.activitypostsize.toString()
        }


        courseView.ivRemoveCourse.setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(v: View?)
            {
                val popup = PopupMenu(context, courseView.ivRemoveCourse)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.actions_leave, popup.menu)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Leave the course")
                    builder.setMessage("Are you sure you wish to leave this course?")
                    builder.setPositiveButton("Leave",
                        { _, _ ->
                            // course leaved
                            FirebaseAuth.getInstance().currentUser?.documentReference()?.update("courses", FieldValue.arrayRemove(course.reference))
                                ?.addOnFailureListener {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                                ?.addOnSuccessListener()
                                {
                                    Toast.makeText(context, "Course Leaved", Toast.LENGTH_LONG).show()
                                }
                        })
                    builder.setNegativeButton("Cancel", null)
                    builder.create().show()

                    true
                })
                popup.show()
            }
        })


        return view
    }
}