package com.atlas.mars.glidecon

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atlas.mars.glidecon.databinding.ActivityListSavedTrackBinding
import com.atlas.mars.glidecon.databinding.TrackListItemBinding


class ListSavedTrack : AppCompatActivity() {
    private val TAG = "ListSavedTrack_tag"

    private lateinit var binding: ActivityListSavedTrackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSavedTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // binding =  DataBindingUtil.setContentView(this, R.layout.action_bar_list_saved_track)
        //setView(binding.root);
        val listView: ListView = binding.listView;
        val countries = arrayListOf<String>(
                "Бразилия",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили", "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили", "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили", "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Аргентина",
                "Колумбия",
                "Чили",
                "Уругвай"
        )

        val adapter = MyListAdapter(this, countries)
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


    inner class MyListAdapter(context: Context, private val items: ArrayList<String>) : ArrayAdapter<String>(context, 0, items) {
        lateinit var view: View
        lateinit var binding: TrackListItemBinding

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            if(!this::view.isInitialized){
                binding = TrackListItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)

            }

            binding.textView1.text = items[position]
            return binding.root
        }

        override fun getCount(): Int {
            return items.size
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }


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