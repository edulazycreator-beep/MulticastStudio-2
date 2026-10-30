package com.multicaststudio.app

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * MultiCast Studio starter screen.
 * This build provides a real CameraX single-source preview and device capability discovery.
 * It does NOT yet composite two cameras or transmit an RTMP stream.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraState: TextView
    private lateinit var capabilityState: TextView
    private lateinit var rearSource: TextView
    private lateinit var frontSource: TextView

    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 410
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 11, 19)
        window.navigationBarColor = Color.rgb(8, 11, 19)
        window.decorView.systemUiVisibility = 0

        setContentView(buildScreen())
        updateSourceSelection()
        updateConcurrentCameraCapability()

        if (hasCameraPermission()) {
            startCameraProvider()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (::previewView.isInitialized && hasCameraPermission() && cameraProvider != null) {
            bindCamera()
        }
    }

    override fun onStop() {
        cameraProvider?.unbindAll()
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraProvider()
            } else {
                cameraState.text = "Camera permission is required for preview"
            }
        }
    }

    private fun buildScreen(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(8, 11, 19), Color.rgb(13, 21, 36), Color.rgb(16, 15, 31))
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brandStack = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brandStack.addView(text("MULTICAST", 17, Color.WHITE, true, 0f))
        brandStack.addView(text("STUDIO  /  MOBILE PRODUCTION", 9, 0xFF9AA9BD.toInt(), true, 0f).apply {
            letterSpacing = 0.12f
        })
        header.addView(brandStack, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(pill("LOCAL PREVIEW", 0xFF99E9F2.toInt()))
        root.addView(header, LinearLayout.LayoutParams(-1, dp(48)))

        root.addView(text("Live production workspace", 13, 0xFF9EABC0.toInt(), false, dp(8)).apply {
            setPadding(0, 0, 0, dp(12))
        })

        val previewCard = FrameLayout(this).apply {
            background = glassDrawable(28, 0xC51A2234.toInt())
            clipToOutline = true
            elevation = dp(4).toFloat()
        }
        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            setBackgroundColor(Color.rgb(5, 8, 14))
        }
        previewCard.addView(
            previewView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val previewTop = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), 0)
        }
        previewTop.addView(pill("CAMERA PREVIEW", Color.WHITE))
        val spacer = View(this)
        previewTop.addView(spacer, LinearLayout.LayoutParams(0, 1, 1f))
        previewTop.addView(pill("NOT STREAMING", 0xFFFFC875.toInt()))
        previewCard.addView(
            previewTop,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42), Gravity.TOP)
        )

        val previewBottom = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), 0, dp(15), dp(14))
            background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(0xD9000000.toInt(), 0x00000000)
            )
        }
        cameraState = text("Waiting for camera…", 12, Color.WHITE, true, 0f)
        previewBottom.addView(cameraState)
        previewCard.addView(
            previewBottom,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60), Gravity.BOTTOM)
        )

        root.addView(
            previewCard,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                weight = 1f
                topMargin = dp(2)
                bottomMargin = dp(16)
            }
        )

        root.addView(text("CAMERA SOURCES", 10, 0xFF8FA0B7.toInt(), true, dp(2)).apply {
            letterSpacing = 0.16f
            setPadding(dp(2), 0, 0, dp(8))
        })

        val sourceRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        rearSource = sourceButton("01", "REAR CAMERA", true)
        frontSource = sourceButton("02", "FRONT CAMERA", false)
        rearSource.setOnClickListener {
            lensFacing = CameraSelector.LENS_FACING_BACK
            updateSourceSelection()
            bindCamera()
        }
        frontSource.setOnClickListener {
            lensFacing = CameraSelector.LENS_FACING_FRONT
            updateSourceSelection()
            bindCamera()
        }
        sourceRow.addView(rearSource, LinearLayout.LayoutParams(0, dp(62), 1f))
        sourceRow.addView(frontSource, LinearLayout.LayoutParams(0, dp(62), 1f).apply {
            leftMargin = dp(10)
        })
        root.addView(sourceRow)

        capabilityState = text("Checking device camera capabilities…", 11, 0xFFB5C1D2.toInt(), false, dp(10))
        capabilityState.setPadding(dp(2), 0, dp(2), dp(12))
        root.addView(capabilityState)

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val switchButton = actionButton("↻   SWITCH SOURCE", false)
        switchButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            updateSourceSelection()
            bindCamera()
        }
        val liveButton = actionButton("GO LIVE  →", true)
        liveButton.setOnClickListener { showStreamingNotice() }
        actionRow.addView(switchButton, LinearLayout.LayoutParams(0, dp(54), 1f))
        actionRow.addView(liveButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            leftMargin = dp(10)
        })
        root.addView(actionRow)

        root.addView(text("STARTER BUILD  •  RTMP ENGINE NOT CONNECTED", 9, 0xFF65758D.toInt(), true, dp(12)).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.09f
        })

        return root
    }

    private fun startCameraProvider() {
        cameraState.text = "Connecting to camera…"
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                bindCamera()
            } catch (error: Exception) {
                cameraState.text = "Camera unavailable: ${error.localizedMessage ?: "unknown error"}"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        if (!hasCameraPermission()) return

        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        val preview = Preview.Builder()
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, selector, preview)
            cameraState.text = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                "SOURCE 01  •  REAR CAMERA ACTIVE"
            } else {
                "SOURCE 02  •  FRONT CAMERA ACTIVE"
            }
        } catch (error: Exception) {
            cameraState.text = "Unable to start this camera: ${error.localizedMessage ?: "unsupported configuration"}"
        }
    }

    private fun updateConcurrentCameraCapability() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraCount = manager.cameraIdList.size
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val hasConcurrentPair = manager.concurrentCameraIds.any { it.size >= 2 }
                capabilityState.text = if (hasConcurrentPair) {
                    "${cameraCount} camera IDs found • device reports concurrent camera support. This starter still previews one source at a time."
                } else {
                    "${cameraCount} camera IDs found • Android does not report a concurrent pair for this device."
                }
            } else {
                capabilityState.text = "$cameraCount camera IDs found • concurrent camera discovery requires Android 11 or newer."
            }
        } catch (error: Exception) {
            capabilityState.text = "Camera capability check unavailable on this device."
        }
    }

    private fun updateSourceSelection() {
        val rearSelected = lensFacing == CameraSelector.LENS_FACING_BACK
        styleSourceButton(rearSource, rearSelected)
        styleSourceButton(frontSource, !rearSelected)
    }

    private fun styleSourceButton(view: TextView, selected: Boolean) {
        view.background = if (selected) {
            glassDrawable(18, 0xD92A5364.toInt(), 0xFF70DCE8.toInt())
        } else {
            glassDrawable(18, 0x9A1B2537.toInt(), 0x3FFFFFFF)
        }
        view.setTextColor(if (selected) Color.WHITE else 0xFF9BAAC0.toInt())
    }

    private fun sourceButton(number: String, label: String, selected: Boolean): TextView {
        return TextView(this).apply {
            text = "$number   $label"
            textSize = 11f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            background = if (selected) glassDrawable(18, 0xD92A5364.toInt(), 0xFF70DCE8.toInt())
            else glassDrawable(18, 0x9A1B2537.toInt(), 0x3FFFFFFF)
            setTextColor(if (selected) Color.WHITE else 0xFF9BAAC0.toInt())
        }
    }

    private fun actionButton(label: String, primary: Boolean): TextView {
        return TextView(this).apply {
            text = label
            textSize = 11f
            letterSpacing = 0.04f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(if (primary) 0xFF07131B.toInt() else Color.WHITE)
            background = if (primary) {
                GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(0xFF85E7F0.toInt(), 0xFF9ABCFB.toInt())
                ).apply { cornerRadius = dp(18).toFloat() }
            } else {
                glassDrawable(18, 0xB5222D43.toInt(), 0x4FFFFFFF)
            }
            elevation = if (primary) dp(5).toFloat() else 0f
        }
    }

    private fun pill(label: String, color: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 9f
            letterSpacing = 0.08f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(color)
            gravity = Gravity.CENTER
            setPadding(dp(11), dp(7), dp(11), dp(7))
            background = glassDrawable(40, 0xA51D293A.toInt(), 0x35FFFFFF)
        }
    }

    private fun text(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean,
        topMargin: Int
    ): TextView = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        typeface = Typeface.create(
            "sans-serif",
            if (bold) Typeface.BOLD else Typeface.NORMAL
        )
        if (topMargin > 0) {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { this.topMargin = topMargin }
        }
    }

    private fun glassDrawable(
        radiusDp: Int,
        fillColor: Int,
        strokeColor: Int = 0x3FFFFFFF
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(radiusDp).toFloat()
        setColor(fillColor)
        setStroke(dp(1), strokeColor)
    }

    private fun showStreamingNotice() {
        AlertDialog.Builder(this)
            .setTitle("Streaming module not connected")
            .setMessage(
                "This is the starter preview build. Camera preview and device capability detection are implemented, " +
                    "but RTMP/RTMPS encoding, audio mixing, scene compositing, and concurrent multi-camera output " +
                    "have not been implemented or tested yet."
            )
            .setPositiveButton("Understood", null)
            .show()
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
