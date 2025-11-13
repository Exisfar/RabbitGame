package com.example.rabbitgame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.rabbitgame.models.Carrot
import com.example.rabbitgame.models.GameState
import com.example.rabbitgame.models.Rabbit
import kotlin.random.Random
import kotlin.math.max

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var rabbit: Rabbit
    private val carrots = mutableListOf<Carrot>()
    private val gameState = GameState()

    // 图片资源
    private lateinit var rabbitBitmap: Bitmap
    private lateinit var carrotBitmap: Bitmap
    private lateinit var backgroundBitmap: Bitmap

    // 画笔
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val gameOverPaint = Paint().apply {
        color = Color.parseColor("#FF6B6B")
        textSize = 64f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }

    // 游戏参数
    private var lastCarrotTime = 0L
    private val carrotInterval = 1000L // 1秒生成一个胡萝卜
    private var screenWidth = 0
    private var screenHeight = 0

    // 触摸控制区域
    private var leftTouchArea: Rect? = null
    private var rightTouchArea: Rect? = null

    init {
        initGame()
    }

    private fun initGame() {
        // 创建简单的位图（实际项目中应该加载图片资源）
        createBitmaps()
        gameState.reset()
    }

    private fun createBitmaps() {
        // 1. 创建更漂亮的兔子位图
        rabbitBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val rabbitCanvas = Canvas(rabbitBitmap)

        // 兔子身体（灰色圆形）
        val rabbitBodyPaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        rabbitCanvas.drawCircle(60f, 60f, 50f, rabbitBodyPaint)

        // 兔子耳朵
        val rabbitEarPaint = Paint().apply {
            color = Color.parseColor("#999999")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // 左耳
        rabbitCanvas.drawOval(RectF(30f, 5f, 50f, 40f), rabbitEarPaint)
        // 右耳
        rabbitCanvas.drawOval(RectF(70f, 5f, 90f, 40f), rabbitEarPaint)

        // 兔子眼睛
        val rabbitEyePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        rabbitCanvas.drawCircle(45f, 55f, 5f, rabbitEyePaint)
        rabbitCanvas.drawCircle(75f, 55f, 5f, rabbitEyePaint)

        // 兔子鼻子（粉色三角形）
        val rabbitNosePaint = Paint().apply {
            color = Color.parseColor("#FFB6C1")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val nosePath = Path()
        nosePath.moveTo(55f, 65f)
        nosePath.lineTo(65f, 65f)
        nosePath.lineTo(60f, 75f)
        nosePath.close()
        rabbitCanvas.drawPath(nosePath, rabbitNosePaint)

        // 2. 创建更漂亮的胡萝卜位图
        carrotBitmap = Bitmap.createBitmap(80, 120, Bitmap.Config.ARGB_8888)
        val carrotCanvas = Canvas(carrotBitmap)

        // 胡萝卜主体（橙色椭圆）
        val carrotBodyPaint = Paint().apply {
            color = Color.parseColor("#FF8C00")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        carrotCanvas.drawOval(RectF(20f, 40f, 60f, 110f), carrotBodyPaint)

        // 胡萝卜纹理（深橙色线条）
        val carrotLinePaint = Paint().apply {
            color = Color.parseColor("#FF4500")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        carrotCanvas.drawLine(40f, 50f, 40f, 100f, carrotLinePaint)
        carrotCanvas.drawLine(30f, 60f, 30f, 90f, carrotLinePaint)
        carrotCanvas.drawLine(50f, 60f, 50f, 90f, carrotLinePaint)

        // 胡萝卜叶子（绿色）
        val carrotLeafPaint = Paint().apply {
            color = Color.parseColor("#32CD32")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // 左叶子
        carrotCanvas.drawOval(RectF(25f, 20f, 45f, 50f), carrotLeafPaint)
        // 右叶子
        carrotCanvas.drawOval(RectF(35f, 15f, 55f, 45f), carrotLeafPaint)

        // 3. 创建更好的背景（渐变背景）
        backgroundBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val bgCanvas = Canvas(backgroundBitmap)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 10f, 10f,
                Color.parseColor("#87CEEB"),
                Color.parseColor("#E0F7FF"),
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        bgCanvas.drawRect(0f, 0f, 10f, 10f, bgPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        // 初始化兔子位置（底部中央）- 使用新尺寸
        val rabbitWidth = 120  // 更新为120
        val rabbitHeight = 120 // 更新为120
        val rabbitX = (w - rabbitWidth) / 2f
        val rabbitY = h - rabbitHeight - 80f  // 离底部更远一些

        rabbit = Rabbit(rabbitX, rabbitY, rabbitWidth, rabbitHeight, rabbitBitmap)

        // 设置触摸控制区域
        val touchAreaHeight = h / 4
        leftTouchArea = Rect(0, h - touchAreaHeight, w / 2, h)
        rightTouchArea = Rect(w / 2, h - touchAreaHeight, w, h)

        // 清空现有的胡萝卜
        carrots.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制渐变背景（平铺）
        val bgShader =
            BitmapShader(backgroundBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        val bgPaint = Paint().apply {
            shader = bgShader
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 绘制草地
        val grassPaint = Paint().apply {
            color = Color.parseColor("#7CFC00")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, height - 100f, width.toFloat(), height.toFloat(), grassPaint)

        // 绘制兔子
        rabbit.draw(canvas)

        // 绘制胡萝卜
        carrots.forEach { it.draw(canvas) }

        // 绘制分数和生命值（带背景）
        val textBgPaint = Paint().apply {
            color = Color.parseColor("#80000000") // 半透明黑色背景
            style = Paint.Style.FILL
        }

        // 分数背景
        canvas.drawRect(10f, 10f, 250f, 80f, textBgPaint)
        // 生命值背景
        canvas.drawRect(width - 240f, 10f, width - 10f, 80f, textBgPaint)

        // 绘制文字
        canvas.drawText("分数: ${gameState.score}", 30f, 50f, textPaint)
        canvas.drawText("生命: ${gameState.lives}", width - 220f, 50f, textPaint)

        // 游戏结束显示
        if (gameState.isGameOver) {
            val gameOverBgPaint = Paint().apply {
                color = Color.parseColor("#CC000000")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gameOverBgPaint)

            canvas.drawText("小小兔挑战失败了（ つД ｀）", width / 2f, height / 2f - 50f, gameOverPaint)
            canvas.drawText(
                "最终分数: ${gameState.score}",
                width / 2f,
                height / 2f + 20f,
                textPaint.apply {
                    textAlign = Paint.Align.CENTER
                    color = Color.YELLOW
                })
            canvas.drawText("点击屏幕再来一次吧~", width / 2f, height / 2f + 90f, textPaint.apply {
                textAlign = Paint.Align.CENTER
                color = Color.WHITE
                textSize = 40f
            })
        }
    }

    fun update() {
        if (gameState.isGameOver || gameState.isPaused) return

        // 更新兔子位置
        rabbit.update(screenWidth)

        // 生成新胡萝卜（带随机大小）
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCarrotTime > carrotInterval) {
            val carrotSize = (40 + Random.nextInt(40)) // 随机大小 40-80
            val carrotX = Random.nextInt(0, screenWidth - carrotSize).toFloat()
            val carrot = Carrot(
                carrotX,
                -carrotSize.toFloat(),
                carrotSize,
                (carrotSize * 1.5).toInt(),
                carrotBitmap,
                speed = 6f + Random.nextFloat() * 4f // 随机速度
            )
            carrots.add(carrot)
            lastCarrotTime = currentTime
        }

        // 更新胡萝卜位置并检测碰撞
        val iterator = carrots.iterator()
        while (iterator.hasNext()) {
            val carrot = iterator.next()
            if (carrot.update(screenHeight)) {
                if (carrot.checkCollision(rabbit.bounds)) {
                    gameState.addScore(10)
                    carrot.isActive = false
                    iterator.remove()
                }
            } else {
                // 胡萝卜落到屏幕外，扣生命值
                gameState.loseLife()
                iterator.remove()
            }
        }

        // 限制胡萝卜数量
        if (carrots.size > 10) {
            carrots.removeAll { !it.isActive }
        }

        invalidate() // 请求重绘
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                leftTouchArea?.let {
                    rabbit.isMovingLeft = it.contains(x.toInt(), y.toInt())
                }
                rightTouchArea?.let {
                    rabbit.isMovingRight = it.contains(x.toInt(), y.toInt())
                }

                // 游戏结束时的重新开始逻辑
                if (gameState.isGameOver && y < screenHeight * 0.8f) {
                    restartGame()
                }
            }

            MotionEvent.ACTION_UP -> {
                rabbit.isMovingLeft = false
                rabbit.isMovingRight = false
            }
        }
        return true
    }

    fun restartGame() {
        carrots.clear()
        gameState.reset()
        lastCarrotTime = System.currentTimeMillis()

        // 重置兔子位置
        rabbit.x = (screenWidth - rabbit.width) / 2f
        rabbit.y = screenHeight - rabbit.height - 50f
    }

    fun pauseGame() {
        gameState.isPaused = true
    }

    fun resumeGame() {
        gameState.isPaused = false
        lastCarrotTime = System.currentTimeMillis()
    }
}