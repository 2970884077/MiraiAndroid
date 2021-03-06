package io.github.mzdluo123.mirai.android

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger

class AndroidMiraiConsoleUI(context: Context) : MiraiConsoleUI {
    val logStorage = LoopQueue<String>(200)
    val loginSolver = AndroidLoginSolver(context)

    companion object {
        val TAG = AndroidLoginSolver::class.java.name
    }

    override fun createLoginSolver(): LoginSolver {
        return loginSolver
    }

    override fun prePushBot(identity: Long) {
        return
    }

    override fun pushBot(bot: Bot) {
        return
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {
        return
    }

    override fun pushLog(identity: Long, message: String) {
        logStorage.add(message)
        Log.d(TAG, message)

    }

    override fun pushLog(
        priority: SimpleLogger.LogPriority,
        identityStr: String,
        identity: Long,
        message: String
    ) {
        logStorage.add("[${priority}] $message")
        Log.d(TAG, "[${priority}] $message")
    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        logStorage.add("正在启动")
        Log.d(TAG, "正在启动")
    }

    override suspend fun requestInput(hint: String): String {
        return ""
    }

}

class AndroidLoginSolver(private val context: Context) : LoginSolver() {
    lateinit var captcha: CompletableDeferred<String>
    lateinit var captchaData: ByteArray

    companion object {
        const val CAPTCHA_NOTIFICATION_ID = 2
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {

        captcha = CompletableDeferred()
        captchaData = data
        val notifyIntent = Intent(context, CaptchaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder =
            NotificationCompat.Builder(context, BotApplication.CAPTCHA_NOTIFICATION)
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false)
                //禁止滑动删除
                .setOngoing(true)
                //右上角的时间显示
                .setShowWhen(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("本次登录需要验证码")
                .setContentText("点击这里输入验证码")
        NotificationManagerCompat.from(context).apply {
            notify(CAPTCHA_NOTIFICATION_ID,builder.build())
        }
        return captcha.await()
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return ""
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return ""
    }

}

