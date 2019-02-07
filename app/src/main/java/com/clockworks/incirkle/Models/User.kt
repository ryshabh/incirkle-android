package com.clockworks.incirkle.Models

import android.util.Patterns
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlin.collections.ArrayList

class User()
{
    enum class Type { TEACHER, STUDENT; }
    enum class Gender { MALE, FEMALE; }

    companion object
    {
        fun collectionReference(): CollectionReference
        {
            return FirebaseFirestore.getInstance().collection("Users")
        }

        fun iterate(userIDs: ArrayList<String>, onIteration: (Int, String, Task<QuerySnapshot>) -> Unit)
        {
            for (index in 0 until userIDs.size)
            {
                val userID = userIDs[index]
                (if (Patterns.PHONE.matcher(userID).matches()) "phoneNumber"
                else if (Patterns.PHONE.matcher(userID).matches()) "emailAddress"
                else null)?.let()
                {
                    key ->
                    User.collectionReference().whereEqualTo(key, userID).get()
                        .addOnCompleteListener() { onIteration(index, key, it) }
                }
            }
        }
    }

    lateinit var uid: String
    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var gender: Gender
    var emailAddress: String? = null
    var phoneNumber: String? = null
    lateinit var type: Type
    var courses = ArrayList<DocumentReference>()

    @Exclude
    var documentReference: DocumentReference? = null

    constructor(uid: String, firstName: String, lastName: String, gender: Gender, emailAddress: String?, phoneNumber: String?, type: Type): this()
    {
        this.uid = uid
        this.firstName = firstName
        this.lastName = lastName
        this.gender = gender
        this.emailAddress = emailAddress
        this.phoneNumber = phoneNumber
        this.type = type
    }

    fun fullName() : String
    {
        return "$firstName $lastName"
    }

    fun userID() : String
    {
        return this.emailAddress ?: this.phoneNumber ?: "User ID"
    }

    fun update(completion: (Exception?) -> Unit)
    {
        this.documentReference?.let()
        {
            reference ->
            reference.set(this).addOnCompleteListener()
            {
                completion(it.exception)
            }
        }
        ?: run()
        {
            User.collectionReference().add(this@User).addOnCompleteListener()
            {
                completion(it.exception)
            }
        }
    }
}

fun FirebaseUser.currentUserData(callback: (User?, Exception?) -> Unit)
{
    User.Companion.collectionReference()
        .whereEqualTo("uid", this.uid)
        .get().addOnCompleteListener()
        { task ->

            if (task.isSuccessful)
            {
                task.result?.let()
                { snapshot ->

                    if (snapshot.isEmpty)
                        callback(null, null)
                    else
                    {
                        val document = snapshot.documents.first()
                        document.toObject(User::class.java)?.let()
                        {
                            it.documentReference = document.reference
                            callback(it, null)
                        } ?: run()
                        {
                            callback(null, Exception("Could not deserialize User data"))
                        }
                    }
                }
            }
            else
                callback(null, task.exception)
        }
}