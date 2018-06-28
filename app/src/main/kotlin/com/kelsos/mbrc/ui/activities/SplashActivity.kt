package com.kelsos.mbrc.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelsos.mbrc.NavigationActivity
import com.kelsos.mbrc.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    runBlocking {
      delay(800)

      withContext(Dispatchers.Main) {
        NavigationActivity.start(this@SplashActivity)
      }
    }
  }
}