package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.ResumeEntity
import java.io.File
import java.io.FileOutputStream

object ResumeExporter {

    fun exportToPdf(context: Context, resume: ResumeEntity, customFilename: String? = null): File? {
        val filename = if (!customFilename.isNullOrBlank()) {
            if (customFilename.endsWith(".pdf", ignoreCase = true)) customFilename else "$customFilename.pdf"
        } else {
            "${resume.name.replace(" ", "_")}_Resume.pdf"
        }

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size: 595 x 842
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Parse Accent Color
        val accentColorInt = try {
            Color.parseColor(resume.accentColor)
        } catch (e: Exception) {
            Color.parseColor("#6750A4")
        }

        var y = 45f // Current Y coordinate for drawing
        val margin = 40f
        val contentWidth = 595f - (margin * 2)

        // Helper to check page boundary and start a new page if necessary
        fun checkNewPage(neededHeight: Float) {
            if (y + neededHeight > 810f) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 45f
            }
        }

        if (resume.templateName == "Professional Split") {
            // Left sidebar width = 175f, gutter = 20f, right column width = 320f
            val sidebarWidth = 175f
            val rightColX = margin + sidebarWidth + 20f
            val rightColWidth = 595f - rightColX - margin

            // Draw Left Sidebar Background (soft light gray)
            val bgPaint = Paint().apply {
                color = Color.parseColor("#F5F5F5")
                style = Paint.Style.FILL
            }
            canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)

            var yLeft = 45f
            var yRight = 45f

            // Helper for Left Column Boundary Check
            fun checkNewLeftPage(neededHeight: Float) {
                if (yLeft + neededHeight > 810f) {
                    // Start new page
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    // Redraw background on new page
                    canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                    yLeft = 45f
                    yRight = 45f
                }
            }

            // Draw Left Contact Details
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f * resume.fontSizeMultiplier
            paint.color = accentColorInt
            canvas.drawText("CONTACT", margin, yLeft, paint)
            yLeft += 14f

            paint.textSize = 8.5f * resume.fontSizeMultiplier
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.BLACK

            if (resume.phone.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Phone:", margin, yLeft, paint)
                yLeft += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.DKGRAY
                canvas.drawText(resume.phone, margin, yLeft, paint)
                yLeft += 14f
            }

            if (resume.email.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText("Email:", margin, yLeft, paint)
                yLeft += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.DKGRAY
                val wrappedEmail = wrapText(resume.email, paint, sidebarWidth)
                for (line in wrappedEmail) {
                    canvas.drawText(line, margin, yLeft, paint)
                    yLeft += 12f
                }
                yLeft += 2f
            }

            val loc = listOfNotNull(
                if (resume.city.isNotBlank()) resume.city else null,
                if (resume.country.isNotBlank()) resume.country else null
            ).joinToString(", ")
            if (loc.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText("Location:", margin, yLeft, paint)
                yLeft += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.DKGRAY
                val wrappedLoc = wrapText(loc, paint, sidebarWidth)
                for (line in wrappedLoc) {
                    canvas.drawText(line, margin, yLeft, paint)
                    yLeft += 12f
                }
                yLeft += 2f
            }

            if (resume.website.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText("Portfolio:", margin, yLeft, paint)
                yLeft += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.DKGRAY
                val wrappedWeb = wrapText(resume.website, paint, sidebarWidth)
                for (line in wrappedWeb) {
                    canvas.drawText(line, margin, yLeft, paint)
                    yLeft += 12f
                }
                yLeft += 2f
            }

            if (resume.github.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText("GitHub:", margin, yLeft, paint)
                yLeft += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.DKGRAY
                val wrappedGit = wrapText(resume.github, paint, sidebarWidth)
                for (line in wrappedGit) {
                    canvas.drawText(line, margin, yLeft, paint)
                    yLeft += 12f
                }
                yLeft += 2f
            }

            yLeft += 12f

