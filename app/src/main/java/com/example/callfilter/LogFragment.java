package com.example.callfilter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogFragment extends Fragment {

    private static final String TAG = "LogFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: LogFragment is being created.");
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ConfigManager configManager = new ConfigManager(requireContext());
        List<BlockedCall> blockedCalls = configManager.getBlockedCalls();

        Log.d(TAG, "onCreateView: Found " + blockedCalls.size() + " blocked calls.");

        LogAdapter adapter = new LogAdapter(blockedCalls, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
}
