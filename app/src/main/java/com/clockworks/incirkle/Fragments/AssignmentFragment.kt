package com.clockworks.incirkle.Fragments

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.clockworks.incirkle.Activities.AppActivity
import com.clockworks.incirkle.Adapters.AssignmentAdapter
import com.clockworks.incirkle.Models.AssignmentModel
import com.clockworks.incirkle.Models.SolutionModel
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.clockworks.incirkle.filePicker.KotConstants
import com.clockworks.incirkle.filePicker.KotConstants.Companion.REQUEST_FILE
import com.clockworks.incirkle.filePicker.KotConstants.Companion.REQUEST_FILE_FOR_SOLUTION
import com.clockworks.incirkle.filePicker.KotRequest
import com.clockworks.incirkle.filePicker.KotResult
import com.clockworks.incirkle.utils.AppConstantsValue
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_assignment.view.*
import kotlinx.android.synthetic.main.popup_add_assigment.view.*
import java.util.*


class AssignmentFragment : Fragment()
{
    private lateinit var coursePath: String
    private lateinit var teacherPath: String
    private var isTeacher: Boolean = false
    private var isTeachingAssistant: Boolean = false

    private var assignmentList = ArrayList<AssignmentModel>()
    private lateinit var rootView: View
    private val TAG = AssignmentFragment::class.java.simpleName

    private val calendar = Calendar.getInstance()
    private var attachmentResult: KotResult? = null

    private lateinit var courseDocumentId:String
    private var currentAssignmentDocumentId: String? = null

    private lateinit var popupViewLayout: View
    private lateinit var dialog: Dialog

    private lateinit var appActivity: AppActivity


    private lateinit var assignmentAdapter: AssignmentAdapter

    private lateinit var listenerRegistration: ListenerRegistration

    private var isSubmitAttachment:Boolean = false

