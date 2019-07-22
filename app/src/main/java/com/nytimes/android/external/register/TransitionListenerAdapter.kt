package com.nytimes.android.external.register

import android.transition.Transition

internal open class TransitionListenerAdapter : Transition.TransitionListener {

    override fun onTransitionStart(transition: Transition) {
        // No op
    }

    override fun onTransitionEnd(transition: Transition) {
        // No op
    }

    override fun onTransitionCancel(transition: Transition) {
        // No op
    }

    override fun onTransitionPause(transition: Transition) {
        // No op
    }

    override fun onTransitionResume(transition: Transition) {
        // No op
    }
}
