package com.appboy.sample.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LogcatExportUtil {
  companion object {
    private val TAG = this::class.java.simpleName

    /**
     * Our logcat command
     * -d states for the command to completely dump to our buffer, then return
     * -v threadtime sets the output log format. See https://developer.android.com/studio/command-line/logcat.html#outputFormat
     */
    private val LOGCAT_CAPTURE_COMMAND = arrayOf("logcat", "-d", "-v", "threadtime")
    private val LOGCAT_CLEAR_COMMAND = arrayOf("logcat", "-c")
    private const val LOGCAT_EXPORT_DIRECTORY = "logcat_files"

    /**
     * Exports the logcat to a file and returns a FileProvider uri to that created file
     */
    fun exportLogcatToFile(context: Context): Uri {
      // Write the logcat to a file in internal storage
      val currentFormattedDate = getCurrentFormattedDate()
      val logcatExportDirectory = File(context.cacheDir, LOGCAT_EXPORT_DIRECTORY)
      logcatExportDirectory.mkdirs()
      val outputFile = File(logcatExportDirectory, "logcat export - $currentFormattedDate.txt")
      outputFile.writeText(getLogcat())
      return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
    }

    private fun getLogcat(): String {
      // The process id is used to filter the messages. This Test runner and the test itself run in the same process.
      val currentProcessId = android.os.Process.myPid().toString()
      val process = Runtime.getRuntime().exec(LOGCAT_CAPTURE_COMMAND)

      var firstLine = ""
      val stringBuilder = StringBuilder()
      process.inputStream.bufferedReader().forEachLine {
        if (it.contains(currentProcessId)) {
          if (firstLine.isEmpty()) {
            firstLine = it
          }
          stringBuilder.append(it)
          stringBuilder.append('\n')
        }
      }

      // Once the logcat is cleared, the next run of this logcat reader will have a
      // much shorter buffer to deal with, resulting in a faster read time.
      clearLogcat()

      return stringBuilder.toString()
    }

    private fun clearLogcat() {
      try {
        Runtime.getRuntime().exec(LOGCAT_CLEAR_COMMAND)
      } catch (e: IOException) {
        Log.e(TAG, "Failed to close logcat", e)
      }
    }

    private fun getCurrentFormattedDate(): String {
      val simpleDateFormat = SimpleDateFormat("MM-dd kk:mm:ss.SSS", Locale.US)
      simpleDateFormat.timeZone = TimeZone.getDefault()
      return simpleDateFormat.format(Date(System.currentTimeMillis()))
    }
  }
}
