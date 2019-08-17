package com.example.bakingtime;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecipeStepFragment extends Fragment {

    private Step step;
    private OnNextStepClickedListener mCallback;
    @BindView(R.id.exo_player) SimpleExoPlayerView mPlayerView;
    private static SimpleExoPlayer mExoPlayer;
    //private Long currentPosition;
    //private Boolean isPlayerReady;
    @Nullable @BindView(R.id.tv_step_detail) TextView mDescription;
    @Nullable @BindView(R.id.btn_next_step) Button mNextStep;
    @BindView(R.id.iv_player_no_image) ImageView mNoImage;


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
       /* if(mExoPlayer != null) {
            args.putLong(PLAYER_CURRENT_POS_KEY, Math.max(0, mExoPlayer.getCurrentPosition()));
            args.putBoolean(PLAYER_IS_READY_KEY, mExoPlayer.getPlayWhenReady());
        }*/

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
        ButterKnife.bind(this, rootView);

        isTablet = getResources().getBoolean(R.bool.isTablet);


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
            initializePlayer(step.getVideoURL(), this.getContext());
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

    /*private void resumePlaybackFromStateBundle() {
        Log.d(TAG, "Resuming player.");
        mExoPlayer.setPlayWhenReady(isPlayerReady);
        mExoPlayer.seekTo(currentPosition);
    }*/

    private void initializePlayer(String urlString, Context context) {
        if (step.getVideoURL() == null || step.getVideoURL().isEmpty()) return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) {
            Toast.makeText(context, "Could not connect to the internet!", Toast.LENGTH_SHORT).show();
            return;
        }

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
            //resumePlaybackFromStateBundle();
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
