package no.byteme.magnuspoppe.eksamen;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class AnimateWeightChange extends Animation
{
    float initialWeight;
    float targetWeight;
    boolean grow = false;
    LinearLayout layout;

    public AnimateWeightChange(LinearLayout linearLayout, float initialWeight, float targetWeight)
    {
        this.layout = linearLayout;
        this.initialWeight = initialWeight;
        this.targetWeight = targetWeight;
        grow = this.initialWeight > targetWeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();

        if (grow)
            params.weight = initialWeight + (targetWeight * interpolatedTime);
        else
            params.weight = initialWeight + (targetWeight * interpolatedTime);

        layout.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds()
    {
        return true;
    }
}
