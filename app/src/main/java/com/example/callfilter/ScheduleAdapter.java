package com.example.callfilter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

    private final List<DaySchedule> scheduleList;

    public ScheduleAdapter(List<DaySchedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_day_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        DaySchedule daySchedule = scheduleList.get(position);
        holder.bind(daySchedule);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }
}
