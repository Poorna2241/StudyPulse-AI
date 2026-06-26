package com.yourgroup.studypulseai.ui.newdeck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yourgroup.studypulseai.R;

public class FilePreviewFragment extends Fragment {
    private TextView tvExtractedText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_preview, container, false);
        tvExtractedText = view.findViewById(R.id.tvExtractedText);

        if (getArguments() != null) {
            String text = getArguments().getString("extractedText");
            tvExtractedText.setText(text);
        }

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            // Logic to confirm and go back to NewDeckFragment with the text
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}