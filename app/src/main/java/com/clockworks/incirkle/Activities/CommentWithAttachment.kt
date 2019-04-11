package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.clockworks.incirkle.Models.Comment
import com.clockworks.incirkle.Models.documentReference
import com.clockworks.incirkle.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_comment_with_attachment.*

class CommentWithAttachment : AppActivity()
{
    private lateinit var fileName: String
    private lateinit var fileUri: Uri
    private lateinit var commentsReference: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_with_attachment)

        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        fileName = intent.getStringExtra("fileName");
        fileUri = intent.getParcelableExtra("fileUri")

        this.commentsReference =
            FirebaseFirestore.getInstance().document(intent.getStringExtra("Post Path")).collection("Comments")


        tv_attachment_name.text = fileName



        btnPost.setOnClickListener() {

            var description = etComment.text.toString().trim()
            if (description.isBlank())
            {
//                etComment.error = " description cannot be empty"
//                return@setOnClickListener
                description = "";
            }
            val intent = Intent()
            intent.putExtra("description",description)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }


    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }

}
