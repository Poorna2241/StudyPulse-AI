package com.yourgroup.studypulseai.ui.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.yourgroup.studypulseai.R;

public class DonutChartView extends View {
    private float strugglingPercent = 0f;
    private float learningPercent = 0f;
    private float masteredPercent = 0f;

    private Paint paintStruggling;
    private Paint paintLearning;
    private Paint paintMastered;
    private Paint paintBackground;

    private RectF rectF = new RectF();

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(0xFFF0F0F0);
        paintBackground.setStyle(Paint.Style.STROKE);
        paintBackground.setStrokeWidth(36f);

        paintStruggling = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStruggling.setColor(ContextCompat.getColor(getContext(), R.color.struggling_red));
        paintStruggling.setStyle(Paint.Style.STROKE);
        paintStruggling.setStrokeWidth(36f);

        paintLearning = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLearning.setColor(ContextCompat.getColor(getContext(), R.color.learning_yellow));
        paintLearning.setStyle(Paint.Style.STROKE);
        paintLearning.setStrokeWidth(36f);

        paintMastered = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMastered.setColor(ContextCompat.getColor(getContext(), R.color.mastered_green));
        paintMastered.setStyle(Paint.Style.STROKE);
        paintMastered.setStrokeWidth(36f);
    }

    public void setData(int struggling, int learning, int mastered) {
        int total = struggling + learning + mastered;
        if (total > 0) {
            strugglingPercent = (float) struggling / total;
            learningPercent = (float) learning / total;
            masteredPercent = (float) mastered / total;
        } else {
            strugglingPercent = 0f;
            learningPercent = 0f;
            masteredPercent = 0f;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - 20f;
        
        rectF.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius);

        // Draw background
        canvas.drawCircle(width / 2f, height / 2f, radius, paintBackground);

        if (strugglingPercent == 0 && learningPercent == 0 && masteredPercent == 0) {
            return;
        }

        float startAngle = -90f;
        
        float strugglingAngle = strugglingPercent * 360f;
        if (strugglingAngle > 0) {
            canvas.drawArc(rectF, startAngle, strugglingAngle, false, paintStruggling);
            startAngle += strugglingAngle;
        }

        float learningAngle = learningPercent * 360f;
        if (learningAngle > 0) {
            canvas.drawArc(rectF, startAngle, learningAngle, false, paintLearning);
            startAngle += learningAngle;
        }

        float masteredAngle = masteredPercent * 360f;
        if (masteredAngle > 0) {
            canvas.drawArc(rectF, startAngle, masteredAngle, false, paintMastered);
        }
    }
}
