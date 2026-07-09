package com.yourgroup.studypulseai.ui.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.yourgroup.studypulseai.R;
import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {
    private List<Integer> scores = new ArrayList<>();
    private Paint paintLine;
    private Paint paintPoints;
    private Paint paintGrid;
    private Path path = new Path();

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(6f);

        paintPoints = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPoints.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        paintPoints.setStyle(Paint.Style.FILL);

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setColor(0x22000000);
        paintGrid.setStrokeWidth(2f);
    }

    public void setScores(List<Integer> newScores) {
        this.scores = newScores;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        // Draw horizontal grid lines
        canvas.drawLine(0, 0, w, 0, paintGrid);
        canvas.drawLine(0, h / 2f, w, h / 2f, paintGrid);
        canvas.drawLine(0, h, w, h, paintGrid);

        if (scores == null || scores.isEmpty()) {
            // Draw a flat baseline
            canvas.drawLine(0, h - 8f, w, h - 8f, paintLine);
            return;
        }

        int pointsCount = scores.size();
        float stepX = pointsCount > 1 ? (float) w / (pointsCount - 1) : w;

        path.reset();
        for (int i = 0; i < pointsCount; i++) {
            float x = i * stepX;
            // score is 0-100, map to height (invert Y)
            float y = h - (scores.get(i) / 100f * h);
            
            // Limit bounds slightly so points aren't cut off
            y = Math.max(8f, Math.min(h - 8f, y));
            x = Math.max(8f, Math.min(w - 8f, x));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paintLine);

        // Draw points on top
        for (int i = 0; i < pointsCount; i++) {
            float x = i * stepX;
            float y = h - (scores.get(i) / 100f * h);
            y = Math.max(8f, Math.min(h - 8f, y));
            x = Math.max(8f, Math.min(w - 8f, x));
            canvas.drawCircle(x, y, 10f, paintPoints);
        }
    }
}
