package com.shalev396.offdutycallfilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private final Paint windowTextPaint = new Paint();
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

        windowTextPaint.setColor(Color.WHITE);
        windowTextPaint.setTextSize(30f);
        windowTextPaint.setTextAlign(Paint.Align.CENTER);

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
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            // Check if the touch is on a window
            TimeWindow touchedWindow = findWindowAt(x, y);
            if (touchedWindow != null) {
                // Request parent to not intercept touch events (important for NestedScrollView)
                getParent().requestDisallowInterceptTouchEvent(true);
                return true; // Must return true for ACTION_DOWN to receive ACTION_UP
            }
        } else if (action == MotionEvent.ACTION_UP) {
            // Find the window at this position and trigger click
            TimeWindow touchedWindow = findWindowAt(x, y);
            if (touchedWindow != null) {
                if (listener != null) {
                    listener.onWindowClick(touchedWindow);
                }
                performClick();
                return true;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            // Allow parent to intercept again if touch was cancelled
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return super.onTouchEvent(event);
    }

    /**
     * Find the TimeWindow at the given coordinates, or null if none.
     */
    @Nullable
    private TimeWindow findWindowAt(float x, float y) {
        List<List<TimeWindow>> columns = getColumns();
        if (columns.isEmpty()) return null;

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
                    return window;
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
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

                // Draw start and end time
                String startTime = String.format(Locale.getDefault(), "%02d:%02d", window.getStartMinutes() / 60, window.getStartMinutes() % 60);
                String endTime = String.format(Locale.getDefault(), "%02d:%02d", window.getEndMinutes() / 60, window.getEndMinutes() % 60);

                float textHeight = windowTextPaint.descent() - windowTextPaint.ascent();
                float textOffset = (textHeight / 2) - windowTextPaint.descent();

                if (rect.height() > textHeight * 2.5) {
                    canvas.drawText(startTime, rect.centerX(), rect.top + textHeight, windowTextPaint);
                    canvas.drawText(endTime, rect.centerX(), rect.bottom - textOffset - 5, windowTextPaint);
                } else if (rect.height() > textHeight) {
                    canvas.drawText(startTime + " - " + endTime, rect.centerX(), rect.centerY() + textOffset, windowTextPaint);
                }
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
