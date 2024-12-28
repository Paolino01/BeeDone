package it.polito.BeeDone

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.BeeDone.profile.UserViewModel
import it.polito.BeeDone.task.TaskViewModel
import it.polito.BeeDone.team.TeamViewModel
import it.polito.BeeDone.ui.theme.Lab05Theme

val userViewModel = UserViewModel()
val taskViewModel = TaskViewModel()
val teamViewModel = TeamViewModel()

class MainActivity : ComponentActivity() {
    lateinit var sharedPreferences: SharedPreferences

    //Creating variables for prefs key, email key and pwd key
    var PREFS_KEY = "prefs"

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permission = arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermissions(permission, 112)
        }

        val db = Firebase.firestore

        setContent {
            sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

            Lab05Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(sharedPreferences, db)
                }
            }
        }

    }

}

