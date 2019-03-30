package com.clockworks.incirkle.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.clockworks.incirkle.filePicker.KotConstants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import android.R.attr.data
import android.R



abstract class AppActivity : AppCompatActivity()
{
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

    private var currentAlert: AlertDialog? = null
    private var progressDialogCount = 0

    private fun rootView(): View
    {
        return this.findViewById<View>(android.R.id.content)
    }

    fun <T> performThrowable(block: () -> T): T?
    {
        var result: T? = null
        try
        {
            result = block()
        } catch (e: Exception)
        {
            this.showError(e)
        }
        return result
    }

    fun showError(exception: Exception)
    {
        exception.printStackTrace()
        //  Snackbar.make(this.rootView(), exception.localizedMessage, Snackbar.LENGTH_INDEFINITE)
        //    .setAction("Dismiss") { }.show()
    }

    fun showLoadingAlert()
    {
        if (this.currentAlert != null)
            return

        ++this.progressDialogCount

        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        this.currentAlert = SpotsDialog.Builder().setContext(this)
            .setCancelable(false).build()
        this.currentAlert?.show()
    }

    fun dismissLoadingAlert()
    {
        if (this.currentAlert == null)
            return

        --this.progressDialogCount

        if (this.progressDialogCount != 0)
            return

        this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        this.currentAlert?.dismiss()
        this.currentAlert = null
    }

    override fun onPause()
    {
        super.onPause()
        this.dismissLoadingAlert()
    }


    private val PICK_FILE_REQUEST = 1

    var selectedFileUri: Uri? = null
    var changedUrl: String? = null
        private set
    private var didSelectFile: () -> Unit = { }

    fun selectFile(onSelection: () -> Unit)
    {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "*/*"
        performThrowable {
            startActivityForResult(
                Intent.createChooser(intent, "Select a File to Upload"),
                PICK_FILE_REQUEST
            )
        }
        this.didSelectFile = onSelection
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
//        Log.d("AppActivity", " " + requestCode + " " + resultCode + " " + data);
//        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            this.selectedFileUri = data?.dataString?.let { Uri.parse(it) }
            this.didSelectFile()
        }
    }

    fun updateAttachmentPath(
        documentReference: DocumentReference,
        fileReference: StorageReference,
        fieldPath: String
    )
    {
        this.selectedFileUri?.let()
        {
            this.selectedFileUri = null
            this.didSelectFile()

            showLoadingAlert()

            try
            {
                fileReference.putFile(it).continueWith()
                {
                    if (!it.isSuccessful)
                        throw it.exception!!
                    return@continueWith fileReference.downloadUrl
                }
                    .addOnCompleteListener { dismissLoadingAlert() }
                    .addOnFailureListener()
                    {
                        showError(it)
                        showLoadingAlert()
                        documentReference.delete()
                            .addOnCompleteListener { dismissLoadingAlert() }
                    }
                    .addOnSuccessListener()
                    {
                        showLoadingAlert()
                        it.addOnCompleteListener { dismissLoadingAlert() }
                            .addOnFailureListener()
                            {
                                showError(it)
                            }
                            .addOnSuccessListener()
                            {
                                showLoadingAlert()
                                changedUrl = it.toString();
                                documentReference.update(fieldPath, it.toString())
                                    .addOnFailureListener { showError(it) }
                                    .addOnCompleteListener {
                                        dismissLoadingAlert() }
                            }
                    }
            } catch (e: Exception)
            {
                e.printStackTrace()
                dismissLoadingAlert()
                showError(e)
            }
        }
    }

}