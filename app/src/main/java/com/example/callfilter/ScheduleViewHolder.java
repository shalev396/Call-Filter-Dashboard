package com.example.callfilter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class ScheduleViewHolder extends RecyclerView.ViewHolder {

    private final TextView textDayOfWeek;
    private final ChipGroup chipGroupTimeWindows;
    private final Button buttonAddTimeWindow;

    public ScheduleViewHolder(@NonNull View itemView) {
        super(itemView);
        textDayOfWeek = itemView.findViewById(R.id.text_day_of_week);
        chipGroupTimeWindows = itemView.findViewById(R.id.chip_group_time_windows);
        buttonAddTimeWindow = itemView.findViewById(R.id.button_add_time_window);
    }

    public void bind(DaySchedule daySchedule) {
        textDayOfWeek.setText(new DateFormatSymbols().getWeekdays()[daySchedule.getDayOfWeek() + 1]);

        chipGroupTimeWindows.removeAllViews();
        for (TimeWindow window : daySchedule.getWindows()) {
            Chip chip = new Chip(itemView.getContext());
            chip.setText(String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d",
                    window.getStartMinutes() / 60, window.getStartMinutes() % 60,
                    window.getEndMinutes() / 60, window.getEndMinutes() % 60));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                // TODO: Remove time window
                Toast.makeText(itemView.getContext(), "Remove time window", Toast.LENGTH_SHORT).show();
            });
            chipGroupTimeWindows.addView(chip);
        }

        buttonAddTimeWindow.setOnClickListener(v -> {
            // TODO: Show time picker
            Toast.makeText(itemView.getContext(), "Add time window", Toast.LENGTH_SHORT).show();
        });
    }
}
