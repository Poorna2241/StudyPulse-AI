package com.yourgroup.studypulseai.ui.challenge;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.yourgroup.studypulseai.R;
import com.yourgroup.studypulseai.network.SupabaseAuthHelper;
import com.yourgroup.studypulseai.network.SupabaseRepo;
import com.yourgroup.studypulseai.network.models.SChallengeParticipant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChallengeWaitingRoomFragment extends Fragment {
    private TextView tvChallengeCode, tvCountdown;
    private RecyclerView rvPlayers;
    private MaterialButton btnStartEarly, btnLeaveRoom, btnCloseChallenge;
    
    private PlayerAdapter adapter;
    private int challengeId = -1;
    private String challengeCode = "";
    private CountDownTimer timer;
    private boolean isHost = false;
    private boolean isNavigating = false;
    
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isNavigating || getActivity() == null) return;
            checkChallengeStatusDirectly();
            pollHandler.postDelayed(this, 3000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge_waiting_room, container, false);

        if (getArguments() != null) {
            challengeId = getArguments().getInt("challengeId", -1);
            challengeCode = getArguments().getString("challengeCode", "");
        }

        tvChallengeCode = view.findViewById(R.id.tvChallengeCode);
        tvCountdown = view.findViewById(R.id.tvCountdown);
        rvPlayers = view.findViewById(R.id.rvPlayers);
        btnStartEarly = view.findViewById(R.id.btnStartEarly);
        btnLeaveRoom = view.findViewById(R.id.btnLeaveRoom);
        btnCloseChallenge = view.findViewById(R.id.btnCloseChallenge);

        tvChallengeCode.setText(challengeCode);

        adapter = new PlayerAdapter();
        rvPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlayers.setAdapter(adapter);

        setupListeners();
        setupNavigation();
        checkHostPermissions();
        startCountdown();
        
        pollHandler.postDelayed(pollRunnable, 3000);

        return view;
    }

    private void checkChallengeStatusDirectly() {
        SupabaseRepo.getChallengeById(challengeId, challenge -> {
            if (challenge == null || getActivity() == null || isNavigating) return;
            
            if ("started".equalsIgnoreCase(challenge.getStatus())) {
                navigateToQuiz();
            } else if ("finished".equalsIgnoreCase(challenge.getStatus())) {
                exitToHome("Challenge ended");
            }
        });
    }

    private void checkHostPermissions() {
        SupabaseRepo.getChallengeById(challengeId, challenge -> {
            if (getActivity() == null || challenge == null) return;
            String currentUserId = SupabaseAuthHelper.getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(challenge.getHost_id())) {
                isHost = true;
                btnCloseChallenge.setVisibility(View.VISIBLE);
                btnStartEarly.setVisibility(View.VISIBLE);
                
                btnCloseChallenge.setOnClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("End Challenge?")
                            .setMessage("This will close the room for everyone.")
                            .setPositiveButton("End", (d, w) -> {
                                SupabaseRepo.updateChallengeStatus(challengeId, "finished");
                                exitToHome(null);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                btnStartEarly.setOnClickListener(v -> triggerStart());
            }
        });
    }

    private void triggerStart() {
        if (!isHost || isNavigating) return;
        btnStartEarly.setEnabled(false);
        SupabaseRepo.updateChallengeStatus(challengeId, "started");
    }

    private void setupNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exitToHome(null);
            }
        });
    }

    private void setupListeners() {
        SupabaseRepo.listenToParticipants(challengeId, participants -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setPlayers(participants));
            }
        });

        SupabaseRepo.listenToChallengeStatus(challengeId, status -> {
            if ("started".equalsIgnoreCase(status)) {
                navigateToQuiz();
            } else if ("finished".equalsIgnoreCase(status)) {
                exitToHome("Challenge ended by host");
            }
        });

        btnLeaveRoom.setOnClickListener(v -> confirmLeave());
    }

    private void confirmLeave() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit Room?")
                .setMessage("You will be removed from the participant list.")
                .setPositiveButton("Exit", (d, w) -> {
                    SupabaseRepo.leaveChallenge(challengeId);
                    exitToHome(null);
                })
                .setNegativeButton("Stay", null)
                .show();
    }

    private void startCountdown() {
        SupabaseRepo.getChallengeById(challengeId, challenge -> {
            if (challenge == null || getActivity() == null) return;
            
            long startTime = challenge.getStart_time();
            long now = System.currentTimeMillis();
            long diff = startTime - now;

            if (diff > 0) {
                if (timer != null) timer.cancel();
                timer = new CountDownTimer(diff, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (tvCountdown != null) {
                            long seconds = millisUntilFinished / 1000;
                            tvCountdown.setText(String.format(Locale.getDefault(), "Starts in: %02d:%02d", seconds / 60, seconds % 60));
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (tvCountdown != null) tvCountdown.setText("Starting...");
                        if (isHost) triggerStart();
                    }
                }.start();
            } else if ("started".equalsIgnoreCase(challenge.getStatus())) {
                navigateToQuiz();
            }
        });
    }

    private void navigateToQuiz() {
        if (isNavigating || getActivity() == null || getView() == null) return;
        isNavigating = true;
        getActivity().runOnUiThread(() -> {
            if (timer != null) timer.cancel();
            pollHandler.removeCallbacks(pollRunnable);
            
            Bundle args = new Bundle();
            args.putInt("challengeId", challengeId);
            args.putString("challengeCode", challengeCode);
            try {
                Navigation.findNavController(requireView()).navigate(R.id.action_challengeWaitingRoomFragment_to_challengeQuizFragment, args);
            } catch (Exception e) {
                Log.e("WaitingRoom", "Nav to Quiz failed", e);
                isNavigating = false;
            }
        });
    }

    private void exitToHome(String message) {
        if (isNavigating || getActivity() == null || getView() == null) return;
        isNavigating = true;
        getActivity().runOnUiThread(() -> {
            if (timer != null) timer.cancel();
            pollHandler.removeCallbacks(pollRunnable);
            if (message != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            try {
                Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
            } catch (Exception e) {
                Log.e("WaitingRoom", "Nav to Home failed", e);
                isNavigating = false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pollHandler.removeCallbacks(pollRunnable);
        if (timer != null) timer.cancel();
    }

    static class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {
        private List<SChallengeParticipant> players = new ArrayList<>();

        public void setPlayers(List<SChallengeParticipant> players) {
            this.players = players;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new PlayerViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
            holder.text.setText(players.get(position).getUser_name());
        }

        @Override
        public int getItemCount() { return players.size(); }

        static class PlayerViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            public PlayerViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
