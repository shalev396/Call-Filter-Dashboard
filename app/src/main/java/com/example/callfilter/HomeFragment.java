package com.example.callfilter;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class HomeFragment extends Fragment {

    private SwitchMaterial switchCallFilter;
    private ConfigManager configManager;
    private TextView textServiceStatus;
    private View statusIndicatorDot;

    private final ActivityResultLauncher<Intent> requestRoleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1) { // RESULT_OK
            configManager.setEnabled(true);
        } else {
            switchCallFilter.setChecked(false);
            Toast.makeText(getContext(), "Call screening permission is required to enable the filter.", Toast.LENGTH_LONG).show();
        }
        updateStatusIndicator();
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        configManager = new ConfigManager(requireContext());
        switchCallFilter = view.findViewById(R.id.switch_call_filter);
        textServiceStatus = view.findViewById(R.id.text_service_status);
        statusIndicatorDot = view.findViewById(R.id.status_indicator_dot);

        switchCallFilter.setChecked(configManager.isEnabled());

        switchCallFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestRole();
            } else {
                configManager.setEnabled(false);
                updateStatusIndicator();
            }
        });

        updateStatusIndicator();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusIndicator();
    }

    private void requestRole() {
        RoleManager roleManager = requireActivity().getSystemService(RoleManager.class);
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            configManager.setEnabled(true);
            updateStatusIndicator();
        } else {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            requestRoleLauncher.launch(intent);
        }
    }

    private void updateStatusIndicator() {
        RoleManager roleManager = requireActivity().getSystemService(RoleManager.class);
        boolean isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);

        if (isRoleHeld && configManager.isEnabled()) {
            textServiceStatus.setText(R.string.service_active);
            statusIndicatorDot.setActivated(true);
        } else {
            textServiceStatus.setText(R.string.service_inactive);
            statusIndicatorDot.setActivated(false);
        }
    }
}