    companion object
    {
        const val IDENTIFIER_COURSE_PATH = "Course Path"
        const val IDENTIFIER_IS_TEACHER = "Is Teacher"
        const val IDENTIFIER_IS_TEACHING_ASSISTANT = "Is Teaching Assistant"
        const val IDENTIFIER_COURSE_TEACHER_PATH = "Course Teacher Path"
        const val IDENTIFIER_COURSE_DOCUMENT_ID= "Course Document Id"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        arguments?.let {
            coursePath = it.getString(IDENTIFIER_COURSE_PATH)
            teacherPath = it.getString(IDENTIFIER_COURSE_TEACHER_PATH)
            isTeacher = it.getBoolean(IDENTIFIER_IS_TEACHER)
            isTeachingAssistant = it.getBoolean(IDENTIFIER_IS_TEACHING_ASSISTANT)
            courseDocumentId = it.getString(IDENTIFIER_COURSE_DOCUMENT_ID)
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
        FirebaseAuth.getInstance().currentUser?.let()
        {

            FirebaseStorage.getInstance().getReference("UserProfiles").child(it.uid).downloadUrl.addOnSuccessListener {
                var request: RequestOptions =
                    RequestOptions().error(R.drawable.ic_user).override(100, 100).placeholder(R.drawable.ic_user)
                Glide.with(activity)
                    .load(it.toString())
                    .apply(request)
                    .into(rootView.imageview_profileimage);
            }.addOnFailureListener {
                it.printStackTrace()
            };
        }

        rootView.rvAssignment.layoutManager = LinearLayoutManager(appActivity)
        assignmentAdapter = AssignmentAdapter(assignmentList, isTeacher, isTeachingAssistant, this)
        rootView.rvAssignment.adapter = assignmentAdapter


        rootView.cvCreateAssignment.visibility = if (isTeacher || isTeachingAssistant) View.VISIBLE else View.GONE
        rootView.cvCreateAssignment.setOnClickListener {
            popupViewLayout = activity!!.layoutInflater.inflate(R.layout.popup_add_assigment, null)

            popupViewLayout.button_post_assignment_dueDate.text =
                getDateFormat(activity).format(this.calendar.time)

            popupViewLayout.button_post_assignment_dueDate.setOnClickListener()
            {
                val listener = DatePickerDialog.OnDateSetListener()
                { _, year, month, day ->
                    this.calendar.set(Calendar.YEAR, year)
                    this.calendar.set(Calendar.MONTH, month)
                    this.calendar.set(Calendar.DAY_OF_MONTH, day)
                    popupViewLayout.button_post_assignment_dueDate.text =
                        getDateFormat(activity).format(this.calendar.time)
                }
                val year = this.calendar.get(Calendar.YEAR)
                val month = this.calendar.get(Calendar.MONTH)
                val day = this.calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(activity, listener, year, month, day).show()
            }

            popupViewLayout.button_post_assignment_dueTime.text =
                getTimeFormat(activity).format(this.calendar.time)
            popupViewLayout.button_post_assignment_dueTime.setOnClickListener()
            {
                val listener = TimePickerDialog.OnTimeSetListener()
                { _, hour, minute ->
                    this.calendar.set(Calendar.HOUR_OF_DAY, hour)
                    this.calendar.set(Calendar.MINUTE, minute)
                    popupViewLayout.button_post_assignment_dueTime.text =
                        android.text.format.DateFormat.getTimeFormat(activity).format(this.calendar.time)
                }
                val hour = this.calendar.get(Calendar.HOUR_OF_DAY)
                val minute = this.calendar.get(Calendar.MINUTE)
                TimePickerDialog(activity, listener, hour, minute, false).show()
            }

            popupViewLayout.button_assignment_selectAttachment.setOnClickListener {
                attachmentResult = null
                KotRequest.File(this, REQUEST_FILE)
                    .isMultiple(false)
                    .setMimeType(KotConstants.FILE_TYPE_FILE_ALL)
                    .pick()

            }

            popupViewLayout.button_post_assignment.setOnClickListener()
            {
                popupViewLayout.editText_post_assignment_name.error = null
                popupViewLayout.editText_post_assignment_details.error = null

                val name = popupViewLayout.editText_post_assignment_name.text.toString().trim()
                val details = popupViewLayout.editText_post_assignment_details.text.toString().trim()

                if (name.isBlank())
                {
                    popupViewLayout.editText_post_assignment_name.error = "Assignment name cannot be empty"
                    return@setOnClickListener
                }
                else if (details.isBlank())
                {
                    popupViewLayout.editText_post_assignment_details.error = "Assignment description cannot be empty"
                    return@setOnClickListener
                }
                else if (attachmentResult == null)
                {
                    popupViewLayout.button_assignment_selectAttachment.error = "Select Attachment"
                    return@setOnClickListener
                }
                // upload attachment first post it
                dialog.dismiss()

                appActivity.showLoadingAlert()
                AppConstantsValue.storageRef.putFile(attachmentResult!!.uri).addOnCompleteListener(OnCompleteListener {
                    if (it.isSuccessful)
                    {
                        AppConstantsValue.storageRef.downloadUrl.addOnSuccessListener {

                            // file uploaded successfully
                            val fileUrl = it.toString()
                            val assignmentModel = AssignmentModel()
                            assignmentModel.courseDocumentId = courseDocumentId
                            assignmentModel.name = name
                            assignmentModel.detail = details
                            assignmentModel.dueDate = Timestamp(this.calendar.time)
                            assignmentModel.assignmentAttachmentUrl = fileUrl
                            assignmentModel.assignmentAttachmentFileType = attachmentResult!!.type
                            assignmentModel.attachmentCreator =
                                FirebaseAuth.getInstance().currentUser!!.documentReference()
                            AppConstantsValue.assignmentCollectionRef.add(assignmentModel).addOnCompleteListener {
                                appActivity.dismissLoadingAlert()
                                if (it.isSuccessful)
                                {
//                                    Toast.makeText(activity, it.result?.path, Toast.LENGTH_LONG).show()
                                }
                                else
                                {
                                    Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                                }
                            }

                        }.addOnFailureListener {
                            Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                        }

                    }
                    else
                    {
                        Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                })


            }

            dialog = AlertDialog.Builder(activity)
                .setView(popupViewLayout)
                .setCancelable(true)
                .create()

            dialog.show()

        }


    }


    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        appActivity = activity as AppActivity

    }


    override fun onStart()
    {
        super.onStart()
        assignmentList.clear()
        assignmentAdapter.notifyDataSetChanged()
        appActivity.showLoadingAlert()
        listenerRegistration = AppConstantsValue.assignmentCollectionRef.whereEqualTo("courseDocumentId",courseDocumentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, fFE ->
                appActivity.dismissLoadingAlert()
                if (fFE != null)
                {
                    Log.w(TAG, "onEvent:error", fFE)
                    return@addSnapshotListener
                }

                if (querySnapshot == null)
                {
                    return@addSnapshotListener
                }

                // Dispatch the event
                Log.d(TAG, "onEvent:numChanges:" + querySnapshot.documentChanges.size)

                for (change in querySnapshot.documentChanges)
                {
                    when (change.type)
                    {
                        DocumentChange.Type.ADDED -> onDocumentAdded(change)
                        DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                        DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                    }
                }

                if(isSubmitAttachment){
                    uploadSolution()
                    isSubmitAttachment = false
                }

            }
    }

    private fun onDocumentAdded(change: DocumentChange)
    {
        val assignmentModel = change.document.toObject(AssignmentModel::class.java)
        assignmentModel.attachmentCreator?.get()?.addOnSuccessListener { document ->
            if (document != null)
            {
                Log.d("", "DocumentSnapshot data: ${document.data}")
                assignmentModel.documentId = change.document.id
                assignmentModel.user = document.toObject(User::class.java)
                assignmentList.add(change.newIndex, assignmentModel)
                assignmentAdapter.notifyItemInserted(change.newIndex)
                rootView.rvAssignment.scrollToPosition(0)
            }
            else
            {
                Log.d("", "No such document")
            }
        }

    }

    private fun onDocumentModified(change: DocumentChange)
    {
        if (change.oldIndex == change.newIndex)
        {
            // Item changed but remained in same position
            assignmentList[change.oldIndex] = change.document.toObject(AssignmentModel::class.java)
            assignmentAdapter.notifyItemChanged(change.oldIndex)
        }
        else
        {
            // Item changed and changed position
            assignmentList.removeAt(change.oldIndex)
            assignmentList.add(change.newIndex, change.document.toObject(AssignmentModel::class.java))
            assignmentAdapter.notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange)
    {
        assignmentList.removeAt(change.oldIndex)
        assignmentAdapter.notifyItemRemoved(change.oldIndex)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK)
        {
            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            attachmentResult = result!!.get(0)
            if (attachmentResult != null)
            {
                popupViewLayout.button_assignment_selectAttachment.text = attachmentResult!!.name
            }
        }
        else if (requestCode == REQUEST_FILE_FOR_SOLUTION && resultCode == Activity.RESULT_OK)
        {
            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            attachmentResult = result!!.get(0)
            if (attachmentResult != null)
            {
                isSubmitAttachment = true
            }
        }
    }

    fun deleteItem(documentId: String)
    {
        AppConstantsValue.assignmentCollectionRef.document(documentId)
            .delete().addOnSuccessListener {
                Toast.makeText(activity, "deleted", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener() {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    fun openFileLocation(documentId: String)
    {
        attachmentResult = null
        currentAssignmentDocumentId = documentId
        KotRequest.File(this, REQUEST_FILE_FOR_SOLUTION)
            .isMultiple(false)
            .setMimeType(KotConstants.FILE_TYPE_FILE_ALL)
            .pick()
    }

    fun uploadSolution()
    {
        appActivity.showLoadingAlert()
        AppConstantsValue.storageSolutionRef.putFile(attachmentResult!!.uri).addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful)
            {
                AppConstantsValue.storageSolutionRef.downloadUrl.addOnSuccessListener {
                    // file uploaded successfully
                    val fileUrl = it.toString()
                    val solutionModel = SolutionModel(currentAssignmentDocumentId,fileUrl,attachmentResult!!.type,FirebaseAuth.getInstance().currentUser!!.documentReference())

                    AppConstantsValue.solutionCollectionRef.add(solutionModel).addOnSuccessListener {
                        appActivity.dismissLoadingAlert()

                        // update in list also
                         updateParticularPost()

                        Toast.makeText(appActivity, getString(R.string.solution_uploaded), Toast.LENGTH_LONG).show()
                    }.addOnFailureListener{
                        appActivity.dismissLoadingAlert()
                            Toast.makeText(appActivity, it.message, Toast.LENGTH_LONG).show()
                    }

                }.addOnFailureListener {
                    appActivity.dismissLoadingAlert()
                    Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                }

            }
            else
            {
                Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        })


    }

    private fun updateParticularPost()
    {
        AppConstantsValue.assignmentCollectionRef.document(currentAssignmentDocumentId.toString()).update("documentId",currentAssignmentDocumentId)

    }


    override fun onStop()
    {
        super.onStop()
        listenerRegistration.remove()
    }


}
