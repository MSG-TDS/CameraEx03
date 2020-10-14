package com.example.cameraex03

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

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

        setViews()
    }

    private fun setViews() {
        btnCam.setOnClickListener {
            openCam()
        }
    }

    private fun openCam() {
        if(checkPermission(CAM_PERMISSION, FLAG_CAM_PERM)){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, FLAG_CAM_REQ)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                FLAG_CAM_REQ -> {
                    if(data?.extras?.get("data") != null){
                        val bitMap = data?.extras?.get("data") as Bitmap
                        imageView.setImageBitmap(bitMap)
                    } else {
                        imageView.setImageBitmap(null)
                    }
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