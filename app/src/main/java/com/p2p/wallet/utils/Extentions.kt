package com.p2p.wallet.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.p2p.wallet.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.concurrent.Executor

fun LineChart.initChart(chartList: List<Entry>) {
    val lineDataSet = LineDataSet(chartList, "DataSet1")
    lineDataSet.lineWidth = 2f
    lineDataSet.setDrawCircles(false)
    lineDataSet.setDrawValues(false)
    lineDataSet.color = R.color.cornflowerblue
    lineDataSet.setDrawFilled(true)
    lineDataSet.setDrawHorizontalHighlightIndicator(false)
    lineDataSet.highLightColor = R.color.cornflowerblue

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
        animateX(500)
    }
}

fun PieChart.drawChart(pieList: List<PieEntry>) {
    val dataSet = PieDataSet(pieList, "Election Results")
    dataSet.sliceSpace = 6f
    dataSet.selectionShift = 15f

    val colors = ArrayList<Int>()
    for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
    for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
    for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
    for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
    for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
    colors.add(ColorTemplate.getHoloBlue())
    dataSet.colors = colors

    val data = PieData(dataSet)
    data.setDrawValues(false)

    apply {
        setUsePercentValues(false)
        setTouchEnabled(false)
        description.isEnabled = false
        isDrawHoleEnabled = true
        setHoleColor(Color.WHITE)
        holeRadius = 68f
        setDrawCenterText(false)
        animateY(500)
        legend.isEnabled = false
        setDrawEntryLabels(false)
        this.data = data
        invalidate()
    }
}

fun Context.shareText(value: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, value)
    startActivity(Intent.createChooser(shareIntent, "Share Text"))
}

fun FragmentActivity.openFingerprintDialog(requestSuccess: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(this)
    val biometricManager = BiometricManager.from(this)
    when (biometricManager.canAuthenticate()) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            this.authUser(executor) {
                requestSuccess.invoke(true)
            }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            requestSuccess.invoke(false)
            Toast.makeText(
                this,
                getString(R.string.error_msg_no_biometric_hardware),
                Toast.LENGTH_LONG
            ).show()
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            requestSuccess.invoke(false)
            Toast.makeText(
                this,
                getString(R.string.error_msg_biometric_hw_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            startActivity(Intent(Settings.ACTION_SETTINGS))
            requestSuccess.invoke(false)
            Toast.makeText(
                this,
                getString(R.string.error_msg_biometric_not_setup),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun FragmentActivity.authUser(executor: Executor, requestSuccess: () -> Unit) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.auth_title))
        .setSubtitle(getString(R.string.auth_subtitle))
        .setDescription(getString(R.string.auth_description))
        .setDeviceCredentialAllowed(true)
        .build()

    val biometricPrompt = BiometricPrompt(
        this, executor,
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
        }
    )

    biometricPrompt.authenticate(promptInfo)
}

fun Fragment.openFingerprintDialog(requestSuccess: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(requireContext())
    val biometricManager = BiometricManager.from(requireContext())
    when (biometricManager.canAuthenticate()) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            this.authUser(executor) {
                requestSuccess.invoke(true)
            }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            requestSuccess.invoke(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_msg_no_biometric_hardware),
                Toast.LENGTH_LONG
            ).show()
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            requestSuccess.invoke(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_msg_biometric_hw_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            startActivity(Intent(Settings.ACTION_SETTINGS))
            requestSuccess.invoke(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_msg_biometric_not_setup),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun Fragment.authUser(executor: Executor, requestSuccess: () -> Unit) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.auth_title))
        .setSubtitle(getString(R.string.auth_subtitle))
        .setDescription(getString(R.string.auth_description))
        .setDeviceCredentialAllowed(true)
        .build()

    val biometricPrompt = BiometricPrompt(
        this, executor,
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
                Toast.makeText(requireContext(), "Auth failed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    biometricPrompt.authenticate(promptInfo)
}

fun Context.isFingerPrintSet(): Boolean {
    return BiometricManager.from(this)
        .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
}

fun TextView.changeTextColor(selectedTextView: AppCompatTextView?) {
    this.setTextColor(ContextCompat.getColor(this.context, R.color.black))
    this.typeface = Typeface.DEFAULT_BOLD
    this.isEnabled = false
    selectedTextView?.run {
        setTextColor(ContextCompat.getColor(selectedTextView.context, R.color.gray_600))
        typeface = Typeface.DEFAULT
        isEnabled = true
    }
}

fun getOneHour(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.HOUR, -1)
    return cal.timeInMillis
}

fun getFourHour(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.HOUR, -4)
    return cal.timeInMillis
}

fun getYesterday(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    return cal.timeInMillis
}

fun getWeekly(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.WEEK_OF_MONTH, -1)
    return cal.timeInMillis
}

fun getMonthly(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    return cal.timeInMillis
}

fun getYear(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR, -1)
    return cal.timeInMillis
}

fun Double.roundCurrencyValue(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.HALF_EVEN).toDouble()
}

fun Double.roundToThousandsCurrencyValue(): Double {
    return BigDecimal(this).setScale(4, RoundingMode.HALF_EVEN).toDouble()
}

fun Double.roundToMilCurrencyValue(): BigDecimal {
    return BigDecimal(this).setScale(6, RoundingMode.HALF_UP)
}

fun Double.roundToBilCurrencyValue(): BigDecimal {
    return BigDecimal(this).setScale(9, RoundingMode.HALF_UP)
}