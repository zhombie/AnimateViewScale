package kz.q19.scalablebutton

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kz.q19.animateviewscale.ScalableView
import kz.q19.animateviewscale.ScalableView.Companion.setScalableViewAnimationListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.textView).setScalableViewAnimationListener(
            params = {
                pushScale = 10F
                pushScaleMode = ScalableView.ScaleMode.DP
            },
            onClickListener = {
                Toast.makeText(this, "${it.id} is clicked", Toast.LENGTH_SHORT).show()
            }
        )
    }

}