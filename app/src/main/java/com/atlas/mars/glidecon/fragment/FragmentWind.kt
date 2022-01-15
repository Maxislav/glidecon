package com.atlas.mars.glidecon.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import com.atlas.mars.glidecon.util.LocationUtil
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject

class FragmentWind : Fragment() {
    val TAG = "FragmentWind"

    private val _onDestroy = AsyncSubject.create<Boolean>();

    lateinit var windView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wind, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.findViewById<ImageView>(R.id.wind_image_view)?.let {
            it.post(Runnable {
                windViewCreated(it)
            })
        }
    }

    private fun windViewCreated(v: ImageView) {
        windView = v
        val myImage = MyImage(activity as Context)
        windView.setImageBitmap(myImage.btnWind)

        Observables.combineLatest(
                windSubject.map { it -> it[MapBoxStore.Wind.DIRECTION] },
                MapBoxStore.cameraPositionSubject.map { it -> it.bearing }
        )
                .takeUntil(_onDestroy)
                .subscribeBy {
                    val windDirection = it.first
                    val bearing = it.second
                    windDirection?.let {
                        windView.rotation = LocationUtil().bearingNormalize(windDirection - bearing).toFloat()
                    }

                }

    }

    override fun onDestroy() {
        super.onDestroy()
        _onDestroy.onComplete()
    }
}