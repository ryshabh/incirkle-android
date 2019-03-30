package com.clockworks.incirkle.Fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clockworks.incirkle.Models.AssignmentPost
import com.clockworks.incirkle.R


class AssignmentFragment : Fragment()
{
    private lateinit var coursePath: String
    private lateinit var teacherPath: String
    private var isTeacher: Boolean = false
    private var isTeachingAssistant: Boolean = false

    private var assignmentList = ArrayList<AssignmentPost>()
    private lateinit var rootView:View
    private val TAG = AssignmentFragment::class.java.simpleName

    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coursePath = it.getString(IDENTIFIER_COURSE_PATH)
            teacherPath = it.getString(IDENTIFIER_COURSE_TEACHER_PATH)
            isTeacher = it.getBoolean(IDENTIFIER_IS_TEACHER)
            isTeachingAssistant = it.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
         rootView = inflater.inflate(R.layout.fragment_assignment, container, false)

        initialise()

        return rootView
    }

    private fun initialise()
    {
        // setAdapter
    }


    override fun onAttach(context: Context)
    {
        super.onAttach(context)

    }


}
