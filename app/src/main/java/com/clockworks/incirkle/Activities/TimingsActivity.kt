package com.clockworks.incirkle.Activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.clockworks.incirkle.Adapters.DetailedListAdapter
import com.clockworks.incirkle.Models.Course
import com.clockworks.incirkle.R
import kotlinx.android.synthetic.main.activity_timings.*

class TimingsActivity : AppCompatActivity(), DetailedListAdapter.DeleteListener
{
    companion object
    {
        val REQUEST_CODE = 3
        val IDENTIFIER_CAN_MODIFY = "Can Modify"
        val IDENTIFIER_TIMINGS = "Timings"
    }

    private var timings = ArrayList<Course.Timing>()
    private var isAdmin = false
    private var deleteAlert: AlertDialog? = null

    fun updateTimingsListView()
    {
        this.deleteAlert = null
        listView_timings.adapter = DetailedListAdapter(this, this.timings.map { Pair(it.day.toString(), it.timePeriod()) }, if (this.isAdmin) this else null)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timings)
        supportActionBar?.let { it.title = getString(R.string.text_timings) }

        this.timings = intent.getSerializableExtra(IDENTIFIER_TIMINGS) as ArrayList<Course.Timing>
        this.isAdmin = this.intent.getBooleanExtra(IDENTIFIER_CAN_MODIFY, false)
        if (this.isAdmin)
        {
            button_addTiming.visibility = View.VISIBLE
            listView_timings.setOnItemClickListener()
            {
                _, _, position, _ ->

                val intent = Intent(this, TimingActivity::class.java)
                intent.putExtra(TimingActivity.IDENTIFIER_TIMING, this.timings[position])
                intent.putExtra(TimingActivity.IDENTIFIER_TIMING_INDEX, position)
                startActivityForResult(intent, TimingActivity.REQUEST_CODE)
            }
            registerForContextMenu(listView_timings)
        }
        this.updateTimingsListView()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)
    {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.listView_invitedStudents)
        {
            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            menu?.setHeaderTitle(this.timings[info.position].toString())
            menu?.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean
    {
        val menuInfo = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        this.timings.removeAt(menuInfo.position)
        this.updateTimingsListView()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == TimingActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            (data?.getSerializableExtra(TimingActivity.IDENTIFIER_TIMING) as? Course.Timing)?.let()
            {
                timing ->

                data.getIntExtra(TimingActivity.IDENTIFIER_TIMING_INDEX, -1).
                    let { if (it == -1) this.timings.add(timing) else this.timings[it] = timing }
                this.updateTimingsListView()
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemDelete(position: Int)
    {
        if (this.isAdmin)
        {
            this.deleteAlert = AlertDialog.Builder(this)
                .setTitle("Delete Timing")
                .setMessage("Are you sure you wish remove the course for timing: ${this.timings[position]}?")
                .setPositiveButton("Yes")
                {
                        _, _ ->
                    this.timings.removeAt(position)
                    this.deleteAlert = null
                    this.updateTimingsListView()
                }
                .setNegativeButton("No")
                {
                        _, _ ->
                    this.deleteAlert = null
                }
                .create()
            this.deleteAlert?.show()
        }
    }

    fun addTiming(view: View)
    {
        startActivityForResult(Intent(this, TimingActivity::class.java), TimingActivity.REQUEST_CODE)
    }

    fun done(v: View)
    {
        val intent = Intent()
        intent.putExtra(IDENTIFIER_TIMINGS, this.timings)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
