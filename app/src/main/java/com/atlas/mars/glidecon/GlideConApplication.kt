package com.atlas.mars.glidecon

import android.app.Activity
import android.app.Application
import com.atlas.mars.glidecon.store.MapBoxStore

class GlideConApplication : Application() {
    var mActivity: Activity? = null
    private val _store: MapBoxStore = MapBoxStore()

    val store: MapBoxStore
        get() {
            return _store
        }

    init {

        // MapBoxStore()
    }


    override fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        // callback?.onActivityDestroyed
        super.registerActivityLifecycleCallbacks(callback)
    }
}
