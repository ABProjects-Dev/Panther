package com.panther.app.data.backup

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.panther.app.R
import com.panther.app.core.format.PantherDateFormat
import com.panther.app.domain.model.ProgramBackup
import com.panther.app.domain.model.TargetType
import java.io.OutputStream

object ProgramPdfExporter {
    // A4 at 72 dpi, with a uniform text margin.
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 50f
    private const val CONTENT_WIDTH = 495 // PAGE_WIDTH - 2 * PAGE_MARGIN
    private const val TOP_BASELINE = 42f
    private const val LINE_SPACING = 8f

    fun write(context: Context, program: ProgramBackup, output: OutputStream) {
        val document = PdfDocument()
        try {
            val regularFont = runCatching { ResourcesCompat.getFont(context, R.font.vazirmatn_regular) }.getOrNull()
            val boldFont = runCatching { ResourcesCompat.getFont(context, R.font.vazirmatn_bold) }.getOrNull()
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 11.5f
                color = android.graphics.Color.BLACK
                if (regularFont != null) typeface = regularFont
            }
            val programTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 18f
                color = android.graphics.Color.BLACK
                if (boldFont != null) typeface = boldFont else isFakeBoldText = true
            }
            val dayTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 15f
                color = android.graphics.Color.BLACK
                if (boldFont != null) typeface = boldFont else isFakeBoldText = true
            }
            val exerciseTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 13f
                color = android.graphics.Color.BLACK
                if (boldFont != null) typeface = boldFont else isFakeBoldText = true
            }
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var y = TOP_BASELINE
            fun nextPage() {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                y = TOP_BASELINE
            }
            fun drawLine(
                text: String,
                paint: TextPaint = bodyPaint,
                keepWithNextHeight: Float = 0f,
                rightIndent: Int = 0,
            ) {
                val rendered = PantherDateFormat.persianDigits(text)
                val availableWidth = (CONTENT_WIDTH - rightIndent).coerceAtLeast(200)
                val layout = StaticLayout.Builder.obtain(rendered, 0, rendered.length, paint, availableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.RTL)
                    .setIncludePad(false)
                    .build()
                if (y > TOP_BASELINE && y + layout.height + keepWithNextHeight > PAGE_HEIGHT - TOP_BASELINE) {
                    nextPage()
                }
                page.canvas.save()
                page.canvas.translate(PAGE_MARGIN, y)
                layout.draw(page.canvas)
                page.canvas.restore()
                y += layout.height + LINE_SPACING
            }

            drawLine("${context.getString(R.string.app_name)} — ${program.name}", paint = programTitlePaint)
            drawLine(context.getString(R.string.created_at, PantherDateFormat.medium(program.createdAt)))
            drawLine(context.getString(R.string.pdf_plan_only))
            y += 8f

            program.days.sortedBy { it.sortOrder }.forEach { day ->
                drawLine(day.name, paint = dayTitlePaint, keepWithNextHeight = 42f)
                day.exercises.sortedBy { it.sortOrder }.forEach { exercise ->
                    drawLine(
                        exercise.name,
                        paint = exerciseTitlePaint,
                        keepWithNextHeight = 24f,
                        rightIndent = 8,
                    )
                    exercise.sets.sortedBy { it.setNumber }.forEach { set ->
                        val target = when (set.targetType) {
                            TargetType.REPS -> context.getString(R.string.pdf_repetitions, set.targetValue ?: 0)
                            TargetType.DURATION -> context.getString(R.string.pdf_duration, set.targetMinutes ?: 0, set.targetSeconds ?: 0)
                        }
                        drawLine(
                            context.getString(R.string.pdf_set_line, set.setNumber, target),
                            paint = bodyPaint,
                            rightIndent = 18,
                        )
                    }
                }
                y += 6f
            }
            if (program.days.isEmpty()) drawLine(context.getString(R.string.no_days))
            document.finishPage(page)
            document.writeTo(output)
        } finally {
            document.close()
        }
    }
}
