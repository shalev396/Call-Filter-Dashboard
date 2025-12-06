package com.shalev396.offdutycallfilter;

import android.app.TimePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements TimeSheetView.OnWindowClickListener {

    private ChipGroup daySelectorChipGroup;
    private TimeSheetView timeSheetView;
    private ConfigManager configManager;
    private List<DaySchedule> schedule;
    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; // Sunday is 0

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        daySelectorChipGroup = view.findViewById(R.id.day_selector_chip_group);
        timeSheetView = view.findViewById(R.id.time_sheet_view);
        configManager = new ConfigManager(requireContext());

        schedule = configManager.getSchedule();

        timeSheetView.setOnWindowClickListener(this);

        setupDaySelector();
        updateScheduleView();

        view.findViewById(R.id.fab_add_schedule).setOnClickListener(v -> showAddTimeWindowDialog());

        return view;
    }

    private void setupDaySelector() {
        daySelectorChipGroup.removeAllViews();
        String[] weekdays = new DateFormatSymbols().getShortWeekdays();
        for (int i = 1; i < weekdays.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(weekdays[i]);

            final int day = (i - 1); // Sunday=0, Monday=1, ..., Saturday=6
            boolean hasSchedule = false;
            for (DaySchedule daySchedule : schedule) {
                if (daySchedule.getDayOfWeek() == day && !daySchedule.getWindows().isEmpty()) {
                    hasSchedule = true;
                    break;
                }
            }

            if (day == selectedDay) {
                chip.setChipBackgroundColorResource(R.color.chip_selected);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                chip.setTypeface(Typeface.DEFAULT_BOLD);
            } else if (hasSchedule) {
                chip.setChipBackgroundColorResource(R.color.chip_has_data);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                chip.setTypeface(Typeface.DEFAULT);
            } else {
                chip.setChipBackgroundColorResource(R.color.chip_default);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text_secondary));
                chip.setTypeface(Typeface.DEFAULT);
            }

            chip.setOnClickListener(v -> {
                selectedDay = day;
                updateScheduleView();
                setupDaySelector(); // Redraw to update selection
            });
            daySelectorChipGroup.addView(chip);
        }
    }

    private void updateScheduleView() {
        DaySchedule currentDaySchedule = findDaySchedule(selectedDay);
        if (currentDaySchedule != null) {
            timeSheetView.setWindows(currentDaySchedule.getWindows());
        } else {
            timeSheetView.setWindows(new ArrayList<>());
        }
    }

    @Override
    public void onWindowClick(TimeWindow window) {
        showEditTimeWindowDialog(window);
    }

    private void showAddTimeWindowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_time_window, null);
        builder.setView(view);

        TextView startTime = view.findViewById(R.id.start_time);
        TextView endTime = view.findViewById(R.id.end_time);
        Button addButton = view.findViewById(R.id.button_add);
        Button cancelButton = view.findViewById(R.id.button_cancel);

        final int[] startMinutes = {0};
        final int[] endMinutes = {0};

        startTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                startMinutes[0] = hourOfDay * 60 + minute;
                startTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        endTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                endMinutes[0] = hourOfDay * 60 + minute;
                endTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        AlertDialog dialog = builder.create();

        addButton.setOnClickListener(v -> {
            TimeWindow newWindow = new TimeWindow(startMinutes[0], endMinutes[0]);

            DaySchedule daySchedule = findDaySchedule(selectedDay);
            if (daySchedule == null) {
                daySchedule = new DaySchedule(selectedDay, new ArrayList<>());
                schedule.add(daySchedule);
            }
            daySchedule.getWindows().add(newWindow);
            configManager.setSchedule(schedule);
            updateScheduleView();
            setupDaySelector();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditTimeWindowDialog(TimeWindow window) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_time_window, null);
        builder.setView(view);

        TextView startTime = view.findViewById(R.id.start_time);
        TextView endTime = view.findViewById(R.id.end_time);
        Button deleteButton = view.findViewById(R.id.button_delete);
        Button saveButton = view.findViewById(R.id.button_save);
        Button cancelButton = view.findViewById(R.id.button_cancel);

        final int[] startMinutes = {window.getStartMinutes()};
        final int[] endMinutes = {window.getEndMinutes()};

        startTime.setText(String.format(Locale.getDefault(), "%02d:%02d", window.getStartMinutes() / 60, window.getStartMinutes() % 60));
        endTime.setText(String.format(Locale.getDefault(), "%02d:%02d", window.getEndMinutes() / 60, window.getEndMinutes() % 60));

        startTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                startMinutes[0] = hourOfDay * 60 + minute;
                startTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        endTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                endMinutes[0] = hourOfDay * 60 + minute;
                endTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        AlertDialog dialog = builder.create();

        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark)
                .setTitle("Delete Time Window")
                .setMessage("Are you sure you want to delete this time window?")
                .setPositiveButton("Yes", (d, w) -> {
                    DaySchedule daySchedule = findDaySchedule(selectedDay);
                    if (daySchedule != null) {
                        daySchedule.getWindows().remove(window);
                        configManager.setSchedule(schedule);
                        updateScheduleView();
                        setupDaySelector();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("No", null)
                .show());

        saveButton.setOnClickListener(v -> {
            window.setStartMinutes(startMinutes[0]);
            window.setEndMinutes(endMinutes[0]);
            configManager.setSchedule(schedule);
            updateScheduleView();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private DaySchedule findDaySchedule(int day) {
        for (DaySchedule daySchedule : schedule) {
            if (daySchedule.getDayOfWeek() == day) {
                return daySchedule;
            }
        }
        return null;
    }
}
