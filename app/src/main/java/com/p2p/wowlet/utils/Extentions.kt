package com.p2p.wowlet.utils

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.p2p.wowlet.R
import java.util.concurrent.Executor

fun LineChart.initChart(chartList: List<Entry>) {
    val lineDataSet = LineDataSet(chartList, "DataSet1")
    lineDataSet.lineWidth = 2f
    lineDataSet.setDrawCircles(false)
    lineDataSet.setDrawValues(false)
    lineDataSet.color = R.color.black
    lineDataSet.setDrawFilled(true)
    lineDataSet.setDrawHorizontalHighlightIndicator(false)
    lineDataSet.highLightColor = R.color.black

    context?.run {
        val fillGradient = ContextCompat.getDrawable(this, R.drawable.bg_chart)
        lineDataSet.fillDrawable = fillGradient
    }
    this.apply {
        description = Description().apply { text = "" }
        setDrawBorders(false)
        setBorderColor(R.color.background_screens)
        axisRight.setDrawGridLines(false)
        axisLeft.setDrawGridLines(false)
        xAxis.setDrawGridLines(false)
        axisLeft.setDrawLabels(false)
        axisRight.setDrawLabels(false)
        xAxis.setDrawLabels(false)
        setTouchEnabled(true)
        legend.isEnabled = false
        val mv = MarkerView(context, R.layout.layout_dot)
        mv.setOffset(
            (-mv.measuredWidth / 2).toFloat(),
            (-mv.measuredHeight).toFloat() / 2
        )
        marker = mv
        data = LineData(lineDataSet)
        invalidate()
        animateX(500);
    }
}

fun Context.copyClipboard(value: String) {
    val clipboard =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", value)
    clipboard.setPrimaryClip(clip)
}

fun FragmentActivity.openFingerprintDialog(requestSuccess:()->Unit) {
    val executor = ContextCompat.getMainExecutor(this)
    val biometricManager = BiometricManager.from(this)
    when (biometricManager.canAuthenticate()) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            this.authUser(executor){
                requestSuccess.invoke()
            }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            Toast.makeText(
                this,
                getString(R.string.error_msg_no_biometric_hardware),
                Toast.LENGTH_LONG
            ).show()
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            Toast.makeText(
                this,
                getString(R.string.error_msg_biometric_hw_unavailable),
                Toast.LENGTH_LONG
            ).show()
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            when {
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R -> {
                    startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
                }
                else -> {
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
            }
            Toast.makeText(
                this,
                getString(R.string.error_msg_biometric_not_setup),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun FragmentActivity.authUser(executor: Executor, requestSuccess:()->Unit) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.auth_title))
        .setSubtitle(getString(R.string.auth_subtitle))
        .setDescription(getString(R.string.auth_description))
        .setDeviceCredentialAllowed(true)
        .build()

    val biometricPrompt = BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                requestSuccess.invoke()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@authUser, "Auth failed", Toast.LENGTH_SHORT).show()
            }
        })

    biometricPrompt.authenticate(promptInfo)
}

fun dp2px(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}

fun px2dp(context: Context, px: Float): Float {
    return px / context.resources.displayMetrics.density
}
