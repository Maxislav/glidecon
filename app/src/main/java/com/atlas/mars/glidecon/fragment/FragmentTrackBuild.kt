package com.atlas.mars.glidecon.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.FragmentTrackBuildBinding
import com.atlas.mars.glidecon.model.LandingBoxViewModel
import com.atlas.mars.glidecon.model.ViewModelBuildTrack
import com.atlas.mars.glidecon.store.MapBoxStore
import io.reactivex.subjects.BehaviorSubject

class FragmentTrackBuild : Fragment() {

    private val TAG = "FragmentTrackBuild Activity"

    var buttonColor: Int = 0

    val routeType = ObservableInt(-1)



    private lateinit var binding: FragmentTrackBuildBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTrackBuildBinding.inflate(inflater, container, false)
        // return inflater.inflate(R.layout.fragment_track_build, container, false)
        binding.fragmentTrackBuild = this;
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        R.color.blue
        // activity?.let{ a -> myViewModel =  ViewModelProviders.of(a).get(ViewModelBuildTrack::class.java)}
        initViewModel()

        activity?.let { buttonColor = ContextCompat.getColor(it.applicationContext, R.color.blue) }
        // binding.imageClose.setOnClickListener (this)
    }

    private fun initViewModel(){
        /*myViewModel =  ViewModelProviders.of(this).get(ViewModelBuildTrack::class.java)
        myViewModel.routeType.set(-1)

        myViewModel.routeType*/
        val v = routeType.get()
    }

    fun onClickRouteType(r: MapBoxStore.ROUTE_TYPE){
         when(r){
            MapBoxStore.ROUTE_TYPE.LINEAR -> {
                routeType.set(0)
            }
            MapBoxStore.ROUTE_TYPE.ROUTE -> {
                routeType.set(1)
            }
        }
    }

    fun onClickClose(){
        MapBoxStore.routeBuildProgress.onNext(false)
    }
}