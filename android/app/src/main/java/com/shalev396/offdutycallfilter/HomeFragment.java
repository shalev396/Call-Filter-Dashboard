package com.shalev396.offdutycallfilter;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class HomeFragment extends Fragment {

    private SwitchMaterial switchCallFilter;
    private ConfigManager configManager;
    private TextView filterStatusText;
    
    // Permissions Card
    private LinearLayout permissionsCard;
    private TextView permissionStatusText;
    private TextView permissionStatusDescription;
    private Button buttonGrantPermission;

    // Track which permission is missing for the button action
    private enum MissingPermission {
        NONE,
        CONTACTS,
        CALL_SCREENING,
        BOTH
    }
    private MissingPermission currentMissingPermission = MissingPermission.NONE;

    // Activity result launcher for Call Screening role
    private final ActivityResultLauncher<Intent> requestRoleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                configManager.setRoleRequested(true);
                if (result.getResultCode() == -1) { // RESULT_OK
                    configManager.setEnabled(true);
                } else {
                    switchCallFilter.setChecked(false);
                    Toast.makeText(getContext(), R.string.call_screening_required, Toast.LENGTH_LONG).show();
                }
                updatePermissionsCard();
            });

    // Activity result launcher for Contacts permission
    private final ActivityResultLauncher<String> requestContactsPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                updatePermissionsCard();
                if (!isGranted) {
                    Toast.makeText(getContext(), R.string.contacts_permission_required, Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        configManager = new ConfigManager(requireContext());
        switchCallFilter = view.findViewById(R.id.switch_call_filter);
        filterStatusText = view.findViewById(R.id.text_filter_status);
        permissionsCard = view.findViewById(R.id.permissions_card);
        permissionStatusText = view.findViewById(R.id.permission_status_text);
        permissionStatusDescription = view.findViewById(R.id.permission_status_description);
        buttonGrantPermission = view.findViewById(R.id.button_grant_permission);
        
        // buttonResetData is only used for setting click listener, so keep as local
        Button buttonResetData = view.findViewById(R.id.button_reset_data);

        switchCallFilter.setChecked(configManager.isEnabled());
        updateFilterStatusLabel(configManager.isEnabled());

        switchCallFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateFilterStatusLabel(isChecked);
            if (isChecked) {
                requestAllPermissions();
            } else {
                configManager.setEnabled(false);
                updatePermissionsCard();
            }
        });

        buttonGrantPermission.setOnClickListener(v -> handleGrantPermissionClick());
        buttonResetData.setOnClickListener(v -> showResetConfirmationDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionsCard();
    }

    /**
     * Checks if Contacts permission is granted
     */
    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if Call Screening role is held
     */
    private boolean hasCallScreeningRole() {
        RoleManager roleManager = requireActivity().getSystemService(RoleManager.class);
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
    }

    /**
     * Request all required permissions in order
     */
    private void requestAllPermissions() {
        // First check contacts permission
        if (!hasContactsPermission()) {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            return;
        }
        
        // Then check call screening role
        if (!hasCallScreeningRole()) {
            requestCallScreeningRole();
            return;
        }
        
        // All permissions granted
        configManager.setEnabled(true);
        updatePermissionsCard();
    }

    /**
     * Request the Call Screening role
     */
    private void requestCallScreeningRole() {
        RoleManager roleManager = requireActivity().getSystemService(RoleManager.class);
        
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            configManager.setEnabled(true);
            updatePermissionsCard();
            return;
        }

        if (configManager.isRoleRequested()) {
            // User denied before - show dialog to guide them to Default Apps settings
            showCallScreeningSettingsDialog();
        } else {
            // First time - use the system dialog
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            requestRoleLauncher.launch(intent);
        }
    }

    /**
     * Handle the "Grant Permission" button click based on which permission is missing
     */
    private void handleGrantPermissionClick() {
        switch (currentMissingPermission) {
            case CONTACTS:
                openAppPermissionsSettings();
                break;
            case CALL_SCREENING:
                openDefaultAppsSettings();
                break;
            case BOTH:
                // If both are missing, prioritize contacts first (easier to fix)
                if (!hasContactsPermission()) {
                    openAppPermissionsSettings();
                } else {
                    openDefaultAppsSettings();
                }
                break;
            default:
                // No missing permissions, try requesting again
                requestAllPermissions();
                break;
        }
    }

    /**
     * Opens the app's permission settings page (for Contacts permission)
     */
    private void openAppPermissionsSettings() {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark)
                .setTitle(R.string.contacts_permission_title)
                .setMessage(R.string.contacts_permission_settings_message)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Opens the Default Apps settings page (for Call Screening / Caller ID & spam)
     */
    private void openDefaultAppsSettings() {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark)
                .setTitle(R.string.call_screening_title)
                .setMessage(R.string.call_screening_settings_message)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    // minSdk is 29 (Android Q), so ACTION_MANAGE_DEFAULT_APPS_SETTINGS is always available
                    Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        // Fallback to general settings if specific intent fails
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Show dialog specifically for call screening settings
     */
    private void showCallScreeningSettingsDialog() {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Dark)
                .setTitle(R.string.call_screening_title)
                .setMessage(R.string.call_screening_settings_message)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Update the permissions card based on current permission status
     */
    private void updatePermissionsCard() {
        boolean hasContacts = hasContactsPermission();
        boolean hasCallScreening = hasCallScreeningRole();

        permissionsCard.setVisibility(View.VISIBLE);

        if (hasContacts && hasCallScreening) {
            // All permissions granted
            currentMissingPermission = MissingPermission.NONE;
            permissionStatusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(permissionStatusText, ContextCompat.getColorStateList(requireContext(), R.color.color_success));
            permissionStatusText.setText(R.string.permissions_granted);
            permissionStatusDescription.setText(R.string.permissions_granted_description);
            permissionStatusDescription.setVisibility(View.VISIBLE);
            buttonGrantPermission.setVisibility(View.GONE);
            
            // Update switch state
            switchCallFilter.setChecked(configManager.isEnabled());
            updateFilterStatusLabel(configManager.isEnabled());
            
        } else if (!hasContacts && !hasCallScreening) {
            // Both missing
            currentMissingPermission = MissingPermission.BOTH;
            permissionStatusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(permissionStatusText, ContextCompat.getColorStateList(requireContext(), R.color.color_error));
            permissionStatusText.setText(R.string.permissions_missing_both);
            permissionStatusDescription.setText(R.string.permissions_missing_both_description);
            permissionStatusDescription.setVisibility(View.VISIBLE);
            buttonGrantPermission.setText(R.string.grant_contacts_permission);
            buttonGrantPermission.setVisibility(View.VISIBLE);
            
            switchCallFilter.setChecked(false);
            configManager.setEnabled(false);
            updateFilterStatusLabel(false);
            
        } else if (!hasContacts) {
            // Only contacts missing
            currentMissingPermission = MissingPermission.CONTACTS;
            permissionStatusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(permissionStatusText, ContextCompat.getColorStateList(requireContext(), R.color.color_warning));
            permissionStatusText.setText(R.string.contacts_permission_missing);
            permissionStatusDescription.setText(R.string.contacts_permission_missing_description);
            permissionStatusDescription.setVisibility(View.VISIBLE);
            buttonGrantPermission.setText(R.string.grant_contacts_permission);
            buttonGrantPermission.setVisibility(View.VISIBLE);
            
            updateFilterStatusLabel(false);
            
        } else {
            // Only call screening missing
            currentMissingPermission = MissingPermission.CALL_SCREENING;
            permissionStatusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(permissionStatusText, ContextCompat.getColorStateList(requireContext(), R.color.color_warning));
            permissionStatusText.setText(R.string.call_screening_missing);
            permissionStatusDescription.setText(R.string.call_screening_missing_description);
            permissionStatusDescription.setVisibility(View.VISIBLE);
            buttonGrantPermission.setText(R.string.grant_call_screening);
            buttonGrantPermission.setVisibility(View.VISIBLE);
            
            switchCallFilter.setChecked(false);
            configManager.setEnabled(false);
            updateFilterStatusLabel(false);
        }
    }

    private void updateFilterStatusLabel(boolean enabled) {
        int color = ContextCompat.getColor(requireContext(),
                enabled ? R.color.color_success : R.color.dark_text_secondary);
        filterStatusText.setText(enabled ? R.string.filter_status_enabled : R.string.filter_status_disabled);
        filterStatusText.setTextColor(color);
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialog_Danger)
                .setTitle(R.string.reset_confirmation_title)
                .setMessage(R.string.reset_confirmation_message)
                .setPositiveButton(R.string.yes_clear, (dialog, which) -> {
                    configManager.clearAllData();
                    Toast.makeText(getContext(), R.string.reset_success, Toast.LENGTH_SHORT).show();
                    updatePermissionsCard();
                })
                .setNegativeButton(R.string.no_keep, null)
                .show();
    }
}
