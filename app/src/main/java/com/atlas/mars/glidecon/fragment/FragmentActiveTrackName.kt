package com.atlas.mars.glidecon.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.FragmentActiveTrackNameBinding
import com.atlas.mars.glidecon.databinding.FragmentDashboardBinding
import com.atlas.mars.glidecon.model.TToast
import com.atlas.mars.glidecon.store.MapBoxStore
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject

class FragmentActiveTrackName: Fragment() {
    private lateinit var binding: FragmentActiveTrackNameBinding
    private val _onDestroy = AsyncSubject.create<Boolean>();

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentActiveTrackNameBinding.inflate(inflater, container, false)
        // return inflater.inflate(R.layout.fragment_dashboard, null)
        binding.fragmentActiveTrackName = this
        return binding.root;
        /*MapBoxStore.activeRouteName
                .takeUntil(_onDestroy)
                .subscribeBy {  name ->
                    findViewById<TextView>(R.id.activeTrackName)?.let {  it.text = name  }
                }*/

    }

    fun onNameClick(v: View){
        activity
      /*  val tToast = activity?.let { TToast(it) }
        tToast?.show("olool", TToast.Type.ERROR, 5000)*/

        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.menu_active_track_name)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId){
                R.id.item_hide -> {
                    MapBoxStore.activeRoute.onNext(-1)
                }
            }
            // Respond to menu item click.
            return@setOnMenuItemClickListener true
        }
        popup.show()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        MapBoxStore.activeRouteName
                .takeUntil(_onDestroy)
                .subscribeBy {  name ->
                    binding.activeTrackName.text = name
                  //   findViewById<TextView>(R.id.activeTrackName)?.let {  it.text = name  }
                }

    }

    override fun onDestroy() {
        _onDestroy.onComplete()
        super.onDestroy()

    }

}