package com.clockworks.incirkle.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppConstantsValue
{
    companion object
    {

        // path
        const val ASSIGNMENT_COLLECTION_PATH = "Assignment"
        const val ASSIGNMENT_STORAGE_PATH = "Assignments Attachments"

        const val SOLUTION_COLLECTION_PATH = "Solution"
        const val SOLUTION_STORAGE_PATH = "Solution Attachments"

        //  assignment their references

        val assignmentStorageRef = FirebaseStorage.getInstance().getReference(AppConstantsValue.ASSIGNMENT_STORAGE_PATH)

        val assignmentCollectionRef =
            FirebaseFirestore.getInstance().collection(AppConstantsValue.ASSIGNMENT_COLLECTION_PATH)

        val storageSolutionRef = FirebaseStorage.getInstance().getReference(AppConstantsValue.SOLUTION_STORAGE_PATH)


        val solutionCollectionRef =
            FirebaseFirestore.getInstance().collection(AppConstantsValue.SOLUTION_COLLECTION_PATH)


        // activity
        const val ACTIVITY_STORAGE_PATH = "Activity Attachments"
        val activityStorgageRef = FirebaseStorage.getInstance().getReference(ACTIVITY_STORAGE_PATH)


        // document
        const val DOCUMENT_STORAGE_PATH = "Document Attachments"
        val documentStorgagRef = FirebaseStorage.getInstance().getReference(DOCUMENT_STORAGE_PATH)

        // forum
        const val FORUM_STORAGE_PATH = "Forum Attachments"
        val forumStorageRef = FirebaseStorage.getInstance().getReference(FORUM_STORAGE_PATH)

        // comment
        const val COMMENT_STORAGE_PATH= "Comments Attachments"
        val commentStorageRef = FirebaseStorage.getInstance().getReference(COMMENT_STORAGE_PATH)


    }
}