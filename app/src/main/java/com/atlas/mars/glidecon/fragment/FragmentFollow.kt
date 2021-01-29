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
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.followTypeSubject
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class FragmentFollow : Fragment() {
    private var isSubscribed = false
    private val TAG = "FragmentFollow"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        isSubscribed = true
        super.onActivityCreated(savedInstanceState)
        val myImage = MyImage(activity as Context)

        val imageView: ImageView? = view?.findViewById(R.id.follow_image_view)
        // imageView?.setImageBitmap(myImage.btnArrowTypical)

        followTypeSubject.subscribeBy(
                onNext = { followType ->
                    when (followType) {
                        MapBoxStore.FollowViewType.TYPICAL -> {
                            imageView?.setImageBitmap(myImage.btnArrowTypical)
                        }
                        MapBoxStore.FollowViewType.FOLLOW -> {
                            imageView?.setImageBitmap(myImage.btnArrowFollow)
                        }
                        MapBoxStore.FollowViewType.FOLLOW_ROTATE -> {
                            imageView?.setImageBitmap(myImage.btnArrowFollowRotate)
                        }
                        else -> {

                        }
                    }
                }
        )

        imageView?.setOnClickListener {
            var index: Int = MapBoxStore.FollowViewType.values().indexOf(followTypeSubject.value)
            if(index == 2){
                index = 0
            }else{
                index++
            }
            followTypeSubject.onNext(MapBoxStore.FollowViewType.values()[index])
        }
    }
}