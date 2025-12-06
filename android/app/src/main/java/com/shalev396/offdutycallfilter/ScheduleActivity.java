package com.shalev396.offdutycallfilter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_schedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for now
        List<DaySchedule> scheduleList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            scheduleList.add(new DaySchedule(i, new ArrayList<>()));
        }

        ScheduleAdapter adapter = new ScheduleAdapter(scheduleList);
        recyclerView.setAdapter(adapter);
    }
}
