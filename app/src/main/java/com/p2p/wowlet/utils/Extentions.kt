package com.p2p.wowlet.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.p2p.wowlet.R
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


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
