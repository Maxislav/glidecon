package com.atlas.mars.glidecon.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.satelliteSubject
import io.reactivex.rxkotlin.subscribeBy

class FragmentGpsStatus : Fragment() {
    private var isSubscribed = false
    private val TAG = "FragmentGpsStatus"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //  return super.onCreateView(inflater, container, savedInstanceState)
        //return inflater.inflate(R.layout.frame_gps_status, container, false)
        return inflater.inflate(R.layout.fragment_gps_status, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        isSubscribed = true
        super.onActivityCreated(savedInstanceState)
        val context = activity as Context
        val myImage = MyImage(context)
        val gpsStatusView = view
        val statusImageView: ImageView? = gpsStatusView?.findViewById(R.id.statusImageView)
        // statusImageView!!.setImageBitmap(myImage.getImageGpsStat(10, 5))


        satelliteSubject
                .takeWhile { isSubscribed }
                .filter (fun(map): Boolean {
                    return 0 < map[MapBoxStore.SatCount.TOTAl]!!
                })
                .filter { map -> 0 < map[MapBoxStore.SatCount.TOTAl]!! }
                .distinctUntilChanged{ prev, next  ->
                    (prev[MapBoxStore.SatCount.USED] == next[MapBoxStore.SatCount.USED]) &&
                            (prev[MapBoxStore.SatCount.TOTAl] == next[MapBoxStore.SatCount.TOTAl])
                }
                .subscribeBy(
                        onNext = { map: Map<MapBoxStore.SatCount, Int> ->
                            val fix = map[MapBoxStore.SatCount.USED]
                            val all = map[MapBoxStore.SatCount.TOTAl]
                            Log.d(TAG, "${map.toString()}")
                            statusImageView?.setImageBitmap(myImage.getImageGpsStat(all!!, fix!!))

                        }
                )
    }


    override fun onDestroy() {
        isSubscribed = false;
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }
}