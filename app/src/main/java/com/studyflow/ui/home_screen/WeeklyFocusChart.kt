package com.studyflow.ui.home_screen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.studyflow.R

class WeeklyFocusChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_blue)
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bg_surface)
        style = Paint.Style.FILL
    }

    private val paintDotStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_blue)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private var dataPoints = listOf(0.2f, 0.5f, 0.3f, 0.8f, 0.6f, 0.9f, 0.7f)
    private val pathLine = Path()
    private val pathFill = Path()

    fun setData(points: List<Float>) {
        dataPoints = points
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 20f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        val stepX = chartWidth / (dataPoints.size - 1)
        
        pathLine.reset()
        pathFill.reset()

        val points = dataPoints.mapIndexed { index, value ->
            PointF(padding + index * stepX, padding + chartHeight * (1 - value))
        }

        // Create Smooth Curve
        pathLine.moveTo(points[0].x, points[0].y)
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val controlX = (p1.x + p2.x) / 2
            pathLine.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
        }

        // Create Fill Path
        pathFill.set(pathLine)
        pathFill.lineTo(points.last().x, height)
        pathFill.lineTo(points.first().x, height)
        pathFill.close()

        // Gradient Fill
        paintFill.shader = LinearGradient(
            0f, 0f, 0f, height,
            ContextCompat.getColor(context, R.color.chart_gradient_start),
            ContextCompat.getColor(context, R.color.chart_gradient_end),
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(pathFill, paintFill)
        canvas.drawPath(pathLine, paintLine)

        // Draw Dots
        points.forEach { point ->
            canvas.drawCircle(point.x, point.y, 10f, paintDot)
            canvas.drawCircle(point.x, point.y, 10f, paintDotStroke)
        }
    }
}
