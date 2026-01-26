package com.example.celestic.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.celestic.R
import com.example.celestic.manager.ImageClassifier
import com.example.celestic.utils.OpenCVInitializer
import com.example.celestic.viewmodel.MainViewModel
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),

) {
    val context = LocalContext.current

    // Executor para análisis de imágenes
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Estado del permiso
    var permissionGranted by remember { mutableStateOf(false) }

    // Strings
    val permissionDeniedMsg = stringResource(R.string.camera_permission_denied)
    val opencvErrorMsg = stringResource(R.string.opencv_init_error)


    // 1. Verificar permisos de cámara
    LaunchedEffect(Unit) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            Log.e("CameraView", permissionDeniedMsg)
        }
    }


    // 2. Inicializar OpenCV
    LaunchedEffect(Unit) {
        val success = OpenCVInitializer.initOpenCV(context)
        if (!success) Log.e("CameraView", opencvErrorMsg)
    }

    // 3. Liberar executor al salir
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    // 4. Si no hay permiso, mostrar mensaje
    if (!permissionGranted) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(permissionDeniedMsg)
        }
        return
    }


    // 5. CONTENEDOR PADRE — SEGURO PARA ANDROIDVIEW
    Box(modifier = modifier.fillMaxSize()) {
        CameraPreviewContainer(
            viewModel = viewModel,
            cameraExecutor = cameraExecutor
        )
    }
}

//  CONTENEDOR SEGURO PARA ANDROIDVIEW (ELIMINA WARNING)
@Composable
private fun CameraPreviewContainer(
    viewModel: MainViewModel,
    cameraExecutor: ExecutorService,
    modifier: Modifier = Modifier
) {
    LocalContext.current

    // AndroidView debe estar SIEMPRE en un contenedor separado
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->

            // Crear PreviewView nativo
            PreviewView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }.also { previewView ->

                // Iniciar cámara
                startCamera(
                    context = ctx,
                    previewView = previewView,
                    cameraExecutor = cameraExecutor,
                    viewModel = viewModel
                )
            }
        }
    )
}


//  INICIO DE LA CÁMARA (CameraX)
private fun startCamera(
    context: Context,
    previewView: PreviewView,
    cameraExecutor: ExecutorService,
    viewModel: MainViewModel,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({

        val cameraProvider = cameraProviderFuture.get()

        // PREVIEW
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }

        // ANÁLISIS DE IMAGEN
        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(224, 224),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                        )
                    )
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) { imageProxy ->

                    try {
                        // Convertir imagen a Bitmap
                        val bitmap = imageProxyToBitmap(imageProxy)

                        // Clasificador
                        val classifier = ImageClassifier(context)
                        val predictions = classifier.runInference(bitmap)
                        val tipo = classifier.mapPredictionToFeatureType(predictions)

                        Log.d("Clasificación", "Resultado: $tipo")

                        // Enviar resultado al ViewModel
                        viewModel.setTipoClasificacion(tipo)

                    } catch (e: Exception) {
                        Log.e("Clasificador", context.getString(R.string.errorInference), e)
                    } finally {
                        imageProxy.close()
                    }
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraView", context.getString(R.string.errorCameraInit), e)
        }

    }, ContextCompat.getMainExecutor(context))
}


//  CONVERTIR ImageProxy → Bitmap (OpenCV)
private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    // Mat YUV
    val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
    yuvMat.put(0, 0, bytes)

    // Convertir a RGB
    val rgbMat = Mat()
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21)

    // Crear Bitmap
    val bmp = createBitmap(rgbMat.cols(), rgbMat.rows())
    Utils.matToBitmap(rgbMat, bmp)

    // Liberar memoria
    yuvMat.release()
    rgbMat.release()

    return bmp
}