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
    private List<String> labels = new ArrayList<>();
    private Paint paintLine;
    private Paint paintPoints;
    private Paint paintGrid;
    private Paint paintText;
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

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.static_text_secondary));
        paintText.setTextSize(24f);
        paintText.setTextAlign(Paint.Align.CENTER);
    }

    public void setScores(List<Integer> newScores) {
        setScores(newScores, null);
    }

    public void setScores(List<Integer> newScores, List<String> newLabels) {
        this.scores = newScores;
        this.labels = newLabels != null ? newLabels : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float bottomPadding = 40f; // space for labels
        float usableHeight = h - bottomPadding;

        // Draw horizontal grid lines
        canvas.drawLine(0, 0, w, 0, paintGrid);
        canvas.drawLine(0, usableHeight / 2f, w, usableHeight / 2f, paintGrid);
        canvas.drawLine(0, usableHeight, w, usableHeight, paintGrid);

        if (scores == null || scores.isEmpty()) {
            canvas.drawLine(0, usableHeight - 8f, w, usableHeight - 8f, paintLine);
            return;
        }

        int pointsCount = scores.size();
        float stepX = pointsCount > 1 ? (float) (w - 20) / (pointsCount - 1) : (w / 2f);
        float startX = pointsCount > 1 ? 10f : (w / 2f);

        path.reset();
        for (int i = 0; i < pointsCount; i++) {
            float x = startX + (i * stepX);
            float y = usableHeight - (scores.get(i) / 100f * usableHeight);
            
            y = Math.max(8f, Math.min(usableHeight - 8f, y));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paintLine);

        // Draw points and labels
        for (int i = 0; i < pointsCount; i++) {
            float x = startX + (i * stepX);
            float y = usableHeight - (scores.get(i) / 100f * usableHeight);
            y = Math.max(8f, Math.min(usableHeight - 8f, y));
            
            canvas.drawCircle(x, y, 10f, paintPoints);

            // Draw label if exists
            if (i < labels.size()) {
                String label = labels.get(i);
                // Truncate label if too long
                if (label.length() > 8) {
                    label = label.substring(0, 6) + "..";
                }
                canvas.drawText(label, x, h - 5f, paintText);
            }
        }
    }
}
