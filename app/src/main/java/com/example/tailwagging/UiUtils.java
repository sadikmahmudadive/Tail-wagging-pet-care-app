package com.example.tailwagging;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class UiUtils {

    public static void animateClick(View view) {
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.95f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.95f);
        scaleXDown.setDuration(100);
        scaleYDown.setDuration(100);

        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.0f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.0f);
        scaleXUp.setDuration(150);
        scaleYUp.setDuration(150);

        AnimatorSet down = new AnimatorSet();
        down.play(scaleXDown).with(scaleYDown);
        
        AnimatorSet up = new AnimatorSet();
        up.play(scaleXUp).with(scaleYUp);
        up.setInterpolator(new DecelerateInterpolator());

        AnimatorSet full = new AnimatorSet();
        full.playSequentially(down, up);
        full.start();
    }

    public static void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(400)
                .setListener(null);
    }
}
