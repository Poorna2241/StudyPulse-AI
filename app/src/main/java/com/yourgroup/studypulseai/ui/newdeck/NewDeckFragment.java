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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.data.db.AppDatabase;
import com.yourgroup.studypulseai.data.model.Deck;
import com.yourgroup.studypulseai.data.model.Flashcard;
import com.yourgroup.studypulseai.data.model.QuizQuestion;
import com.yourgroup.studypulseai.network.GeminiApiService;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import com.yourgroup.studypulseai.network.SupabaseRepo;
import com.yourgroup.studypulseai.network.models.SFlashcard;
import com.yourgroup.studypulseai.util.FileExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewDeckFragment extends Fragment {
    private TextInputEditText etDeckTitle, etNotes;
    private TabLayout tabInputMethod;
    private LinearLayout panelText, panelFile;
    private MaterialCardView dropZone;
    private TextView tvFileName, tvProgressMessage;
    private CheckBox cbFlashcards, cbQuiz;
    private TextView tvFlashcardCount, tvQuizCount;
    private Slider sliderFlashcards, sliderQuiz;
    private MaterialButton btnGenerate;
    private LinearProgressIndicator progressGenerate;
    private androidx.appcompat.app.AlertDialog progressDialog;

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

        getParentFragmentManager().setFragmentResultListener("fileTextKey", this, (requestKey, bundle) -> {
            String confirmedText = bundle.getString("confirmedText");
            if (confirmedText != null) {
                etNotes.setText(confirmedText);
                if (tabInputMethod.getTabAt(0) != null) {
                    tabInputMethod.getTabAt(0).select();
                }
            }
        });

        etDeckTitle = view.findViewById(R.id.etDeckTitle);
        etNotes = view.findViewById(R.id.etNotes);
        tabInputMethod = view.findViewById(R.id.tabInputMethod);
        panelText = view.findViewById(R.id.panelText);
        panelFile = view.findViewById(R.id.panelFile);
        dropZone = view.findViewById(R.id.dropZone);
        tvFileName = view.findViewById(R.id.tvFileName);
        
        cbFlashcards = view.findViewById(R.id.cbGenerateFlashcards);
        cbQuiz = view.findViewById(R.id.cbGenerateQuiz);
        tvFlashcardCount = view.findViewById(R.id.tvFlashcardCountValue);
        tvQuizCount = view.findViewById(R.id.tvQuizCountValue);
        sliderFlashcards = view.findViewById(R.id.sliderFlashcardCount);
        sliderQuiz = view.findViewById(R.id.sliderQuizCount);
        
        btnGenerate = view.findViewById(R.id.btnGenerate);
        progressGenerate = view.findViewById(R.id.progressGenerate);
        tvProgressMessage = view.findViewById(R.id.tvProgressMessage);

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

        setupControls();
        btnGenerate.setOnClickListener(v -> generateDeck());
        startRotatingBorderAnimation();

        return view;
    }

    private void setupControls() {
        cbFlashcards.setOnCheckedChangeListener((b, isChecked) -> {
            sliderFlashcards.setEnabled(isChecked);
            updateButtonText();
        });
        cbQuiz.setOnCheckedChangeListener((b, isChecked) -> {
            sliderQuiz.setEnabled(isChecked);
            updateButtonText();
        });

        sliderFlashcards.addOnChangeListener((s, value, fromUser) -> tvFlashcardCount.setText(String.valueOf((int) value)));
        sliderQuiz.addOnChangeListener((s, value, fromUser) -> tvQuizCount.setText(String.valueOf((int) value)));

        updateButtonText(); // Set initial button text based on default checked states
    }

    private void updateButtonText() {
        boolean f = cbFlashcards.isChecked();
        boolean q = cbQuiz.isChecked();
        if (f && q) btnGenerate.setText("Generate Flashcards & Quiz");
        else if (f) btnGenerate.setText("Generate Flashcards Only");
        else if (q) btnGenerate.setText("Generate Quiz Only");
        else btnGenerate.setText("Select generation type");
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
                if (getView() == null) return;
                Bundle args = new Bundle();
                args.putString("extractedText", text);
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_newDeckFragment_to_filePreviewFragment, args);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    Toast.makeText(getContext(), "Couldn't open file preview. Please try again.", Toast.LENGTH_SHORT).show();
                }
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
        String notes = etNotes.getText().toString().trim();
        
        boolean genFlash = cbFlashcards.isChecked();
        boolean genQuiz = cbQuiz.isChecked();
        
        if (!genFlash && !genQuiz) {
            Toast.makeText(getContext(), "Please select at least one type to generate", Toast.LENGTH_SHORT).show();
            return;
        }

        int fCount = genFlash ? (int) sliderFlashcards.getValue() : 0;
        int qCount = genQuiz ? (int) sliderQuiz.getValue() : 0;

        if (title.isEmpty()) title = "Untitled Deck";
        if (notes.isEmpty()) {
            Toast.makeText(getContext(), "Please provide some notes", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerate.setEnabled(false);
        progressGenerate.setVisibility(View.VISIBLE);
        progressGenerate.setIndeterminate(true);
        tvProgressMessage.setVisibility(View.VISIBLE);

        progressDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setMessage("Please wait, your materials are being created...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        String finalTitle = title;
        new GeminiApiService().generateDeck(notes, fCount, qCount, new GeminiApiService.ApiCallback() {
            @Override
            public void onSuccess(List<Flashcard> flashcards, List<QuizQuestion> questions) {
                if (getActivity() == null) return;

                String[] backgrounds = {"c1", "c2", "c3", "c4", "c5"};
                String randomBg = backgrounds[new Random().nextInt(backgrounds.length)];

                SupabaseRepo.saveDeck(finalTitle, randomBg, deckId -> {
                    if (getActivity() == null) return;
                    if (deckId != null) {
                        List<SFlashcard> sFlashcards = new ArrayList<>();
                        String userId = SupabaseAuthHelper.getCurrentUserId();
                        if (userId == null) userId = "guest";

                        for (Flashcard f : flashcards) {
                            sFlashcards.add(new SFlashcard(null, deckId, userId, f.getQuestion(), f.getAnswer(), 1));
                        }

                        SupabaseRepo.saveFlashcards(sFlashcards, success -> {
                            if (getActivity() == null) return;
                            new Thread(() -> {
                                if (getContext() == null) return;
                                try {
                                    Deck localDeck = new Deck(finalTitle, randomBg, notes, qCount);
                                    long localId = AppDatabase.getInstance(requireContext()).deckDao().insertDeck(localDeck);
                                    for (Flashcard f : flashcards) {
                                        f.setDeckId((int) localId);
                                        AppDatabase.getInstance(requireContext()).deckDao().insertFlashcard(f);
                                    }
                                    for (QuizQuestion q : questions) {
                                        q.setDeckId((int) localId);
                                    }
                                    AppDatabase.getInstance(requireContext()).deckDao().insertQuizQuestions(questions);

                                     requireActivity().runOnUiThread(() -> {
                                         if (getView() == null) return;
                                         if (progressDialog != null && progressDialog.isShowing()) {
                                             progressDialog.dismiss();
                                         }
                                         progressGenerate.setVisibility(View.GONE);
                                         tvProgressMessage.setVisibility(View.GONE);
                                         btnGenerate.setEnabled(true);
                                        Toast.makeText(getContext(), "Deck generated and synced!", Toast.LENGTH_LONG).show();
                                        try {
                                            Navigation.findNavController(requireView())
                                                    .navigate(R.id.action_newDeckFragment_to_homeFragment);
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), "Deck saved. Please go back to Home manually.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (getView() == null) return;
                                        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                                        progressGenerate.setVisibility(View.GONE);
                                        tvProgressMessage.setVisibility(View.GONE);
                                        btnGenerate.setEnabled(true);
                                        Toast.makeText(getContext(), "Local save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }).start();
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            if (getView() == null) return;
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                            progressGenerate.setVisibility(View.GONE);
                            tvProgressMessage.setVisibility(View.GONE);
                            btnGenerate.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to sync with Supabase", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    progressGenerate.setVisibility(View.GONE);
                    tvProgressMessage.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
