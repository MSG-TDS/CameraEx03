package com.example.cameraex03

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    val CAM_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val FLAG_CAM_PERM = 10
    val FLAG_STOR_PERM = 20

    val FLAG_CAM_REQ = 100
    val FLAG_STOR_REQ = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(checkPermission(STORAGE_PERMISSION, FLAG_STOR_PERM)) {
            setViews()
        }
    }

    private fun setViews() {
        btnCam.setOnClickListener {
            openCam()
        }
        btnGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, FLAG_STOR_REQ)
    }

    private fun openCam() {
        if(checkPermission(CAM_PERMISSION, FLAG_CAM_PERM)){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, FLAG_CAM_REQ)
        }
    }

    private fun saveImageFile(filename: String, mimeType: String, bitmap: Bitmap) : Uri? {
        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if(uri != null) {
            var descriptor = contentResolver.openFileDescriptor(uri, "w")

            if (descriptor != null) {
                val fos = FileOutputStream(descriptor.fileDescriptor)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri!!, values, null, null)
                }
            }
        }

        return uri
    }

    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.jpg"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                FLAG_CAM_REQ -> {
                    if(data?.extras?.get("data") != null){
                        val bitMap = data?.extras?.get("data") as Bitmap
                        val uri = saveImageFile(newFileName(), "image/jpg", bitMap)

                        imageView.setImageURI(uri)
                    } else {
                        imageView.setImageBitmap(null)
                    }
                }
                FLAG_STOR_REQ -> {
                    val uri = data?.data
                    imageView.setImageURI(uri)
                }
            }
        }
        else {
            imageView.setImageBitmap(null)
        }
    }

    fun checkPermission(permissions: Array<out String>, flag : Int ) : Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(perm in permissions){
                if(ActivityCompat.checkSelfPermission( this, perm) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissions, flag)
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            FLAG_CAM_PERM -> {
                for(g in grantResults){
                    if(g != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this,"카메라 권한 필요", Toast.LENGTH_LONG)
                        return
                    }
                }
            }
            FLAG_STOR_PERM -> {
                for(g in grantResults){
                    if(g != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this,"스토리지 권한 필요", Toast.LENGTH_LONG)
                        return
                    }
                }
                setViews()
            }
        }
    }
}