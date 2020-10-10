package kz.q19.scalablebutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kz.q19.animateviewscale.ScalableView
import kz.q19.animateviewscale.ScalableView.Companion.setScalableViewAnimationListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setScalableViewAnimationListener(
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