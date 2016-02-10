package fr.nantes.iut.ruvcom.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import fr.nantes.iut.ruvcom.R;

public class FullScreenImageActivity extends AppCompatActivity {

    private final ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        setContentView(R.layout.activity_full_screen_image);

        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);

        imageLoader.displayImage(imageUrl, fullScreenImageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            int color = Color.BLACK;

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

}
