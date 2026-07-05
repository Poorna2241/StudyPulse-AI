package com.yourgroup.studypulseai.ui.newdeck;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.util.FileExtractor;

import java.util.Random;

public class NewDeckFragment extends Fragment {
    private TextInputEditText etDeckTitle, etNotes;
    private TabLayout tabInputMethod;
    private LinearLayout panelText, panelFile;
    private MaterialCardView dropZone;
    private TextView tvFileName, tvSliderValue, tvSliderLabel;
    private Slider sliderCount;
    private ChipGroup chipGroupMode;
    private MaterialButton btnGenerate;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri fileUri = result.getData().getData();
                if (fileUri != null) {
                    handleFileSelected(fileUri);
                }
            }
        });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_deck, container, false);

        etDeckTitle = view.findViewById(R.id.etDeckTitle);
        etNotes = view.findViewById(R.id.etNotes);
        tabInputMethod = view.findViewById(R.id.tabInputMethod);
        panelText = view.findViewById(R.id.panelText);
        panelFile = view.findViewById(R.id.panelFile);
        dropZone = view.findViewById(R.id.dropZone);
        tvFileName = view.findViewById(R.id.tvFileName);
        sliderCount = view.findViewById(R.id.sliderCount);
        tvSliderValue = view.findViewById(R.id.tvSliderValue);
        tvSliderLabel = view.findViewById(R.id.tvSliderLabel);
        chipGroupMode = view.findViewById(R.id.chipGroupMode);
        btnGenerate = view.findViewById(R.id.btnGenerate);

        tabInputMethod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    panelText.setVisibility(View.VISIBLE);
                    panelFile.setVisibility(View.GONE);
                } else {
                    panelText.setVisibility(View.GONE);
                    panelFile.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        dropZone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "text/plain",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            });
            filePickerLauncher.launch(intent);
        });

        sliderCount.addOnChangeListener((slider, value, fromUser) -> {
            tvSliderValue.setText(String.valueOf((int) value));
        });

        chipGroupMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipFlashcards) {
                    tvSliderLabel.setText(R.string.label_number_of_flashcards);
                    btnGenerate.setText(R.string.btn_generate_flashcards);
                    tvSliderValue.setText(String.valueOf((int) sliderCount.getValue()));
                } else {
                    tvSliderLabel.setText(R.string.label_number_of_questions);
                    btnGenerate.setText(R.string.btn_generate_quiz);
                    tvSliderValue.setText(String.valueOf((int) sliderCount.getValue()));
                }
            }
        });

        btnGenerate.setOnClickListener(v -> generateDeck());

        startRotatingBorderAnimation();

        return view;
    }

    private void startRotatingBorderAnimation() {
        Drawable background = btnGenerate.getBackground();
        if (background instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) background;
            Drawable border = layerDrawable.findDrawableByLayerId(R.id.borderLayer);
            if (border != null) {
                ObjectAnimator animator = ObjectAnimator.ofInt(border, "level", 0, 10000);
                animator.setDuration(2000); // 2 seconds per rotation
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new LinearInterpolator());
                animator.start();
            }
        }
    }

    private void handleFileSelected(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        tvFileName.setText("Selected: " + fileName);
        
        new Thread(() -> {
            String text = FileExtractor.extract(requireContext(), uri);
            requireActivity().runOnUiThread(() -> {
                Bundle args = new Bundle();
                args.putString("extractedText", text);
                Navigation.findNavController(requireView())
                    .navigate(R.id.filePreviewFragment, args);
            });
        }).start();
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void generateDeck() {
        String title = etDeckTitle.getText().toString().trim();
        if (title.isEmpty()) title = "Untitled Deck";

        String[] backgrounds = {"c1", "c2", "c3", "c4", "c5"};
        String randomBg = backgrounds[new Random().nextInt(backgrounds.length)];

        Deck newDeck = new Deck(title, randomBg);
        
        // Navigation or DB logic...
    }
}