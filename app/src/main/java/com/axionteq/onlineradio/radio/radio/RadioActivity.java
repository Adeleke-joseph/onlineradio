package com.axionteq.onlineradio.radio.radio;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.axionteq.onlineradio.ConnectionDetector;
import com.axionteq.onlineradio.R;
import com.axionteq.onlineradio.api.ApiInterface;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RadioActivity extends AppCompatActivity implements CurrentSessionCallback, View.OnClickListener {

    TextView tvRadio, tvPastor, tvSubtitle;
    CustomToast toast;
    ConnectionDetector connectionDetector = new ConnectionDetector( this );

    public Call<List<RadioType>> call;
    public Context context;
    ImageView imgRadio;
    MediaPlayer mediaPlayer = new MediaPlayer();
    ShimmerFrameLayout shimmerFrameLayout;
    ProgressBar progressBar;
    Toolbar toolbar;
    List<RadioType> radioTypeList = new ArrayList<>();
    String radioLink, radioImage, radioTitle, radioPastor, radioSubtitle;
    PlayPauseView btnPlay;
    ImageView imgPlayerBottom, imgPlayerBackground, imgPlayer;

    AudioStreamingManager streamingManager;
    RelativeLayout pgPlayPauseLayout, rlMiniPlayer;
    List<RadioType> radioType = new ArrayList<>();

    RadioType radioType1;
    AudioPlaybackListener playbackListener;
    ImageView btnForward, btnBackward;

    AudioManager audioManager;
    private ApiInterface apiInterface;
    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder( R.drawable.wci_logo )
            .error( R.drawable.wci_logo );

    public RadioActivity(Context context){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.radio_player );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        shimmerFrameLayout = findViewById( R.id.sfl_radio );
        toolbar = findViewById( R.id.tb_radio );
        tvRadio = findViewById( R.id.tv_radio );
        imgRadio = findViewById( R.id.img_radio );
        tvPastor = findViewById( R.id.radio_tv_pastor );
        tvSubtitle = findViewById( R.id.radio_tv_subtitle );
        pgPlayPauseLayout = findViewById( R.id.pgPlayPauseLayout );
        btnPlay = findViewById( R.id.btn_play );
        btnBackward = findViewById( R.id.img_previous );
        btnForward = findViewById( R.id.img_next );

        playbackListener = new AudioPlaybackListener( this );
        shimmerFrameLayout.startShimmer();
        toolbar.setTitle( "Live Radio" );

        btnBackward.setOnClickListener( this );
        btnForward.setOnClickListener( this );
        btnPlay.setOnClickListener( this );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( "https://wcisantamaria.000webhostapp.com/" )
                .addConverterFactory( GsonConverterFactory.create() )
                .build();
        apiInterface = retrofit.create( ApiInterface.class );

        if (!connectionDetector.Connection()) {
            Retry();
        } else {
            getRadio();
        }
        btnPlay.Pause();

        pgPlayPauseLayout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        } );
    }

    private void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance( this );
        streamingManager.setPlayMultiple( false );

        streamingManager.setMediaList( radioType );
        streamingManager.setShowPlayerNotification( true );
        streamingManager.setPendingIntentAct( getNotificationPendingIntent() );
    }

    private PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent( this, RadioActivity.class );
        intent.setAction( "openplayer" );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
        return PendingIntent.getActivity( this, 0, intent, 0 );
    }

    private void getRadio() {
        call = apiInterface.getRadioType();

        call.enqueue( new Callback<List<RadioType>>() {
            @Override
            public void onResponse(Call<List<RadioType>> call, Response<List<RadioType>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                radioType = response.body();

                assert radioType != null;
                for (RadioType radioType : radioType) {

                    playSong( radioType );

                    configAudioStreamer();
                    checkAlreadyPlaying();
                    String image, title, link, subtitle, pastor;

                    title = radioType.getRadioTitle();
                    image = radioType.getRadioImage();

                    Glide.with( RadioActivity.this ).load( radioType.getRadioImage() ).apply( options ).into( imgRadio );
                    pastor = radioType.getRadioPastor();
                    subtitle = radioType.getRadioSubtitle();

                    tvPastor.setText( pastor );
                    tvSubtitle.setText( subtitle );
                    tvRadio.setText( title );

                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility( View.GONE );
                }
            }

            @Override
            public void onFailure(Call<List<RadioType>> call, Throwable t) {
                Toast.makeText( RadioActivity.this, "Check internet connection and try again",  Toast.LENGTH_LONG).show();
//                toast.createToast( "Check internet connection and try again", 0 );
                Retry();
            }
        } );
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (streamingManager != null) {
                streamingManager.subscribesCallBack( this );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                if (radioType != null) {
                    playPauseEvent( view );
                }
                break;
        }
    }

    private void playPauseEvent(View v) {
        if (streamingManager.isPlaying()) {
            streamingManager.onPause();
            ((PlayPauseView) v).Pause();
        } else {
            streamingManager.onPlay( radioType1 );
            ((PlayPauseView) v).Play();
        }
    }

    @Override
    public void updatePlaybackState(int state) {
        Logger.e( "updatePlaybackState: ", "" + state );
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                pgPlayPauseLayout.setVisibility( View.INVISIBLE );
                btnPlay.Play();
                if (radioType1 != null) {
                    radioType1.setPlayState( PlaybackStateCompat.STATE_PLAYING );
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                pgPlayPauseLayout.setVisibility( View.INVISIBLE );
                btnPlay.Pause();
                if (radioType1 != null) {
                    radioType1.setPlayState( PlaybackStateCompat.STATE_PAUSED );
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                radioType1.setPlayState( PlaybackStateCompat.STATE_NONE );
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                pgPlayPauseLayout.setVisibility( View.INVISIBLE );
                btnPlay.Pause();
                if (radioType1 != null) {
                    radioType1.setPlayState( PlaybackStateCompat.STATE_NONE );
                }
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                pgPlayPauseLayout.setVisibility( View.VISIBLE );
                if (radioType1 != null) {
                    radioType1.setPlayState( PlaybackStateCompat.STATE_NONE );
                }
                break;
        }
    }

    @Override
    public void playSongComplete() {

    }

    @Override
    public void currentSeekBarPosition(int progress) {

    }

    @Override
    public void playCurrent(int indexP, RadioType currentAudio) {

    }

    @Override
    public void playNext(int indexP, RadioType currentAudio) {

    }

    @Override
    public void playPrevious(int indexP, RadioType currentAudio) {

    }

    private void playSong(RadioType media) {
        if (streamingManager != null) {
            streamingManager.onPlay( media );
        }
    }

    private void checkAlreadyPlaying() {
        if (streamingManager.isPlaying()) {
            radioType1 = streamingManager.getCurrentAudio();
            if (radioType1 != null) {
                radioType1.setPlayState( streamingManager.mLastPlaybackState );
            }
        }
    }

    public void Retry() {
/*        ChocoBar.builder().setActivity( RadioActivity.this ).setActionText( "Retry" )
                .setActionClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getRadio();
                    }

                } )
                .setText( "Internet Connection Error" )
                .setDuration( ChocoBar.LENGTH_INDEFINITE )
                .setIcon( R.drawable.ic_info_outline_black_24dp )
                .build()
                .show();*/
    }


}
