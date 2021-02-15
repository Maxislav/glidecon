package com.atlas.mars.glidecon.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import io.reactivex.ObservableOnSubscribe
import io.reactivex.rxkotlin.Observables
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

class FragmentZoomControl : Fragment() {
    var isSubscribe = true;
    lateinit var imageZoomIn: ImageView
    lateinit var imageZoomOut: ImageView
    lateinit var myImage: MyImage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_zoom_control, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myImage = MyImage(activity as Context)
        val v1 = Observable.create<ImageView>(ObservableOnSubscribe { emitter ->
            view?.findViewById<ImageView>(R.id.zoom_in_view)?.let {
                imageZoomIn = it;
                emitter.onNext(it)
                emitter.onComplete()
            }
        })

        val v2 = Observable.create<ImageView>(ObservableOnSubscribe { emitter ->
            view?.findViewById<ImageView>(R.id.zoom_out_view)?.let {
                imageZoomOut = it;
                emitter.onNext(it)
                emitter.onComplete()
            }
        })

        Observables.combineLatest(v1, v2)
                .take(1)
                .subscribeBy {
                    imageViewCreated()
                }


    }

    private fun imageViewCreated() {
        imageZoomIn.setImageBitmap(myImage.btnZoomIn)
        imageZoomOut.setImageBitmap(myImage.btnZoomOut)

        imageZoomIn.setOnClickListener{
            MapBoxStore.zoomControlSubject.onNext(MapBoxStore.Zoom.IN)
        }
        imageZoomOut.setOnClickListener{
            MapBoxStore.zoomControlSubject.onNext(MapBoxStore.Zoom.OUT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}