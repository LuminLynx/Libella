package com.example.foss101.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Minimal CommonMark renderer for the unit reader's authored prose.
 *
 * Supports the subset present in the v1 Tokenization unit and template:
 *   - paragraphs separated by blank lines
 *   - bullet lists (`-` or `*` prefix)
 *   - numbered lists (`1.` prefix)
 *   - inline **bold** and *italic*
 *   - multi-line list items via indented continuation lines
 *
 * Anything else (headings, links, code blocks) is rendered as plain
 * text. The authoring template is constrained enough that this
 * coverage is sufficient for v1; a heavier renderer can replace this
 * if richer formatting is needed later.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph ->
                    Text(text = parseInline(block.text), style = style)

                is MarkdownBlock.BulletList ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        block.items.forEach { item ->
                            BulletRow(marker = "•", content = item, style = style)
                        }
                    }

                is MarkdownBlock.NumberedList ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        block.items.forEachIndexed { index, item ->
                            BulletRow(marker = "${index + 1}.", content = item, style = style)
                        }
                    }
            }
        }
    }
}

@Composable
private fun BulletRow(marker: String, content: String, style: TextStyle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = marker,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = parseInline(content),
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}

private sealed interface MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletList(val items: List<String>) : MarkdownBlock
    data class NumberedList(val items: List<String>) : MarkdownBlock
}

private val BULLET_PREFIX = Regex("""^[-*]\s+(.+)$""")
private val NUMBERED_PREFIX = Regex("""^\d+\.\s+(.+)$""")

private fun parseBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = StringBuilder()
    val bullets = mutableListOf<String>()
    val numbered = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(paragraph.toString().trim()))
            paragraph.clear()
        }
    }
    fun flushBullets() {
        if (bullets.isNotEmpty()) {
            blocks.add(MarkdownBlock.BulletList(bullets.toList()))
            bullets.clear()
        }
    }
    fun flushNumbered() {
        if (numbered.isNotEmpty()) {
            blocks.add(MarkdownBlock.NumberedList(numbered.toList()))
            numbered.clear()
        }
    }

    for (line in markdown.lines()) {
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> {
                flushParagraph(); flushBullets(); flushNumbered()
            }
            BULLET_PREFIX.matchEntire(trimmed) != null -> {
                flushParagraph(); flushNumbered()
                bullets.add(BULLET_PREFIX.matchEntire(trimmed)!!.groupValues[1])
            }
            NUMBERED_PREFIX.matchEntire(trimmed) != null -> {
                flushParagraph(); flushBullets()
                numbered.add(NUMBERED_PREFIX.matchEntire(trimmed)!!.groupValues[1])
            }
            bullets.isNotEmpty() -> {
                bullets[bullets.size - 1] = bullets.last() + " " + trimmed
            }
            numbered.isNotEmpty() -> {
                numbered[numbered.size - 1] = numbered.last() + " " + trimmed
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
            }
        }
    }
    flushParagraph(); flushBullets(); flushNumbered()
    return blocks
}

private fun parseInline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val end = text.indexOf("**", i + 2)
            if (end > i + 2) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(text.substring(i + 2, end))
                pop()
                i = end + 2
                continue
            }
        }
        if (text[i] == '*' && (i + 1 >= text.length || text[i + 1] != '*')) {
            val end = text.indexOf('*', i + 1)
            if (end > i + 1 && (end + 1 >= text.length || text[end + 1] != '*')) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                append(text.substring(i + 1, end))
                pop()
                i = end + 1
                continue
            }
        }
        append(text[i])
        i++
    }
}
