package com.example.bakingtime;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bakingtime.models.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class RecipeStepFragment extends Fragment {

    private Step step;
    private OnNextStepClickedListener mCallback;
    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private TextView mDescription;
    private Button mNextStep;

    private final static String TAG = RecipeStepFragment.class.getSimpleName();
    private final static String DESCRIPTION = "description";
    private final static String MOVIE_URL = "movie_url";
    private final static String PLAYER_CURRENT_POS_KEY = "player_pos";
    private final static String PLAYER_IS_READY_KEY = "player_ready";

    private Boolean isPortrait = false;


    public interface OnNextStepClickedListener{
        void onNextStepClickedListener(int stepId);
    }

    public RecipeStepFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_step_fragment, container,false);

        mDescription = rootView.findViewById(R.id.tv_step_detail);
        mPlayerView = rootView.findViewById(R.id.exo_player);
        mNextStep = rootView.findViewById((R.id.btn_next_step));

        try{
            mCallback = (OnNextStepClickedListener) container.getContext();
        } catch (ClassCastException e){
            throw new ClassCastException( " must be implemented!");
        }

        if(rootView.findViewById(R.id.tv_step_detail) == null){
            Log.d(TAG, "it is portrait mode");
            isPortrait = true;
        }

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState == null){
            if(step != null){
                mDescription.setText(step.getDescription());
                initializePlayer(step.getVideoURL(), this.getContext());
                if(!isPortrait)
                    mNextStep.setOnClickListener( (view) -> mCallback.onNextStepClickedListener(step.getId()) );
            } else {
                Log.d(TAG, "Step is not set!");
            }
        } else {
            if(!isPortrait){
                mNextStep.setOnClickListener( (view) -> mCallback.onNextStepClickedListener(step.getId()) );
                if(savedInstanceState.containsKey(DESCRIPTION)){
                    mDescription.setText(savedInstanceState.getString(DESCRIPTION));
                }
                initializePlayer(step.getVideoURL(), this.getContext());
            }
            resumePlaybackFromStateBundle(savedInstanceState);
        }
    }

    private void resumePlaybackFromStateBundle(@Nullable Bundle inState) {
        Log.d(TAG, "Resuming player.");
        mExoPlayer.setPlayWhenReady(inState.getBoolean(PLAYER_IS_READY_KEY));
        mExoPlayer.seekTo(inState.getLong(PLAYER_CURRENT_POS_KEY));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DESCRIPTION, step.getDescription());
        outState.putString(MOVIE_URL, step.getVideoURL());
        if(mExoPlayer != null) {
            outState.putLong(PLAYER_CURRENT_POS_KEY, Math.max(0, mExoPlayer.getCurrentPosition()));
            outState.putBoolean(PLAYER_IS_READY_KEY, mExoPlayer.getPlayWhenReady());
        }
    }

    public void setStep(Step step) {
        this.step = step;
    }

    private void initializePlayer(String urlString, Context context) {
        if( urlString == null || urlString.isEmpty()) return;

        Uri mediaUri = Uri.parse(urlString);
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);
            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(context, "BakingTime");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    context, userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mExoPlayer != null)
            releasePlayer();
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }
}