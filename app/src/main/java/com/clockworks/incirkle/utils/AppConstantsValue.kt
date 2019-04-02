package com.clockworks.incirkle.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppConstantsValue{
    companion object
    {

        // path
        const val ASSIGNMENT_COLLECTION_PATH = "Assignment"
        const val ASSIGNMENT_STORAGE_PATH = "Assignments Attachments"

        const val SOLUTION_COLLECTION_PATH = "Solution"
        const val SOLUTION_STORAGE_PATH = "Solution Attachments"

        // their references

        val storageRef = FirebaseStorage.getInstance().getReference(AppConstantsValue.ASSIGNMENT_STORAGE_PATH)
        .child(System.currentTimeMillis().toString())

        val assignmentCollectionRef =
        FirebaseFirestore.getInstance().collection(AppConstantsValue.ASSIGNMENT_COLLECTION_PATH)

         val storageSolutionRef = FirebaseStorage.getInstance().getReference(AppConstantsValue.SOLUTION_STORAGE_PATH)
        .child(System.currentTimeMillis().toString())

        val solutionCollectionRef =
        FirebaseFirestore.getInstance().collection(AppConstantsValue.SOLUTION_COLLECTION_PATH)
    }
}