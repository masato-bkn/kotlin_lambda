package com.example.service

import org.jraf.klibnotion.client.blocking.BlockingNotionClient
import org.jraf.klibnotion.model.base.UuidString
import org.jraf.klibnotion.model.block.Block
import org.jraf.klibnotion.model.richtext.RichTextList

class NotionService(
    private val apiKey: String,
    private val pageId: String
) {
    private val client = BlockingNotionClient.newInstance(
        authentication = apiKey
    )

    /**
     * Notionページの末尾にテキストブロックを追加
     */
    fun appendTextToPage(text: String) {
        try {
            // テキストブロックを作成
            val paragraph = Block.Paragraph(
                richTextList = RichTextList.fromText(text)
            )

            // ページにブロックを追加
            client.blocks.appendBlockChildren(
                id = UuidString(pageId),
                blocks = listOf(paragraph)
            )
        } catch (e: Exception) {
            throw NotionPostException("Failed to post to Notion: ${e.message}", e)
        }
    }
}

class NotionPostException(message: String, cause: Throwable? = null) : Exception(message, cause)
