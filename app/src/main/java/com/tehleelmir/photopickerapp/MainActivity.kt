package com.tehleelmir.photopickerapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.tehleelmir.photopickerapp.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val CAMERA_PERMISSION_CODE  = 10
    private val FILE_PERMISSION_CODE    = 100
    private val OPEN_CAMERA             = 23
    private val OPEN_GALLERY            = 32
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        checkForCameraPermission()
        checkFileWritePermission()
        initListener()
    }

    private fun initListener() {
        val listener = View.OnClickListener {
            if(it.id == binding.cameraBtn.id) {
                openCamera()
            }
            else {
                openGallery()
            }
        }
        binding.apply {
            cameraBtn.setOnClickListener(listener)
            galleryBtn.setOnClickListener(listener)
        }
    }

    private fun checkForCameraPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun checkFileWritePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), FILE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "You won't abe to use the camera until Camera permission is granted", Toast.LENGTH_LONG).show()
        }

        if(requestCode == FILE_PERMISSION_CODE) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "You won't be able to upload any file from the gallery", Toast.LENGTH_LONG).show()
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("here22", ex.toString())
                    null
                }

                photoFile?.also {

                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    try {
                    startActivityForResult(takePictureIntent, OPEN_CAMERA)
                    }
                    catch (e: Exception) {
                        Log.e("here22", e.toString())
                    }
                }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, OPEN_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_CAMERA)
            if(resultCode == RESULT_OK && data != null) {
                openCropImageView(Uri.fromFile(File(currentPhotoPath)))
            }

        if(requestCode == OPEN_GALLERY)
            if(resultCode == RESULT_OK && data != null) {
                val image: Uri = data.data!!
                openCropImageView(image)
            }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val uri = UCrop.getOutput(data!!)
            val imgFile = File(uri?.path)

            if (imgFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                binding.previewImage.setImageBitmap(myBitmap)
            }
        }
    }

    private fun openCropImageView(imageUri: Uri) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${Calendar.getInstance().timeInMillis} image")
        val destinationUri = Uri.fromFile(file)

        UCrop.of(imageUri, destinationUri)
            .withAspectRatio(0f, 0f)
            .withMaxResultSize(2000, 2000)
            .start(this)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

}









































