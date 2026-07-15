package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object Utils {

    fun getClipboardText(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text
            return text?.toString() ?: ""
        }
        return ""
    }

    fun copyToClipboard(context: Context, text: String, label: String = "OTD Feedback") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun openVideoFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist or was deleted.", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No compatible video player found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareVideoFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Video"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing video file.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAppLink(context: Context) {
        try {
            val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "OTD - Download TikTok Videos without Watermark! Get it here: $playStoreUrl")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share OTD App"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun rateApp(context: Context) {
        try {
            val uri = Uri.parse("market://details?id=${context.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun sendFeedbackEmail(context: Context, name: String, email: String, message: String) {
        val subject = "OTD App Feedback from $name"
        val body = "Name: $name\nEmail: $email\n\nFeedback:\n$message"
        val recipient = "triqet@gmail.com"

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send feedback email..."))
        } catch (e: Exception) {
            // Fallback: Copy to clipboard and notify
            copyToClipboard(context, body, "OTD Feedback")
            Toast.makeText(
                context,
                "No email clients found. Feedback copied to clipboard! Please send manually to $recipient",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
