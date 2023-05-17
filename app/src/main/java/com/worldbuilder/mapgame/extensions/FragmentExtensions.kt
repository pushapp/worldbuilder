package com.worldbuilder.mapgame.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackbar(message: String) {
    view?.let { v ->
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
    }
}

fun Fragment.showSnackbar(@StringRes messageResId: Int) {
    view?.let { v ->
        Snackbar.make(v, messageResId, Snackbar.LENGTH_LONG).show()
    }
}