package ai.lor.app

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val localServerUrl = "http://10.0.2.2:8787/generate"
    private val lime = Color.rgb(216, 244, 108)
    private val background = Color.rgb(17, 19, 17)
    private val panel = Color.rgb(28, 33, 28)
    private val muted = Color.rgb(157, 163, 152)
    private lateinit var prompt: EditText
    private lateinit var generate: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        setContentView(buildScreen())
    }

    private fun buildScreen(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 25)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val logo = label("Lor ", 24, Color.WHITE).apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        val ai = label("Ai", 24, lime).apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        header.addView(logo)
        header.addView(ai)
        val notification = actionButton("♢")
        header.addView(notification, LinearLayout.LayoutParams(48, 48).apply { gravity = Gravity.END })
        notification.setOnClickListener { toast("لا توجد إشعارات جديدة") }
        content.addView(header, matchWrap())

        val greeting = label("مساء الخير، لينا", 13, muted)
        greeting.setPadding(0, 30, 0, 4)
        content.addView(greeting, matchWrap())
        content.addView(label("اصنع شيئًا يُرى.", 32, Color.WHITE).apply {
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 3)
        }, matchWrap())
        content.addView(label("أفكار جديدة، تتحول إلى مشاهد لا تُنسى.", 14, muted), matchWrap())

        val studio = card()
        studio.addView(label("Lor5.7 prime  •  متاح", 12, lime), matchWrap())
        studio.addView(label("ما الذي تريد أن تراه اليوم؟", 23, Color.WHITE).apply { setPadding(0, 17, 0, 8) }, matchWrap())
        studio.addView(label("اكتب فكرتك، وسيتولى Lor تحويلها إلى فيديو سينمائي مدته 20 ثانية.", 12, muted), matchWrap())
        prompt = EditText(this).apply {
            hint = "صف المشهد الذي تتخيله..."
            hintTextColor = Color.rgb(105, 115, 103)
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.TOP or Gravity.RIGHT
            minLines = 4
            maxLines = 5
            setPadding(14, 14, 14, 14)
            background = rounded(Color.rgb(19, 26, 19), Color.rgb(58, 71, 54), 10)
        }
        studio.addView(prompt, LinearLayout.LayoutParams(-1, 125).apply { topMargin = 18 })
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
        val landscape = choiceButton("16:9", true)
        val portrait = choiceButton("9:16", false)
        landscape.setOnClickListener { chooseRatio(landscape, portrait) }
        portrait.setOnClickListener { chooseRatio(portrait, landscape) }
        controls.addView(landscape, LinearLayout.LayoutParams(0, 43, 1f).apply { marginEnd = 6 })
        controls.addView(portrait, LinearLayout.LayoutParams(0, 43, 1f).apply { marginEnd = 6 })
        generate = Button(this).apply {
            text = "توليد 20 ثانية"
            textSize = 12f
            setTextColor(Color.rgb(24, 32, 20))
            background = rounded(lime, lime, 9)
            isAllCaps = false
        }
        controls.addView(generate, LinearLayout.LayoutParams(0, 43, 1.65f))
        studio.addView(controls, LinearLayout.LayoutParams(-1, 43).apply { topMargin = 12 })
        progress = ProgressBar(this).apply { visibility = View.GONE; indeterminateTintList = android.content.res.ColorStateList.valueOf(lime) }
        studio.addView(progress, LinearLayout.LayoutParams(-2, 28).apply { gravity = Gravity.CENTER; topMargin = 6 })
        generate.setOnClickListener { startGeneration() }
        content.addView(studio, matchWrap().apply { topMargin = 26 })

        val feedTitle = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        feedTitle.addView(label("يحدث الآن", 20, Color.WHITE), LinearLayout.LayoutParams(0, -2, 1f))
        feedTitle.addView(label("مباشر  ●", 11, lime))
        content.addView(feedTitle, matchWrap().apply { topMargin = 30 })
        content.addView(label("موجز مختار من صناع Lor حول العالم", 11, muted).apply { setPadding(0, 3, 0, 13) }, matchWrap())
        addVideoCard(content, "نورة السبيعي", "الرياض، السعودية", "ليل الرياض حين يلتقي الضوء بالسماء.", "2.4k")
        addVideoCard(content, "عمر يصنع", "دبي، الإمارات", "مستقبل المواصلات كما تخيلناه في 2045.", "1.8k")
        content.addView(label("✦  مساحة إبداع آمنة", 12, lime).apply { setPadding(14, 16, 14, 16); background = rounded(Color.rgb(26, 36, 26), Color.rgb(50, 69, 46), 10) }, matchWrap().apply { topMargin = 15 })

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNavigation(), LinearLayout.LayoutParams(-1, 68))
        return root
    }

    private fun addVideoCard(parent: LinearLayout, creator: String, location: String, description: String, likes: String) {
        val item = card()
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(label("●", 20, lime), LinearLayout.LayoutParams(35, 40))
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        details.addView(label(creator, 12, Color.WHITE))
        details.addView(label("$location  ·  قبل دقائق", 10, muted))
        top.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(label("•••", 14, muted))
        item.addView(top, matchWrap())
        item.addView(label("▶  فيديو 20 ثانية", 12, Color.rgb(220, 227, 216)).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(42, 52, 40), Color.rgb(62, 78, 56), 10)
        }, LinearLayout.LayoutParams(-1, 145).apply { topMargin = 12 })
        item.addView(label(description, 12, Color.rgb(194, 200, 187)).apply { setPadding(0, 11, 0, 8) }, matchWrap())
        val like = Button(this).apply { text = "♡  $likes     ◌  86     ↗ مشاركة"; textSize = 11f; gravity = Gravity.RIGHT; setTextColor(muted); background = null; isAllCaps = false }
        like.setOnClickListener { like.text = "♥  ${likes.replace("k", "") .toDoubleOrNull()?.plus(.1)?.toString()?.take(3) ?: likes}k     ◌  86     ↗ مشاركة"; like.setTextColor(Color.rgb(244, 138, 134)) }
        item.addView(like, matchWrap())
        parent.addView(item, matchWrap().apply { topMargin = 12 })
    }

    private fun bottomNavigation(): View = LinearLayout(this).apply {
        gravity = Gravity.CENTER
        setBackgroundColor(Color.rgb(25, 29, 25))
        val items = listOf("⌂\nالرئيسية", "✦\nإنشاء", "♡\nالمحفوظات", "◉\nحسابي")
        items.forEachIndexed { index, text ->
            val item = TextView(this@MainActivity).apply { this.text = text; gravity = Gravity.CENTER; textSize = 11f; setTextColor(if (index == 0) lime else muted); setPadding(8, 4, 8, 4) }
            addView(item, LinearLayout.LayoutParams(0, -1, 1f))
            item.setOnClickListener { toast(text.substringAfter('\n')) }
        }
    }

    private fun startGeneration() {
        if (prompt.text.toString().trim().isEmpty()) { prompt.error = "اكتب وصف الفيديو أولًا"; return }
        generate.isEnabled = false
        progress.visibility = View.VISIBLE
        generate.text = "Lor يبني المشهد..."
        thread {
            val response = requestLocalGeneration()
            runOnUiThread {
                progress.visibility = View.GONE
                generate.isEnabled = true
                generate.text = "توليد 20 ثانية"
                if (response.first) {
                    toast("تم حجز التوليد محليًا · المتبقي اليوم: ${response.second}")
                } else {
                    toast(response.third)
                }
            }
        }
    }

    private fun requestLocalGeneration(): Triple<Boolean, Int, String> {
        return try {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown-device"
            val body = JSONObject().apply {
                put("device_id", deviceId)
                put("account_id", "lina-demo")
                put("prompt", prompt.text.toString().trim())
                put("duration", 20)
            }
            val connection = URL(localServerUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 2500
            connection.readTimeout = 5000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val responseText = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: "{}"
            val response = JSONObject(responseText)
            val accepted = connection.responseCode in 200..299
            Triple(accepted, response.optInt("remaining", 0), response.optString("error", "تعذر الاتصال بخادم الماك"))
        } catch (_: Exception) {
            Triple(false, 0, "تعذر الاتصال بخادم الماك. تأكد أنه يعمل وعلى نفس الشبكة.")
        }
    }

    private fun chooseRatio(selected: Button, other: Button) { selected.setTextColor(Color.rgb(24, 32, 20)); selected.background = rounded(lime, lime, 8); other.setTextColor(muted); other.background = rounded(Color.rgb(35, 44, 34), Color.rgb(52, 64, 47), 8) }
    private fun choiceButton(text: String, active: Boolean) = Button(this).apply { this.text = text; textSize = 11f; isAllCaps = false; setTextColor(if (active) Color.rgb(24, 32, 20) else muted); background = rounded(if (active) lime else Color.rgb(35, 44, 34), if (active) lime else Color.rgb(52, 64, 47), 8) }
    private fun card() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(15, 15, 15, 13); background = rounded(panel, Color.rgb(47, 55, 45), 14); layoutDirection = View.LAYOUT_DIRECTION_RTL }
    private fun label(text: String, size: Int, color: Int) = TextView(this).apply { this.text = text; textSize = size.toFloat(); setTextColor(color); gravity = Gravity.RIGHT; layoutDirection = View.LAYOUT_DIRECTION_RTL }
    private fun actionButton(text: String) = Button(this).apply { this.text = text; textSize = 22f; setTextColor(Color.WHITE); background = null; isAllCaps = false }
    private fun rounded(fill: Int, stroke: Int, radius: Int) = GradientDrawable().apply { setColor(fill); setStroke(1, stroke); cornerRadius = radius.toFloat() }
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