            // Draw Left Skills
            val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
            if (activeSkills.isNotEmpty()) {
                checkNewLeftPage(30f)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText("SKILLS", margin, yLeft, paint)
                yLeft += 14f

                paint.textSize = 8.5f * resume.fontSizeMultiplier
                for (group in activeSkills) {
                    checkNewLeftPage(24f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.BLACK
                    canvas.drawText(group.label, margin, yLeft, paint)
                    yLeft += 10f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.DKGRAY
                    val wrappedItems = wrapText(group.items.joinToString(", "), paint, sidebarWidth)
                    for (line in wrappedItems) {
                        checkNewLeftPage(12f)
                        canvas.drawText(line, margin, yLeft, paint)
                        yLeft += 12f
                    }
                    yLeft += 4f
                }
            }

            yLeft += 10f

            // Draw Left Education
            val activeEdu = resume.education.filter { it.institute.isNotBlank() }
            if (activeEdu.isNotEmpty()) {
                checkNewLeftPage(30f)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText("EDUCATION", margin, yLeft, paint)
                yLeft += 14f

                paint.textSize = 8.5f * resume.fontSizeMultiplier
                for (edu in activeEdu) {
                    checkNewLeftPage(35f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.BLACK
                    val wrappedInst = wrapText(edu.institute, paint, sidebarWidth)
                    for (line in wrappedInst) {
                        canvas.drawText(line, margin, yLeft, paint)
                        yLeft += 10f
                    }

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.color = Color.DKGRAY
                    val wrappedDeg = wrapText(edu.degree, paint, sidebarWidth)
                    for (line in wrappedDeg) {
                        canvas.drawText(line, margin, yLeft, paint)
                        yLeft += 10f
                    }

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.GRAY
                    canvas.drawText("${edu.startYear} - ${edu.endYear}", margin, yLeft, paint)
                    yLeft += 14f
                }
            }

            // Draw Right Main Column content
            // Name
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 22f * resume.fontSizeMultiplier
            paint.color = accentColorInt
            val nameText = if (resume.name.isBlank()) "YOUR NAME" else resume.name
            canvas.drawText(nameText, rightColX, yRight, paint)
            yRight += 24f

            // Summary Section
            if (resume.summary.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText("PROFESSIONAL SUMMARY", rightColX, yRight, paint)
                yRight += 12f

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 9.5f * resume.fontSizeMultiplier
                paint.color = Color.BLACK
                val lines = wrapText(resume.summary, paint, rightColWidth)
                for (line in lines) {
                    if (yRight > 810f) {
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                        yRight = 45f
                    }
                    canvas.drawText(line, rightColX, yRight, paint)
                    yRight += 13f
                }
                yRight += 12f
            }

            // Experiences
            val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
            if (activeExp.isNotEmpty()) {
                if (yRight + 25f > 810f) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                    yRight = 45f
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText("EXPERIENCE", rightColX, yRight, paint)
                yRight += 14f

                for (exp in activeExp) {
                    if (yRight + 30f > 810f) {
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                        yRight = 45f
                    }

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    canvas.drawText(exp.role, rightColX, yRight, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val dateStr = "${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}"
                    val dateWidth = paint.measureText(dateStr)
                    canvas.drawText(dateStr, 595f - margin - dateWidth, yRight, paint)
                    yRight += 12f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.color = accentColorInt
                    canvas.drawText(exp.organization, rightColX, yRight, paint)
                    yRight += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.BLACK
                    paint.textSize = 9f * resume.fontSizeMultiplier
                    for (bullet in exp.bullets) {
                        if (bullet.isBlank()) continue
                        val wrapped = wrapText("•  $bullet", paint, rightColWidth - 10f)
                        for (i in wrapped.indices) {
                            if (yRight + 12f > 810f) {
                                pdfDocument.finishPage(page)
                                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                                page = pdfDocument.startPage(newPageInfo)
                                canvas = page.canvas
                                canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                                yRight = 45f
                            }
                            val indent = if (i == 0) rightColX else rightColX + 8f
                            canvas.drawText(wrapped[i], indent, yRight, paint)
                            yRight += 12f
                        }
                    }
                    yRight += 6f
                }
            }

            // Projects
            val activeProjects = resume.projects.filter { it.title.isNotBlank() }
            if (activeProjects.isNotEmpty()) {
                if (yRight + 25f > 810f) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                    yRight = 45f
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText("PROJECTS & PORTFOLIOS", rightColX, yRight, paint)
                yRight += 14f

                for (proj in activeProjects) {
                    if (yRight + 30f > 810f) {
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                        yRight = 45f
                    }

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    val titleString = if (proj.stack.isNotBlank()) "${proj.title}  (${proj.stack})" else proj.title
                    canvas.drawText(titleString, rightColX, yRight, paint)

                    if (proj.role.isNotBlank()) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        paint.textSize = 9f * resume.fontSizeMultiplier
                        paint.color = Color.GRAY
                        val roleWidth = paint.measureText(proj.role)
                        canvas.drawText(proj.role, 595f - margin - roleWidth, yRight, paint)
                    }
                    yRight += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.BLACK
                    paint.textSize = 9f * resume.fontSizeMultiplier
                    for (bullet in proj.bullets) {
                        if (bullet.isBlank()) continue
                        val wrapped = wrapText("•  $bullet", paint, rightColWidth - 10f)
                        for (i in wrapped.indices) {
                            if (yRight + 12f > 810f) {
                                pdfDocument.finishPage(page)
                                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                                page = pdfDocument.startPage(newPageInfo)
                                canvas = page.canvas
                                canvas.drawRect(margin - 10f, 30f, margin + sidebarWidth + 10f, 812f, bgPaint)
                                yRight = 45f
                            }
                            val indent = if (i == 0) rightColX else rightColX + 8f
                            canvas.drawText(wrapped[i], indent, yRight, paint)
                            yRight += 12f
                        }
                    }
                    yRight += 6f
                }
            }

        } else if (resume.templateName == "Executive Elegant") {
            // Draw Center Header
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 22f * resume.fontSizeMultiplier
            paint.color = accentColorInt
            val nameText = if (resume.name.isBlank()) "YOUR NAME" else resume.name.uppercase()
            val nameWidth = paint.measureText(nameText)
            canvas.drawText(nameText, (595f - nameWidth) / 2f, y, paint)
            y += 18f

            // Center contacts
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f * resume.fontSizeMultiplier
            paint.color = Color.DKGRAY

            val contactLine = listOfNotNull(
                if (resume.city.isNotBlank() || resume.country.isNotBlank()) {
                    listOfNotNull(resume.city.ifBlank { null }, resume.country.ifBlank { null }).joinToString(", ")
                } else null,
                if (resume.phone.isNotBlank()) resume.phone else null,
                if (resume.email.isNotBlank()) resume.email else null
            ).joinToString("   •   ")

            if (contactLine.isNotBlank()) {
                val cWidth = paint.measureText(contactLine)
                canvas.drawText(contactLine, (595f - cWidth) / 2f, y, paint)
                y += 13f
            }

            val linksLine = listOfNotNull(
                if (resume.website.isNotBlank()) "Portfolio: ${resume.website}" else null,
                if (resume.github.isNotBlank()) "GitHub: ${resume.github}" else null
            ).joinToString("   •   ")

            if (linksLine.isNotBlank()) {
                val lWidth = paint.measureText(linksLine)
                canvas.drawText(linksLine, (595f - lWidth) / 2f, y, paint)
                y += 15f
            } else {
                y += 5f
            }

            // Draw Double Elegant Border line
            paint.color = accentColorInt
            paint.strokeWidth = 3f
            canvas.drawLine(margin, y, 595f - margin, y, paint)
            y += 4f
            paint.strokeWidth = 1f
            canvas.drawLine(margin, y, 595f - margin, y, paint)
            y += 20f

            // Section Header helper for Elegant
            fun drawElegantHeader(title: String) {
                checkNewPage(35f)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                val tWidth = paint.measureText(title.uppercase())
                canvas.drawText(title.uppercase(), (595f - tWidth) / 2f, y, paint)
                y += 5f

                // Underline
                paint.color = accentColorInt
                paint.strokeWidth = 1f
                canvas.drawLine((595f - tWidth) / 2f, y, (595f + tWidth) / 2f, y, paint)
                y += 15f
            }

            // Summary
            if (resume.summary.isNotBlank()) {
                drawElegantHeader("Professional Summary")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f * resume.fontSizeMultiplier
                paint.color = Color.BLACK
                val lines = wrapText(resume.summary, paint, contentWidth)
                for (line in lines) {
                    checkNewPage(14f)
                    canvas.drawText(line, margin, y, paint)
                    y += 14f
                }
                y += 10f
            }

            // Experience
            val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
            if (activeExp.isNotEmpty()) {
                drawElegantHeader("Experience")
                for (exp in activeExp) {
                    checkNewPage(45f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    canvas.drawText(exp.role, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val dateStr = "${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}"
                    val dateWidth = paint.measureText(dateStr)
                    canvas.drawText(dateStr, 595f - margin - dateWidth, y, paint)
                    y += 13f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = accentColorInt
                    canvas.drawText(exp.organization, margin, y, paint)
                    y += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.BLACK
                    for (bullet in exp.bullets) {
                        if (bullet.isBlank()) continue
                        val wrapped = wrapText("•  $bullet", paint, contentWidth - 15f)
                        for (i in wrapped.indices) {
                            checkNewPage(14f)
                            val indent = if (i == 0) margin else margin + 12f
                            canvas.drawText(wrapped[i], indent, y, paint)
                            y += 14f
                        }
                    }
                    y += 8f
                }
            }

            // Projects
            val activeProjects = resume.projects.filter { it.title.isNotBlank() }
            if (activeProjects.isNotEmpty()) {
                drawElegantHeader("Projects & Portfolios")
                for (proj in activeProjects) {
                    checkNewPage(40f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    val titleString = if (proj.stack.isNotBlank()) "${proj.title}  (${proj.stack})" else proj.title
                    canvas.drawText(titleString, margin, y, paint)

                    if (proj.role.isNotBlank()) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        paint.textSize = 9.5f * resume.fontSizeMultiplier
                        paint.color = Color.GRAY
                        val roleWidth = paint.measureText(proj.role)
                        canvas.drawText(proj.role, 595f - margin - roleWidth, y, paint)
                    }
                    y += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.BLACK
                    for (bullet in proj.bullets) {
                        if (bullet.isBlank()) continue
                        val wrapped = wrapText("•  $bullet", paint, contentWidth - 15f)
                        for (i in wrapped.indices) {
                            checkNewPage(14f)
                            val indent = if (i == 0) margin else margin + 12f
                            canvas.drawText(wrapped[i], indent, y, paint)
                            y += 14f
                        }
                    }
                    y += 8f
                }
            }

            // Education
            val activeEdu = resume.education.filter { it.institute.isNotBlank() }
            if (activeEdu.isNotEmpty()) {
                drawElegantHeader("Education")
                for (edu in activeEdu) {
                    checkNewPage(35f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    canvas.drawText(edu.institute, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val dateStr = "${edu.startYear} - ${edu.endYear}"
                    val dateWidth = paint.measureText(dateStr)
                    canvas.drawText(dateStr, 595f - margin - dateWidth, y, paint)
                    y += 13f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = accentColorInt
                    canvas.drawText(edu.degree, margin, y, paint)
                    y += 18f
                }
            }

            // Skills
            val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
            if (activeSkills.isNotEmpty()) {
                drawElegantHeader("Technical Expertise")
                for (group in activeSkills) {
                    checkNewPage(18f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    val groupLabel = "${group.label}: "
                    val labelWidth = paint.measureText(groupLabel)
                    canvas.drawText(groupLabel, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.DKGRAY
                    val skillItems = group.items.joinToString(", ")
                    val wrappedSkills = wrapText(skillItems, paint, contentWidth - labelWidth)

                    for (i in wrappedSkills.indices) {
                        if (i == 0) {
                            canvas.drawText(wrappedSkills[i], margin + labelWidth, y, paint)
                        } else {
                            checkNewPage(14f)
                            canvas.drawText(wrappedSkills[i], margin, y, paint)
                        }
                        y += 14f
                    }
                    y += 4f
                }
            }

        } else {
            // Draw default Modern Minimalist single-column
            // Draw Name
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 22f * resume.fontSizeMultiplier
            paint.color = accentColorInt
            val nameText = if (resume.name.isBlank()) "YOUR NAME" else resume.name
            canvas.drawText(nameText, margin, y, paint)
            y += 18f

            // Draw Sub-header contact info
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f * resume.fontSizeMultiplier
            paint.color = Color.DKGRAY

            val contactLine1 = listOfNotNull(
                if (resume.city.isNotBlank()) resume.city else null,
                if (resume.country.isNotBlank()) resume.country else null,
                if (resume.phone.isNotBlank()) resume.phone else null,
                if (resume.email.isNotBlank()) resume.email else null
            ).joinToString("  |  ")

            if (contactLine1.isNotBlank()) {
                canvas.drawText(contactLine1, margin, y, paint)
                y += 13f
            }

            val contactLine2 = listOfNotNull(
                if (resume.website.isNotBlank()) "Portfolio: ${resume.website}" else null,
                if (resume.github.isNotBlank()) "GitHub: ${resume.github}" else null
            ).joinToString("  |  ")

            if (contactLine2.isNotBlank()) {
                canvas.drawText(contactLine2, margin, y, paint)
                y += 15f
            } else {
                y += 5f
            }

            // Draw Horizontal Accent Bar
            paint.color = accentColorInt
            paint.strokeWidth = 2.5f
            canvas.drawLine(margin, y, 595f - margin, y, paint)
            y += 20f

            // Helper to draw a Section Title
            fun drawSectionHeader(title: String) {
                checkNewPage(35f)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f * resume.fontSizeMultiplier
                paint.color = accentColorInt
                canvas.drawText(title.uppercase(), margin, y, paint)
                y += 5f

                // Subtle underline
                paint.color = accentColorInt
                paint.strokeWidth = 1f
                canvas.drawLine(margin, y, margin + 80f, y, paint)
                y += 15f
            }

            // Summary Section
            if (resume.summary.isNotBlank()) {
                drawSectionHeader("Professional Summary")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f * resume.fontSizeMultiplier
                paint.color = Color.BLACK

                val lines = wrapText(resume.summary, paint, contentWidth)
                for (line in lines) {
                    checkNewPage(14f)
                    canvas.drawText(line, margin, y, paint)
                    y += 14f
                }
                y += 10f
            }

            // Work Experience
            val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
            if (activeExp.isNotEmpty()) {
                drawSectionHeader("Experience")
                for (exp in activeExp) {
                    checkNewPage(45f)

                    // Job Title & Dates
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    canvas.drawText(exp.role, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val dateStr = "${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}"
                    val dateWidth = paint.measureText(dateStr)
                    canvas.drawText(dateStr, 595f - margin - dateWidth, y, paint)
                    y += 13f

                    // Organization
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = accentColorInt
                    canvas.drawText(exp.organization, margin, y, paint)
                    y += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK

                    for (bullet in exp.bullets) {
                        if (bullet.isBlank()) continue
                        val wrappedBullets = wrapText("•  $bullet", paint, contentWidth - 15f)
                        for (i in wrappedBullets.indices) {
                            checkNewPage(14f)
                            val indent = if (i == 0) margin else margin + 12f
                            canvas.drawText(wrappedBullets[i], indent, y, paint)
                            y += 14f
                        }
                    }
                    y += 8f
                }
            }

            // Projects
            val activeProjects = resume.projects.filter { it.title.isNotBlank() }
            if (activeProjects.isNotEmpty()) {
                drawSectionHeader("Projects & Portfolios")
                for (proj in activeProjects) {
                    checkNewPage(40f)

                    // Project Title & Stack
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    val titleString = if (proj.stack.isNotBlank()) "${proj.title}  (${proj.stack})" else proj.title
                    canvas.drawText(titleString, margin, y, paint)

                    if (proj.role.isNotBlank()) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        paint.textSize = 9.5f * resume.fontSizeMultiplier
                        paint.color = Color.GRAY
                        val roleWidth = paint.measureText(proj.role)
                        canvas.drawText(proj.role, 595f - margin - roleWidth, y, paint)
                    }
                    y += 14f

                    // Bullets
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK

                    for (bullet in proj.bullets) {
                        if (bullet.isBlank()) continue
                        val wrappedBullets = wrapText("•  $bullet", paint, contentWidth - 15f)
                        for (i in wrappedBullets.indices) {
                            checkNewPage(14f)
                            val indent = if (i == 0) margin else margin + 12f
                            canvas.drawText(wrappedBullets[i], indent, y, paint)
                            y += 14f
                        }
                    }
                    y += 8f
                }
            }

            // Education
            val activeEdu = resume.education.filter { it.institute.isNotBlank() }
            if (activeEdu.isNotEmpty()) {
                drawSectionHeader("Education")
                for (edu in activeEdu) {
                    checkNewPage(35f)

                    // Institution & Dates
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    canvas.drawText(edu.institute, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val dateStr = "${edu.startYear} - ${edu.endYear}"
                    val dateWidth = paint.measureText(dateStr)
                    canvas.drawText(dateStr, 595f - margin - dateWidth, y, paint)
                    y += 13f

                    // Degree details
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = accentColorInt
                    canvas.drawText(edu.degree, margin, y, paint)
                    y += 18f
                }
            }

            // Skills
            val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
            if (activeSkills.isNotEmpty()) {
                drawSectionHeader("Technical Expertise")
                for (group in activeSkills) {
                    checkNewPage(18f)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9.5f * resume.fontSizeMultiplier
                    paint.color = Color.BLACK
                    val groupLabel = "${group.label}: "
                    val labelWidth = paint.measureText(groupLabel)
                    canvas.drawText(groupLabel, margin, y, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.DKGRAY
                    val skillItems = group.items.joinToString(", ")
                    val wrappedSkills = wrapText(skillItems, paint, contentWidth - labelWidth)

                    for (i in wrappedSkills.indices) {
                        if (i == 0) {
                            canvas.drawText(wrappedSkills[i], margin + labelWidth, y, paint)
                        } else {
                            checkNewPage(14f)
                            canvas.drawText(wrappedSkills[i], margin, y, paint)
                        }
                        y += 14f
                    }
                    y += 4f
                }
            }
        }

        pdfDocument.finishPage(page)

        return try {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        pdfDocument.writeTo(outStream)
                    }
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                } else null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, filename)
                FileOutputStream(targetFile).use { outStream ->
                    pdfDocument.writeTo(outStream)
                }
                targetFile
            }
            
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    fun exportToDocx(context: Context, resume: ResumeEntity, customFilename: String? = null): File? {
        val filename = if (!customFilename.isNullOrBlank()) {
            if (customFilename.endsWith(".doc", ignoreCase = true)) customFilename else "$customFilename.doc"
        } else {
            "${resume.name.replace(" ", "_")}_Resume.doc"
        }

        val htmlContent = buildString {
            append("<!DOCTYPE html>")
            append("<html>")
            append("<head>")
            append("<meta charset='utf-8'>")
            append("<title>${resume.name}</title>")
            append("<style>")
            
            // Shared Base Styles
            append("body { font-family: 'Arial', sans-serif; line-height: 1.4; color: #333333; margin: 40px; }")
            append(".entry { margin-top: 8px; margin-bottom: 8px; }")
            append(".bullets { margin-left: 15px; margin-top: 3px; margin-bottom: 5px; font-size: 10px; }")
            append(".skill-label { font-weight: bold; }")
            
            // Conditional CSS based on templateName
            when (resume.templateName) {
                "Professional Split" -> {
                    append("h1 { color: ${resume.accentColor}; font-size: 24px; margin-bottom: 15px; }")
                    append(".sidebar-title { color: ${resume.accentColor}; font-size: 12px; text-transform: uppercase; font-weight: bold; margin-top: 15px; border-bottom: 1px solid #cccccc; padding-bottom: 3px; }")
                    append(".section-title { color: ${resume.accentColor}; font-size: 13px; text-transform: uppercase; font-weight: bold; margin-top: 15px; border-bottom: 1px solid #cccccc; padding-bottom: 3px; }")
                    append(".sidebar-content { font-size: 9.5px; color: #555555; margin-bottom: 10px; }")
                }
                "Executive Elegant" -> {
                    append("h1 { color: ${resume.accentColor}; font-size: 24px; text-align: center; margin-bottom: 5px; text-transform: uppercase; }")
                    append(".contact { font-size: 10px; color: #555555; text-align: center; margin-bottom: 10px; }")
                    append(".double-line { border-top: 3px double ${resume.accentColor}; margin-top: 10px; margin-bottom: 15px; }")
                    append(".section-title { color: ${resume.accentColor}; font-size: 14px; text-transform: uppercase; font-weight: bold; text-align: center; margin-top: 20px; border-bottom: 1px solid #cccccc; padding-bottom: 3px; }")
                }
                else -> { // Modern Minimalist / Default
                    append("h1 { color: ${resume.accentColor}; font-size: 24px; margin-bottom: 2px; }")
                    append(".contact { font-size: 10px; color: #666666; margin-bottom: 15px; border-bottom: 2px solid ${resume.accentColor}; padding-bottom: 8px; }")
                    append(".section-title { color: ${resume.accentColor}; font-size: 14px; text-transform: uppercase; font-weight: bold; margin-top: 20px; border-bottom: 1px solid #cccccc; padding-bottom: 3px; }")
                }
            }
            append("</style>")
            append("</head>")
            append("<body>")

            when (resume.templateName) {
                "Professional Split" -> {
                    // Two-column layout using table (highly compatible with Word)
                    append("<table style='width: 100%; border-collapse: collapse; table-layout: fixed;'>")
                    append("<tr>")
                    
                    // Left Column (35% width)
                    append("<td style='width: 35%; vertical-align: top; padding-right: 15px; border-right: 1.5px solid #dddddd;'>")
                    
                    // Contact Info
                    append("<div class='sidebar-title'>Contact</div>")
                    append("<div class='sidebar-content'>")
                    if (resume.phone.isNotBlank()) append("<strong>Phone:</strong><br>${resume.phone}<br><br>")
                    if (resume.email.isNotBlank()) append("<strong>Email:</strong><br>${resume.email}<br><br>")
                    val loc = listOfNotNull(
                        if (resume.city.isNotBlank()) resume.city else null,
                        if (resume.country.isNotBlank()) resume.country else null
                    ).joinToString(", ")
                    if (loc.isNotBlank()) append("<strong>Location:</strong><br>$loc<br><br>")
                    if (resume.website.isNotBlank()) append("<strong>Portfolio:</strong><br>${resume.website}<br><br>")
                    if (resume.github.isNotBlank()) append("<strong>GitHub:</strong><br>${resume.github}")
                    append("</div>")

                    // Skills
                    val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
                    if (activeSkills.isNotEmpty()) {
                        append("<div class='sidebar-title'>Skills</div>")
                        append("<div class='sidebar-content'>")
                        for (group in activeSkills) {
                            append("<div style='margin-top: 6px;'><strong>${group.label}</strong></div>")
                            append("<div>${group.items.joinToString(", ")}</div>")
                        }
                        append("</div>")
                    }

                    // Education
                    val activeEdu = resume.education.filter { it.institute.isNotBlank() }
                    if (activeEdu.isNotEmpty()) {
                        append("<div class='sidebar-title'>Education</div>")
                        append("<div class='sidebar-content'>")
                        for (edu in activeEdu) {
                            append("<div style='margin-top: 6px; font-weight: bold;'>${edu.institute}</div>")
                            append("<div style='font-style: italic;'>${edu.degree}</div>")
                            append("<div style='color: #777777;'>${edu.startYear} - ${edu.endYear}</div>")
                        }
                        append("</div>")
                    }
                    
                    append("</td>")
                    
                    // Right Column (65% width)
                    append("<td style='width: 65%; vertical-align: top; padding-left: 15px;'>")
                    
                    // Name
                    append("<h1>${if (resume.name.isBlank()) "YOUR NAME" else resume.name.uppercase()}</h1>")

                    // Summary
                    if (resume.summary.isNotBlank()) {
                        append("<div class='section-title'>Professional Summary</div>")
                        append("<p style='font-size: 10px; text-align: justify; margin-top: 5px;'>${resume.summary}</p>")
                    }

                    // Experience
                    val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
                    if (activeExp.isNotEmpty()) {
                        append("<div class='section-title'>Experience</div>")
                        for (exp in activeExp) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${exp.role}</td>")
                            append("<td style='text-align: right; font-size: 10px;'>${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}</td>")
                            append("</tr></table>")
                            append("<div style='font-style: italic; font-size: 10px; color: ${resume.accentColor};'>${exp.organization}</div>")
                            if (exp.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in exp.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }

                    // Projects
                    val activeProjects = resume.projects.filter { it.title.isNotBlank() }
                    if (activeProjects.isNotEmpty()) {
                        append("<div class='section-title'>Projects & Portfolios</div>")
                        for (proj in activeProjects) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${proj.title} <span style='font-weight: normal; font-size: 10px; color: #555555;'>(${proj.stack})</span></td>")
                            append("<td style='text-align: right; font-size: 10px; font-style: italic; color: #555555;'>${proj.role}</td>")
                            append("</tr></table>")
                            if (proj.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in proj.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }
                    
                    append("</td>")
                    append("</tr>")
                    append("</table>")
                }
                "Executive Elegant" -> {
                    // Name
                    append("<h1>${if (resume.name.isBlank()) "YOUR NAME" else resume.name.uppercase()}</h1>")
                    
                    // Center contacts
                    append("<div class='contact'>")
                    val contactLine = listOfNotNull(
                        if (resume.city.isNotBlank() || resume.country.isNotBlank()) {
                            listOfNotNull(resume.city.ifBlank { null }, resume.country.ifBlank { null }).joinToString(", ")
                        } else null,
                        if (resume.phone.isNotBlank()) resume.phone else null,
                        if (resume.email.isNotBlank()) resume.email else null
                    ).joinToString("   •   ")
                    append("<div>$contactLine</div>")
                    
                    val linksLine = listOfNotNull(
                        if (resume.website.isNotBlank()) "Portfolio: ${resume.website}" else null,
                        if (resume.github.isNotBlank()) "GitHub: ${resume.github}" else null
                    ).joinToString("   •   ")
                    if (linksLine.isNotBlank()) {
                        append("<div style='margin-top: 2px;'>$linksLine</div>")
                    }
                    append("</div>")

                    append("<div class='double-line'></div>")

                    // Summary
                    if (resume.summary.isNotBlank()) {
                        append("<div class='section-title'>Professional Summary</div>")
                        append("<p style='font-size: 10px; text-align: justify;'>${resume.summary}</p>")
                    }

                    // Experience
                    val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
                    if (activeExp.isNotEmpty()) {
                        append("<div class='section-title'>Experience</div>")
                        for (exp in activeExp) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${exp.role}</td>")
                            append("<td style='text-align: right; font-size: 10px;'>${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}</td>")
                            append("</tr></table>")
                            append("<div style='font-style: italic; font-size: 10px; color: ${resume.accentColor};'>${exp.organization}</div>")
                            if (exp.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in exp.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }

                    // Projects
                    val activeProjects = resume.projects.filter { it.title.isNotBlank() }
                    if (activeProjects.isNotEmpty()) {
                        append("<div class='section-title'>Projects & Portfolios</div>")
                        for (proj in activeProjects) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${proj.title} <span style='font-weight: normal; font-size: 10px; color: #555555;'>(${proj.stack})</span></td>")
                            append("<td style='text-align: right; font-size: 10px; font-style: italic; color: #555555;'>${proj.role}</td>")
                            append("</tr></table>")
                            if (proj.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in proj.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }

                    // Education
                    val activeEdu = resume.education.filter { it.institute.isNotBlank() }
                    if (activeEdu.isNotEmpty()) {
                        append("<div class='section-title'>Education</div>")
                        for (edu in activeEdu) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${edu.institute}</td>")
                            append("<td style='text-align: right; font-size: 10px;'>${edu.startYear} - ${edu.endYear}</td>")
                            append("</tr></table>")
                            append("<div style='font-style: italic; font-size: 10px; color: ${resume.accentColor};'>${edu.degree}</div>")
                            append("</div>")
                        }
                    }

                    // Skills
                    val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
                    if (activeSkills.isNotEmpty()) {
                        append("<div class='section-title'>Technical Expertise</div>")
                        for (group in activeSkills) {
                            append("<div style='font-size: 10px; margin-top: 5px;'>")
                            append("<span class='skill-label'>${group.label}: </span>")
                            append("<span>${group.items.joinToString(", ")}</span>")
                            append("</div>")
                        }
                    }
                }
                else -> { // Modern Minimalist
                    // Name
                    append("<h1>${if (resume.name.isBlank()) "YOUR NAME" else resume.name}</h1>")
                    
                    // Contacts
                    append("<div class='contact'>")
                    val contactLine1 = listOfNotNull(
                        if (resume.city.isNotBlank()) resume.city else null,
                        if (resume.country.isNotBlank()) resume.country else null,
                        if (resume.phone.isNotBlank()) resume.phone else null,
                        if (resume.email.isNotBlank()) resume.email else null
                    ).joinToString("  |  ")
                    append("<div>$contactLine1</div>")
                    
                    val contactLine2 = listOfNotNull(
                        if (resume.website.isNotBlank()) "Portfolio: ${resume.website}" else null,
                        if (resume.github.isNotBlank()) "GitHub: ${resume.github}" else null
                    ).joinToString("  |  ")
                    if (contactLine2.isNotBlank()) {
                        append("<div>$contactLine2</div>")
                    }
                    append("</div>")

                    // Summary
                    if (resume.summary.isNotBlank()) {
                        append("<div class='section-title'>Professional Summary</div>")
                        append("<p style='font-size: 10px; text-align: justify;'>${resume.summary}</p>")
                    }

                    // Experience
                    val activeExp = resume.experiences.filter { it.organization.isNotBlank() }
                    if (activeExp.isNotEmpty()) {
                        append("<div class='section-title'>Experience</div>")
                        for (exp in activeExp) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${exp.role}</td>")
                            append("<td style='text-align: right; font-size: 10px;'>${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}</td>")
                            append("</tr></table>")
                            append("<div class='entry-subtitle'>${exp.organization}</div>")
                            if (exp.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in exp.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }

                    // Projects
                    val activeProjects = resume.projects.filter { it.title.isNotBlank() }
                    if (activeProjects.isNotEmpty()) {
                        append("<div class='section-title'>Projects & Portfolios</div>")
                        for (proj in activeProjects) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${proj.title} <span style='font-weight: normal; font-size: 10px; color: #555555;'>(${proj.stack})</span></td>")
                            append("<td style='text-align: right; font-size: 10px; font-style: italic; color: #555555;'>${proj.role}</td>")
                            append("</tr></table>")
                            if (proj.bullets.isNotEmpty()) {
                                append("<ul class='bullets'>")
                                for (bullet in proj.bullets) {
                                    if (bullet.isNotBlank()) append("<li>$bullet</li>")
                                }
                                append("</ul>")
                            }
                            append("</div>")
                        }
                    }

                    // Education
                    val activeEdu = resume.education.filter { it.institute.isNotBlank() }
                    if (activeEdu.isNotEmpty()) {
                        append("<div class='section-title'>Education</div>")
                        for (edu in activeEdu) {
                            append("<div class='entry'>")
                            append("<table style='width: 100%; border-collapse: collapse;'><tr>")
                            append("<td style='font-weight: bold; font-size: 11px;'>${edu.institute}</td>")
                            append("<td style='text-align: right; font-size: 10px;'>${edu.startYear} - ${edu.endYear}</td>")
                            append("</tr></table>")
                            append("<div class='entry-subtitle'>${edu.degree}</div>")
                            append("</div>")
                        }
                    }

                    // Skills
                    val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
                    if (activeSkills.isNotEmpty()) {
                        append("<div class='section-title'>Technical Expertise</div>")
                        for (group in activeSkills) {
                            append("<div class='skills'>")
                            append("<span class='skill-label'>${group.label}: </span>")
                            append("<span>${group.items.joinToString(", ")}</span>")
                            append("</div>")
                        }
                    }
                }
            }

            append("</body>")
            append("</html>")
        }

        return try {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        outStream.write(htmlContent.toByteArray(Charsets.UTF_8))
                    }
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                } else null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, filename)
                FileOutputStream(targetFile).use { outStream ->
                    outStream.write(htmlContent.toByteArray(Charsets.UTF_8))
                }
                targetFile
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
