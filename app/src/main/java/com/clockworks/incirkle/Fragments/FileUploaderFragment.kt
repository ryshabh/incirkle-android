package com.clockworks.incirkle.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.support.v4.app.Fragment
import com.clockworks.incirkle.Activities.AppActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import android.provider.OpenableColumns
import android.util.Log


fun Uri.getName(context: Context): String
{
    var result: String? = null
    if (this.getScheme().equals("content"))
    {
        val cursor = context.getContentResolver().query(this, null, null, null, null)
        try
        {
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } finally
        {
            cursor!!.close()
        }
    }
    if (result == null)
    {
        result = this.getPath()
        val cut = result!!.lastIndexOf('/')
        if (cut != -1)
            result = result.substring(cut + 1)
    }
    return result
}

abstract class FileUploaderFragment : Fragment()
{
    private val PICK_FILE_REQUEST = 1

    var selectedFileUri: Uri? = null
        private set
    private var didSelectFile: () -> Unit = { }

    fun selectFile(onSelection: () -> Unit)
    {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.flags = FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "*/*"
        (this.activity as? AppActivity)?.performThrowable {
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Select a File to Upload"
                ), PICK_FILE_REQUEST
            )
        }
        this.didSelectFile = onSelection
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        Log.d("FileUploader", " " + requestCode + " " + resultCode + " " + data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            this.selectedFileUri = data?.dataString?.let { Uri.parse(it) }
            this.didSelectFile()
        }
    }

    fun updateAttachmentPath(
        documentReference: DocumentReference,
        fileReference: StorageReference,
        fieldPath: String,
        onSuccess: () -> Unit = { }
    )
    {
        val appActivity = activity as AppActivity
        this.selectedFileUri?.let()
        {
            this.selectedFileUri = null
            this.didSelectFile()

            appActivity.showLoadingAlert()

            try
            {
                fileReference.putFile(it).continueWith()
                {
                    if (!it.isSuccessful)
                        throw it.exception!!
                    return@continueWith fileReference.downloadUrl
                }
                    .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                    .addOnFailureListener()
                    {
                        appActivity.showError(it)
                        appActivity.showLoadingAlert()
                        documentReference.delete()
                            .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                    }
                    .addOnSuccessListener()
                    {
                        appActivity.showLoadingAlert()
                        it.addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            .addOnFailureListener()
                            {
                                appActivity.showError(it)
                            }
                            .addOnSuccessListener()
                            {
                                appActivity.showLoadingAlert()
                                documentReference.update(fieldPath, it.toString())
                                    .addOnFailureListener { appActivity.showError(it) }
                                    .addOnCompleteListener { appActivity.dismissLoadingAlert() }
                            }
                    }
            } catch (e: Exception)
            {
                appActivity.dismissLoadingAlert()
                appActivity.showError(e)
            }
        }
    }
}