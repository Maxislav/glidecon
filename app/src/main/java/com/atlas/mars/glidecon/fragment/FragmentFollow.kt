package com.atlas.mars.glidecon.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.FragmentFollowBinding
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.followTypeSubject
import io.reactivex.subjects.AsyncSubject
import java.util.concurrent.TimeUnit

class FragmentFollow : Fragment() {
    private val _onDestroy = AsyncSubject.create<Boolean>();
    private val TAG = "FragmentFollow"
    lateinit var binding: FragmentFollowBinding
    lateinit var  myImage: MyImage

    private val handler = object: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                MapBoxStore.FollowViewType.TYPICAL.type ->{
                    binding.followImageView.setImageBitmap(myImage.btnArrowTypical)
                }
                MapBoxStore.FollowViewType.FOLLOW.type -> {
                    binding.followImageView.setImageBitmap(myImage.btnArrowFollow)
                }
                MapBoxStore.FollowViewType.FOLLOW_ROTATE.type -> {
                    binding.followImageView.setImageBitmap(myImage.btnArrowFollowRotate)
                }
            }
        }
    }

    companion object{

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _onDestroy.onComplete()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()

    }

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myImage = MyImage(activity as Context)
        followTypeSubject
                .takeUntil(_onDestroy)
                .subscribe{ followType ->
                    handler.sendEmptyMessageDelayed(followType.type, 40)
                }

        binding.followImageView.setOnClickListener {
            var index: Int = followTypeSubject.value.type //MapBoxStore.FollowViewType.values().indexOf(followTypeSubject.value)
            index = (index +1)%3

            followTypeSubject.onNext(MapBoxStore.FollowViewType.from(index))
        }
    }
}