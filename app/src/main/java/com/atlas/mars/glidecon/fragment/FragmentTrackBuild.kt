package com.atlas.mars.glidecon.fragment

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.FragmentTrackBuildBinding
import com.atlas.mars.glidecon.model.LandingBoxViewModel
import com.atlas.mars.glidecon.model.ViewModelBuildTrack
import com.atlas.mars.glidecon.store.MapBoxStore
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject

class FragmentTrackBuild : Fragment() {

    private val TAG = "FragmentTrackBuild Activity"
    private val _onDestroy = AsyncSubject.create<Boolean>();

    var buttonColor: Drawable? = null

    val routeType: ObservableField<MapBoxStore.RouteType> = ObservableField<MapBoxStore.RouteType>()

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

        //  activity?.let { buttonColor = ContextCompat.getColor(it.applicationContext, R.color.blue) }
        activity?.let { buttonColor = ContextCompat.getDrawable(it, R.drawable.corner) }
        // binding.imageClose.setOnClickListener (this)
    }

    private fun initViewModel() {
        MapBoxStore.routeType
                .takeUntil(_onDestroy)
                .subscribeBy {
                    routeType.set(it)
                }

    }

    fun onClickRouteType(r: MapBoxStore.RouteType) {
        MapBoxStore.routeType.onNext(r)
    }

    fun onClickClose() {
        MapBoxStore.routeBuildProgress.onNext(false)
    }

    fun onClickSave() {
        MapBoxStore.routeButtonClick.onNext(MapBoxStore.RouteAction.SAVE)
        MapBoxStore.routeButtonClick.onNext(MapBoxStore.RouteAction.CLOSE)
        onClickClose()
    }

    fun onClickBack() {
        MapBoxStore.routeButtonClick.onNext(MapBoxStore.RouteAction.BACK)
    }

    override fun onDestroy() {
        if(!MapBoxStore.routeBuildProgress.hasComplete()){
            MapBoxStore.routeBuildProgress.onNext(false)
        }

        _onDestroy.onNext(true)
        _onDestroy.onComplete()
        super.onDestroy()
    }
}