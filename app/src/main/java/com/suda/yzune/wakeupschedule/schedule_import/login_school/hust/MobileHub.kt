package com.suda.yzune.wakeupschedule.schedule_import.login_school.hust

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide
import com.suda.yzune.wakeupschedule.schedule_import.exception.NetworkErrorException
import com.suda.yzune.wakeupschedule.schedule_import.exception.PasswordErrorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MobileHub private constructor() {
    companion object {
        @JvmStatic
        fun getInstance(): MobileHub {
            return SingletonHolder.mInstance
        }
    }
    //改为单例模式用来保证cookie一致
    private object SingletonHolder {
        val mInstance: MobileHub = MobileHub()
    }
    private val loginUrl = "https://pass.hust.edu.cn/cas/login?service=http%3A%2F%2Fhub.m.hust.edu.cn%2Fcj%2Findex.jsp"
    private val getScheduleUrl = "http://hub.m.hust.edu.cn/kcb/todate/namecourse.action"
    private val getVerificationCodeUrl = "https://pass.hust.edu.cn/cas/code"

    private val headers = Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
            .add("Origin", "pass.hust.edu.cn")
            .add("Upgrade-Insecure-Requests", "1")
            .build()
    private val regexExecution = "name=\"execution\" value=\"(.+?)\"".toRegex()
    private val regexLt = "name=\"lt\" value=\"(.+?)\"".toRegex()
    private val cookieStore = HashMap<String, List<Cookie>>()

    private var httpClient = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                    cookieStore.put(url.host(), cookies)
                }

                override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                    val cookies = cookieStore[url.host()]

                    return cookies?.toMutableList() ?: ArrayList()
                }
            })
            .build()
    // private lateinit var modulus: String
    private lateinit var execution: String
    private lateinit var lt : String

    lateinit var courseHTML: String

    suspend fun getVerificationCode(): ByteArray? {
        refreshSession()
        return withContext(Dispatchers.IO){
            val request = Request.Builder()
                    .url(getVerificationCodeUrl)
                    .headers(headers)
                    .get()
                    .build()
            val response = httpClient.newCall(request).execute()
            response.body()?.bytes()
        }
    }
    private suspend fun refreshSession() {
        val request = Request.Builder()
                .url(loginUrl)
                .headers(headers)
                .get()
                .build()

        val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }
        val bodyString = withContext(Dispatchers.IO) { response.body()!!.string() }

        var matchResult = regexLt.find(bodyString) ?: throw Exception("页面加载失败")
        lt = matchResult.groupValues.last()

        matchResult = regexExecution.find(bodyString) ?: throw Exception("页面加载失败")
        execution = matchResult.groupValues.last()
    }

    suspend fun login(username: String, password: String,  code: String) {
        val user = username.toUpperCase(Locale.ROOT)

        val cipher = Cipher()

        // 明明是des为啥叫rsa
        val rsa = cipher.encrypt(user + password + lt)

        val formBody = FormBody.Builder()
                .add("rsa", rsa)
                .add("ul", user.length.toString())
                .add("pl", password.length.toString())
                .add("execution", execution)
                .add("code", code)
                .add("lt", lt)
                .add("_eventId", "submit")
                .build()


        val request = Request.Builder()
                .url(loginUrl)
                .headers(headers)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

        val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }

        if (response.request().url().toString().contains("login")) {
            throw PasswordErrorException("学号、密码或验证码错误，请检查后再输入")
        }
    }

    suspend fun getCourseSchedule() {
        val request = Request.Builder()
                .url(getScheduleUrl)
                .headers(headers)
                .get()
                .build()

        val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }

        courseHTML = withContext(Dispatchers.IO) { response.body()!!.string() }

        if (courseHTML.contains("failed to connect")) {
            throw NetworkErrorException("无法访问HUB系统，请检查是否连接校园网")
        }
    }
}