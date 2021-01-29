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
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.cameraPosition
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.compassOnClickSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.mapbox.mapboxsdk.camera.CameraPosition
import io.reactivex.rxkotlin.subscribeBy


class FragmentCompass: Fragment() {

    private var isSubscribed = true
    var compassImageView: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_compass, null)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        val myImage = MyImage(activity as Context)
        compassImageView = view?.findViewById(R.id.compass_image_view)
        compassImageView?.setImageBitmap(myImage.btnCompass)

        isSubscribed = true
      /*  mapboxMapSubject
                .take(1)
                .subscribeBy (
                        onNext = {bearing ->
                            compassImageView?.rotation = 360 - bearing.toFloat()
                        }
                )*/

        cameraPosition.map { cameraPosition: CameraPosition -> cameraPosition.bearing }

                .subscribeBy (
                        onNext = {bearing ->
                            compassImageView?.rotation = 360 - bearing.toFloat()
                        }
                )

        compassImageView?.setOnClickListener {
            compassOnClickSubject.onNext(true)
        }
    }


    override fun onDestroy() {
        isSubscribed = false
        super.onDestroy()
    }
}