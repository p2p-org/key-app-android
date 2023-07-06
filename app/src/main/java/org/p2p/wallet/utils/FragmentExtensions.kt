package org.p2p.wallet.utils

import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import android.content.ActivityNotFoundException
import android.content.Intent
import timber.log.Timber
import java.io.File
import kotlin.reflect.KClass
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R

fun <T : Fragment> Fragment.instantiate(clazz: KClass<T>): Fragment =
    childFragmentManager.fragmentFactory.instantiate(clazz)

fun <T : Fragment> FragmentFactory.instantiate(clazz: KClass<T>): Fragment =
    instantiate(clazz.java.classLoader!!, clazz.java.name)

fun Fragment.openFile(file: File) {
    val fromFile = FileProvider.getUriForFile(
        requireContext(),
        BuildConfig.APPLICATION_ID + ".provider",
        file
    )

    val target = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fromFile, "application/pdf")
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        startActivity(target)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e, "Cannot open file")
        view?.showSnackbarShort(getString(R.string.error_opening_file))
    }
}
