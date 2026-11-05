package com.panther.app.data.backup

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.panther.app.R
import com.panther.app.domain.model.ProgramBackup
import java.io.OutputStream

object ProgramPdfExporter {
    fun write(context: Context, program: ProgramBackup, output: OutputStream) {
        val document = PdfDocument()
        try {
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = android.graphics.Color.BLACK }
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f; color = android.graphics.Color.BLACK; isFakeBoldText = true }
            val width = 495
            val height = 842
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(595, height, pageNumber).create())
            var y = 42f
            fun nextPage() {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(595, height, pageNumber).create())
                y = 42f
            }
            fun drawLine(text: String, title: Boolean = false) {
                val paint = if (title) titlePaint else textPaint
                val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.RTL)
                    .setIncludePad(false)
                    .build()
                if (y + layout.height > height - 42f) nextPage()
                page.canvas.save()
                page.canvas.translate(50f, y)
                layout.draw(page.canvas)
                page.canvas.restore()
                y += layout.height + 9f
            }
            drawLine(program.name, title = true)
            drawLine(context.getString(R.string.created_at, java.text.DateFormat.getDateInstance().format(java.util.Date(program.createdAt))))
            drawLine(context.getString(R.string.pdf_plan_only))
            y += 8f
            program.days.sortedBy { it.sortOrder }.forEach { day ->
                drawLine(day.name, title = true)
                day.exercises.sortedBy { it.sortOrder }.forEach { exercise ->
                    drawLine(exercise.name, title = true)
                    exercise.sets.sortedBy { it.setNumber }.forEach { set ->
                        val target = when (set.targetType) {
                            com.panther.app.domain.model.TargetType.REPS -> context.getString(R.string.pdf_repetitions, set.targetValue ?: 0)
                            com.panther.app.domain.model.TargetType.DURATION -> context.getString(R.string.pdf_duration, set.targetMinutes ?: 0, set.targetSeconds ?: 0)
                        }
                        drawLine(context.getString(R.string.pdf_set_line, set.setNumber, target))
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
