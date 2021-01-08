package kz.q19.animateviewscale

import android.view.View

/**
 * Performs the given action when this view is attached to a window. If the view is already
 * attached to a window the action will be performed immediately, otherwise the
 * action will be performed after the view is next attached.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 *
 * @see doOnDetach
 */
internal inline fun View.doOnAttach(crossinline action: (view: View) -> Unit) {
    if (isAttachedToWindow) {
        action(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                removeOnAttachStateChangeListener(this)
                action(view)
            }

            override fun onViewDetachedFromWindow(view: View) {}
        })
    }
}

/**
 * Performs the given action when this view is detached from a window. If the view is not
 * attached to a window the action will be performed immediately, otherwise the
 * action will be performed after the view is detached from its current window.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 *
 * @see doOnAttach
 */
internal inline fun View.doOnDetach(crossinline action: (view: View) -> Unit) {
    if (!isAttachedToWindow) {
        action(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {}

            override fun onViewDetachedFromWindow(view: View) {
                removeOnAttachStateChangeListener(this)
                action(view)
            }
        })
    }
}