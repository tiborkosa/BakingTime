package com.example.bakingtime;

import android.content.Context;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class RecipeStepFragment extends Fragment {

    private Step step;
    private OnNextStepClickedListener mCallback;
    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private Long currentPosition;
    private Boolean isPlayerReady;
    private TextView mDescription;
    private Button mNextStep;
    private ImageView mNoImage;

    private final static String TAG = RecipeStepFragment.class.getSimpleName();
    private final static String STEP = "step";
    private final static String PLAYER_CURRENT_POS_KEY = "player_pos";
    private final static String PLAYER_IS_READY_KEY = "player_ready";

    private Boolean isPortrait = false;
    private Boolean isTablet;


    public interface OnNextStepClickedListener{
        void onNextStepClickedListener(int stepId);
    }

    // Required empty public constructor
    public RecipeStepFragment() {}

    public static RecipeStepFragment newInstance(Step step){
        RecipeStepFragment fragment = new RecipeStepFragment();
        Bundle args = new Bundle();
        args.putParcelable(STEP, step);
        //args.putLong(PLAYER_CURRENT_POS_KEY, Math.max(0, mExoPlayer.getCurrentPosition()));
        //args.putBoolean(PLAYER_IS_READY_KEY, mExoPlayer.getPlayWhenReady());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate RecipeStepFragment.");
        if(getArguments() != null){
            step = getArguments().getParcelable(STEP);
            Log.d(TAG, step.toString());
            //currentPosition = getArguments().getLong(PLAYER_CURRENT_POS_KEY);
            //isPlayerReady = getArguments().getBoolean(PLAYER_IS_READY_KEY);
        } else {
            Log.d(TAG, "onCreate RecipeStepFragment. Argument is null");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_step_fragment, container,false);

        isTablet = getResources().getBoolean(R.bool.isTablet);
        mDescription = rootView.findViewById(R.id.tv_step_detail);
        mPlayerView = rootView.findViewById(R.id.exo_player);
        mNextStep = rootView.findViewById((R.id.btn_next_step));
        mNoImage = rootView.findViewById(R.id.iv_player_no_image);

        if(rootView.findViewById(R.id.tv_step_detail) == null){
            Log.d(TAG, "it is portrait mode");
            isPortrait = true;
        }

        try{
            mCallback = (OnNextStepClickedListener) container.getContext();
        } catch (ClassCastException e){
            throw new ClassCastException( " must be implemented!");
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNextStepClickedListener) {
            mCallback = (OnNextStepClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNextStepClickedListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!isPortrait || isTablet) {
            mDescription.setText(step.getDescription());
        }
        if(!isPortrait && !isTablet){
            mNextStep.setVisibility(View.VISIBLE);
            mNextStep.setOnClickListener((view) -> mCallback.onNextStepClickedListener(step.getId()));
        }

        if (step.getVideoURL() != null && !step.getVideoURL().isEmpty()) {
            initializePlayer(step.getVideoURL(), this.getContext(), savedInstanceState);
        } else if(step.getThumbnailURL() != null && !step.getThumbnailURL().isEmpty()){
            hideVideoShowImage();
            Picasso
                .get()
                .load(step.getThumbnailURL())
                .into(mNoImage, new Callback() {
                    @Override
                    public void onSuccess() { }
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"error ");
                    }
                });
        }else {
            hideVideoShowImage();
        }
    }

    private void hideVideoShowImage() {
        mPlayerView.setVisibility(View.GONE);
        mNoImage.setVisibility(View.VISIBLE);
    }

    private void resumePlaybackFromStateBundle(@Nullable Bundle inState) {
        Log.d(TAG, "Resuming player.");
        mExoPlayer.setPlayWhenReady(inState.getBoolean(PLAYER_IS_READY_KEY));
        mExoPlayer.seekTo(inState.getLong(PLAYER_CURRENT_POS_KEY));
    }

    private void initializePlayer(String urlString, Context context, Bundle inState) {
        if (step.getVideoURL() == null || step.getVideoURL().isEmpty()) return;

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
        } else{
            if (inState != null) {
                resumePlaybackFromStateBundle(inState);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

}
