package br.edu.checkpoint.ui.common

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * Image picker with Android Photo Picker (API 33+) and SAF fallback.
 * No runtime storage permission needed.
 */
class ImagePicker(
    fragment: Fragment,
    private val onPicked: (Uri) -> Unit
) {

    // Android 13+ Photo Picker
    private val pickPhotoLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                onPicked(uri)
            } else {
                // user canceled
            }
        }

    // Fallback: SAF Open Document
    private val openDocLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                // Persist read permission across restarts
                fragment.requireContext().contentResolver.takePersistableReadPermission(uri)
                onPicked(uri)
            }
        }

    fun launch() {
        if (Build.VERSION.SDK_INT >= 33) {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            openDocLauncher.launch(arrayOf("image/*"))
        }
    }
}

/** Helper to persist read permission for SAF URIs */
private fun ContentResolver.takePersistableReadPermission(uri: Uri) {
    try {
        takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: SecurityException) {
        // On some devices this may throw if not persistable; safe to ignore.
    }
}



//PARA USAR

//class SpotFormFragment : Fragment(R.layout.fragment_spot_form) {
//
//    private lateinit var imagePicker: ImagePicker
//    private var pickedImageUri: Uri? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        imagePicker = ImagePicker(this) { uri ->
//            pickedImageUri = uri
//            // Atualize a UI (Glide/Coil) e salve no DB como String
//            // imageView.load(uri) com Coil, por exemplo
//        }
//
//        // Exemplo: botão para escolher imagem
//        view.findViewById<View>(R.id.btnPickImage).setOnClickListener {
//            imagePicker.launch()
//        }
//    }
//}

// COMO MOSTRAR

//imageView.load(pickedImageUri)


//Sua entidade já tem imageUri: String?. Então basta converter Uri → String:
//
//val entity = existing.copy(
//    imageUri = pickedImageUri?.toString()
//)
//repository.saveSpot(entity, resolveAddress = false)
//
//
//E, na hora de exibir:
//
//val uri = spot.imageUri?.let(Uri::parse)
//imageView.load(uri)

