package com.kite.mnemoai.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class BannerControl(val bannerView: View, val lifecycle: Lifecycle): LifecycleEventObserver {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoHideRunnable: Runnable? = null
    private var isShowing = false

    init {
        lifecycle.addObserver(this)
    }

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        if(event == Lifecycle.Event.ON_DESTROY){
            onDestroy()
        }
    }

    fun show(){
        autoHideRunnable?.let {
            mainHandler.removeCallbacks(it)
        }
        autoHideRunnable = null
        // 取消正在运行的动画，防止冲突
        bannerView.animate().cancel()
        //如果在展示就重置
        if(isShowing){
            bannerView.translationY = -bannerView.height.toFloat()
        }else{
            isShowing = true
            bannerView.visibility = View.VISIBLE
        }
        bannerView.translationY = -bannerView.height.toFloat()
        bannerView.animate()
            .translationY(0f).apply {
                duration = 300
                interpolator = AccelerateInterpolator()
            }.start()

    }

    fun startTimer(delayMillis: Int) {
        autoHideRunnable?.let {
            mainHandler.removeCallbacks(it)
        }
        autoHideRunnable = null
        val runnable = Runnable { hide() }
        autoHideRunnable = runnable
        mainHandler.postDelayed(runnable, delayMillis.toLong())
    }

    fun hide() {
        if(isShowing){
            bannerView.animate()
                .translationY(-bannerView.height.toFloat())
                .apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                }.setListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator) {
                        bannerView.visibility = View.GONE
                        bannerView.translationY = 0f
                        isShowing = false
                        bannerView.animate().setListener(null);
                    }
                })
                .start()
        }else{
            return
        }
    }

    fun forceHide(){
        if(isShowing){
            bannerView.visibility = View.GONE
            bannerView.translationY = 0f
            isShowing = false
            bannerView.animate().setListener(null);
        }
    }

    // 清理资源，防止内存泄漏
    private fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        bannerView.animate().cancel()
    }
}