package com.clockworks.incirkle.Activities

import android.app.AlertDialog
import android.support.design.widget.Snackbar
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import dmax.dialog.SpotsDialog
import java.lang.Exception

abstract class AppActivity: AppCompatActivity()
{
    private var currentAlert: AlertDialog? = null
    private var progressDialogCount = 0

    private fun rootView(): View
    {
        return this.findViewById<View>(android.R.id.content)
    }

    fun <T>performThrowable(block: () -> T): T?
    {
        var result: T? = null
        try
        {
            result = block()
        }
        catch (e: Exception)
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
}