package com.example.callfilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TimeSheetView extends View {

    public interface OnWindowClickListener {
        void onWindowClick(TimeWindow window);
    }

    private final Paint hourPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final List<TimeWindow> windows = new ArrayList<>();
    private final List<Paint> windowPaints = new ArrayList<>();
    private final float hourTextHeight;
    private final float hourHeight = 100; // Set a fixed height for each hour
    private final float rightPadding = 100; // Padding for the hour labels
    private OnWindowClickListener listener;
    private final RectF rect = new RectF();

    public TimeSheetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        hourPaint.setColor(ContextCompat.getColor(context, R.color.dark_text_secondary));
        hourPaint.setTextSize(30f);
        hourPaint.setTextAlign(Paint.Align.RIGHT);

        linePaint.setColor(ContextCompat.getColor(context, R.color.dark_card_background));
        linePaint.setStrokeWidth(1f);

        // Colors for overlapping windows
        windowPaints.add(createWindowPaint(ContextCompat.getColor(context, R.color.dark_colorAccent)));
        windowPaints.add(createWindowPaint(ContextCompat.getColor(context, R.color.dark_color_accent_variant)));

        hourTextHeight = hourPaint.descent() - hourPaint.ascent();
    }

    public void setOnWindowClickListener(OnWindowClickListener listener) {
        this.listener = listener;
    }

    private Paint createWindowPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    public void setWindows(List<TimeWindow> windows) {
        this.windows.clear();
        if (windows != null) {
            this.windows.addAll(windows);
        }
        this.windows.sort(Comparator.comparingInt(TimeWindow::getStartMinutes));
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) (24 * hourHeight);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            float x = event.getX();
            float y = event.getY();

            List<List<TimeWindow>> columns = getColumns();
            if (columns.isEmpty()) return false;

            float columnWidth = (getWidth() - rightPadding) / columns.size();
            for (int i = 0; i < columns.size(); i++) {
                List<TimeWindow> column = columns.get(i);
                float left = 10 + i * columnWidth;
                float right = left + columnWidth - 10;

                for (TimeWindow window : column) {
                    float top = (window.getStartMinutes() / 60f) * hourHeight;
                    float bottom = (window.getEndMinutes() / 60f) * hourHeight;
                    rect.set(left, top, right, bottom);
                    if (rect.contains(x, y)) {
                        if (listener != null) {
                            listener.onWindowClick(window);
                        }
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();

        // Draw hour lines and labels every 2 hours
        for (int i = 0; i < 24; i += 2) {
            float y = i * hourHeight;
            canvas.drawLine(0, y, width, y, linePaint);
            canvas.drawText(String.format(Locale.getDefault(), "%02d:00", i), width - 10, y + hourTextHeight / 2, hourPaint);
        }

        List<List<TimeWindow>> columns = getColumns();
        if (columns.isEmpty()) return;

        float columnWidth = (width - rightPadding) / columns.size();
        for (int i = 0; i < columns.size(); i++) {
            List<TimeWindow> column = columns.get(i);
            float left = 10 + i * columnWidth;
            float right = left + columnWidth - 10; // Add some spacing between columns
            Paint paint = windowPaints.get(i % windowPaints.size());

            for (TimeWindow window : column) {
                float top = (window.getStartMinutes() / 60f) * hourHeight;
                float bottom = (window.getEndMinutes() / 60f) * hourHeight;
                rect.set(left, top, right, bottom);
                canvas.drawRect(rect, paint);
            }
        }
    }

    private List<List<TimeWindow>> getColumns() {
        List<List<TimeWindow>> columns = new ArrayList<>();
        for (TimeWindow window : windows) {
            boolean placed = false;
            for (List<TimeWindow> column : columns) {
                if (column.isEmpty() || window.getStartMinutes() >= column.get(column.size() - 1).getEndMinutes()) {
                    column.add(window);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                List<TimeWindow> newColumn = new ArrayList<>();
                newColumn.add(window);
                columns.add(newColumn);
            }
        }
        return columns;
    }
}
