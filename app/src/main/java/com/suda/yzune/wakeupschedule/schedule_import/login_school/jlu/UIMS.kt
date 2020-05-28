package com.suda.yzune.wakeupschedule.schedule_import.login_school.jlu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.suda.yzune.wakeupschedule.schedule_import.exception.NetworkErrorException
import com.suda.yzune.wakeupschedule.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager


class UIMS {

    lateinit var studentId: String
    lateinit var adcId: String
    lateinit var vpnscookie: String
    lateinit var termId: String
    lateinit var courseJSON: JSONObject

    private fun initSSLSocketFactory(): SSLSocketFactory {
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("SSL")
            val xTrustArray = arrayOf(initTrustManager())
            sslContext.init(
                    null,
                    xTrustArray, SecureRandom()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return sslContext!!.socketFactory
    }

    private fun initTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
        }
    }

    private var httpClient: OkHttpClient = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(httpUrl: HttpUrl, list: List<Cookie>) {}

                override fun loadForRequest(httpUrl: HttpUrl): List<Cookie> {
                    return ArrayList()
                }
            })
            .hostnameVerifier { hostname, session -> true }
            .sslSocketFactory(initSSLSocketFactory(), initTrustManager())
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

    lateinit var builder: MultipartBody.Builder
    private val mediaType = MediaType.parse("application/json; charset=utf-8")
    suspend fun getVPNSCookie() {
        builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val request = Request.Builder()
                .url("https://vpns.jlu.edu.cn/login")
                .header("Connection", "close")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .build()

        val response = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute()
        }

        val str = response.headers().get("Set-Cookie")
        vpnscookie = str.toString()
    }

    suspend fun connectToVPNS(user: String, pass: String) {
        val formBody = FormBody.Builder()
                .add("auth_type", "local")
//                vpns账号
                .add("username", "liuwei5518")
//                vpns密码
                .add("password", "2000icespite")
                .add("sms_code", "")
                .build()

        val request = Request.Builder()
                .url("https://vpns.jlu.edu.cn/do-login?local_login=true")
                .header("Cookie", vpnscookie)
                .header("Connection", "close")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .post(formBody)
                .build()
        withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute()
        }
    }

    suspend fun getCheckCode(): Bitmap {
        val request = Request.Builder()
                .url(Address.validCodeAddress)
                .header("Cookie", vpnscookie)
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .get()
                .build()
        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                if (response.body() == null) throw NetworkErrorException("请检查网络连接")
                val verificationCode = response.body()!!.bytes()
                BitmapFactory.decodeByteArray(verificationCode, 0, verificationCode.size)
            } else {
                throw NetworkErrorException("请检查网络连接")
            }
        }
    }

    suspend fun login(user: String, pass: String, code: String) {
        val formBody = FormBody.Builder()
                .add("username", user)
                .add("password", Utils.getMD5Str("UIMS$user$pass"))
                .add("mousePath", "")
                .add("vcode", code)
                .build()

        val request = Request.Builder()
                .url(Address.hostAddress + "/ntms/j_spring_security_check")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .header("Cookie", vpnscookie)
                .header("Connection", "close")
                .header("Referer", Address.hostAddress + "/ntms/userLogin.jsp?reason=nologin")
                .post(formBody)
                .build()
        val response = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute()
        }
    }


    suspend fun getCurrentUserInfo() {
        val formBody = FormBody.Builder().build()
        val request = Request.Builder()
                .url(Address.hostAddress + "/ntms/action/getCurrentUserInfo.do?vpn-12-o2-uims.jlu.edu.cn")
                .header("Referer", Address.hostAddress + "/ntms/index.do")
                .header("Connection", "close")
                .header("Origin", Address.hostAddress)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .header("Cookie", vpnscookie)
                .post(formBody)
                .build()

        val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }
        val bufferedReader = BufferedReader(
                InputStreamReader(response.body()?.byteStream(), "UTF-8"), 8 * 1024)
        val entityStringBuilder = StringBuilder()

        withContext(Dispatchers.IO) {
            var line = bufferedReader.readLine()
            while (line != null) {
                entityStringBuilder.append(line + "\n")
                line = bufferedReader.readLine()
            }
        }

        val obj = JSONObject(entityStringBuilder.toString())
        val defRes = obj.get("defRes") as JSONObject
        studentId = defRes.getString("personId")
        termId = defRes.getString("term_l")
        adcId = defRes.getString("adcId")
    }

    suspend fun getCourseSchedule() {
        val params = JSONObject()
        params.put("termId", termId)
        params.put("studId", studentId)

        val jsonObject = JSONObject()
        jsonObject.put("tag", "teachClassStud@schedule")
        jsonObject.put("branch", "default")
        jsonObject.put("params", params)

        val requestBody = RequestBody.create(mediaType, jsonObject.toString())

        val request = Request.Builder()
                .url(Address.hostAddress + "/ntms/service/res.do?vpn-12-o2-uims.jlu.edu.cn")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .header("Cookie", vpnscookie)
                .header("Host", Address.host)
                .header("Origin", Address.hostAddress)
                .header("Content-Type", "application/json")
                .header("Referer", Address.hostAddress + "/ntms/index.do")
                .post(requestBody)
                .build()
        val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }
        val bufferedReader = BufferedReader(
                InputStreamReader(response.body()!!.byteStream(), "UTF-8"), 8 * 1024)
        val entityStringBuilder = StringBuilder()
        withContext(Dispatchers.IO) {
            var line = bufferedReader.readLine()
            while (line != null) {
                entityStringBuilder.append(line + "\n")
                line = bufferedReader.readLine()
            }
        }
        Log.e("json", entityStringBuilder.toString())
        courseJSON = JSONObject(entityStringBuilder.toString())
    }


}


