package com.clockworks.incirkle.Activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.clockworks.incirkle.Adapters.SolutionAdapter
import com.clockworks.incirkle.Models.SolutionModel
import com.clockworks.incirkle.Models.User
import com.clockworks.incirkle.R
import com.clockworks.incirkle.utils.AppConstantsValue
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_submission.*

class SubmissionActivity : AppActivity()
{
    private lateinit var solutionAdapter: SolutionAdapter
    private var solutionList = ArrayList<SolutionModel>()
    private lateinit var listenerRegistration: ListenerRegistration

    private val TAG = SubmissionActivity::class.java.simpleName

    private lateinit var assignmentId: String

    companion object
    {
        const val ASSIGNMENT_ID = "ASSIGNMENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)
        toolbar.setTitle(getString(R.string.assignment_submisstion_list))
        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)


        assignmentId = intent.getStringExtra(ASSIGNMENT_ID)

        rvSubmission.layoutManager = LinearLayoutManager(this)
        solutionAdapter = SolutionAdapter(solutionList)
        rvSubmission.adapter = solutionAdapter
    }

    override fun onStart()
    {
        super.onStart()
        solutionList.clear()
        solutionAdapter.notifyDataSetChanged()
        this.showLoadingAlert()
        listenerRegistration =
            AppConstantsValue.solutionCollectionRef.whereEqualTo("assignmentDocumentId", assignmentId)
                .addSnapshotListener { querySnapshot, fFE ->
                    this.dismissLoadingAlert()
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

                }
    }

    private fun onDocumentAdded(change: DocumentChange)
    {
        var solutionModel = change.document.toObject(SolutionModel::class.java)
        solutionModel.studentSubmitter?.get()?.addOnSuccessListener { document ->
            if (document != null)
            {
                try
                {
                    Log.d("", "DocumentSnapshot data: ${document.data}")
                    solutionModel.documentId = change.document.id
                    solutionModel.user = document.toObject(User::class.java)
                    solutionList.add(change.newIndex, solutionModel)
                    solutionAdapter.notifyItemInserted(change.newIndex)
                } catch (e: Exception)
                {
                    e.printStackTrace()
                }
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
            solutionList[change.oldIndex] = change.document.toObject(SolutionModel::class.java)
            solutionAdapter.notifyItemChanged(change.oldIndex)
        }
        else
        {
            // Item changed and changed position
            solutionList.removeAt(change.oldIndex)
            solutionList.add(change.newIndex, change.document.toObject(SolutionModel::class.java))
            solutionAdapter.notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange)
    {
        solutionList.removeAt(change.oldIndex)
        solutionAdapter.notifyItemRemoved(change.oldIndex)
    }

    override fun onStop()
    {
        super.onStop()
        listenerRegistration.remove()
    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }
}
