package com.example.smarttasks

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val desc = inputData.getString("desc") ?: ""
        val email = inputData.getString("email") ?: return Result.failure()
        val dueTime = inputData.getLong("dueTime", System.currentTimeMillis())

        // Format due time for notification/email
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val formattedTime = sdf.format(Date(dueTime))
        val fullDesc = "$desc\nScheduled for: $formattedTime"

        try {
            // Send email via Gmail
            val props = java.util.Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        "nonzuzoshange12@gmail.com",  // your email
                        "cnmp gmif diio wsuc"        // app password
                    )
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress("nonzuzoshange12@gmail.com"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                subject = "Task Reminder: $title"
                setText(fullDesc)
            }

            Transport.send(message)

            // Show notification
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(applicationContext, "task_channel")
                .setContentTitle(title)
                .setContentText(fullDesc)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
