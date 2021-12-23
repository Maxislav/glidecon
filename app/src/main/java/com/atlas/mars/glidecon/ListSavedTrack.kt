package com.atlas.mars.glidecon

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.databinding.ActivityListSavedTrackBinding
import com.atlas.mars.glidecon.databinding.TrackListItemBinding
import com.atlas.mars.glidecon.model.ListTrackItem
import com.atlas.mars.glidecon.store.MapBoxStore


interface IVehicle {
    val name: String
    val make: String
}

class ListSavedTrack : AppCompatActivity() {
    // private val TAG = "ListSavedTrack_tag"

    private lateinit var binding: ActivityListSavedTrackBinding
    var mapDateBase = MapDateBase(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSavedTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // binding =  DataBindingUtil.setContentView(this, R.layout.action_bar_list_saved_track)
        //setView(binding.root);
        val listView: ListView = binding.listView;


        // val trackList = arrayListOf<ListTrackItem>(ListTrackItem("Бразилия", 27))
        val trackList = mapDateBase.getTrackNameLis()






        val adapter = MyListAdapter(this, trackList)
        listView.adapter = adapter;
        Helper.getListViewSize(listView);
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.action_bar_list_saved_track)
        val tView = supportActionBar?.customView;
        tView?.findViewById<TextView>(R.id.title)?.setOnClickListener { v: View ->
            Log.d(TAG, "Title clicked")
            finish()
        }
        return true
    }

    fun finish(trackId: Int) {
        MapBoxStore.activeRoute.onNext(trackId)
        finish()
    }

    inner class MyLongClick(private val context: ListSavedTrack, private val item: ListTrackItem) : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            Log.d(TAG, "click ${item.name}")
            val popup = PopupMenu(context, v)
            popup.inflate(R.menu.menu_track_list)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                // Respond to menu item click.
                return@setOnMenuItemClickListener true
            }
            popup.show()
            return true
        }

    }


    inner class MyListAdapter(private val listSavedTrack: ListSavedTrack, private val items: ArrayList<ListTrackItem>) : ArrayAdapter<ListTrackItem>(listSavedTrack, 0, items), View.OnLongClickListener {
        lateinit var view: View
        lateinit var binding: TrackListItemBinding

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            if (!this::view.isInitialized) {
                binding = TrackListItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)

            }

            binding.linearRow.setOnClickListener {
                listSavedTrack.finish(items[position].trackId)
            }
            binding.linearRow.setOnLongClickListener(MyLongClick(listSavedTrack, items[position]))
            binding.trackItem = items[position]
            return binding.root
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onLongClick(v: View?): Boolean {
            return true
        }


    }

    companion object {
        const val TAG = "ListSavedTrack_tag"
    }

    object Helper {
        fun getListViewSize(myListView: ListView) {
            val myListAdapter = myListView.adapter
                    ?: //do nothing return null
                    return
            //set listAdapter in loop for getting final size
            var totalHeight = 0
            for (size in 0 until myListAdapter.count) {
                val listItem = myListAdapter.getView(size, null, myListView)
                listItem.measure(0, 0)
                totalHeight += listItem.measuredHeight
            }
            //setting listview item in adapter
            val params = myListView.layoutParams
            params.height = totalHeight + myListView.dividerHeight * (myListAdapter.count - 1)
            myListView.layoutParams = params
        }
    }
}