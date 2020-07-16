package com.axionteq.onlineradio.radio;

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

import com.axionteq.onlineradio.R;
import com.axionteq.onlineradio.api.ApiInterface;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RadioActivity extends AppCompatActivity implements CurrentSessionCallback, View.OnClickListener {

    private static final String TAG = Logger.makeLogTag( RadioActivity.class );
    TextView tvRadio, tvPastor, tvSubtitle;
    public Call<List<Radio>> call;
    public Context context;
    ImageView imgRadio;
    MediaPlayer mediaPlayer = new MediaPlayer();
    ShimmerFrameLayout shimmerFrameLayout;
    ProgressBar progressBar;
    Toolbar toolbar;
    List<Radio> radioList = new ArrayList<>();
    String radioLink, radioImage, radioTitle, radioPastor, radioSubtitle;
    PlayPauseView btnPlay;
    ImageView imgPlayerBottom, imgPlayerBackground, imgPlayer;
    Disposable disposable;

    AudioStreamingManager streamingManager;
    RelativeLayout pgPlayPauseLayout, rlMiniPlayer;
    List<Radio> radio = new ArrayList<>();

    Radio radio1 = new Radio();
    AudioPlaybackListener playbackListener;
    ImageView btnForward, btnBackward;

    AudioManager audioManager;
    private ApiInterface apiInterface;
    private RequestOptions options = new RequestOptions().centerCrop().placeholder( R.drawable.wci_logo ).error( R.drawable.wci_logo );


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
        shimmerFrameLayout.setVisibility( View.VISIBLE );

        playbackListener = new AudioPlaybackListener( this );
        shimmerFrameLayout.startShimmer();
        setSupportActionBar( toolbar );
        if (getSupportActionBar() != null) {
            toolbar.setTitle( "Live Radio" );

            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
            getSupportActionBar().setHomeAsUpIndicator( R.drawable.ic_notification_icon_24dp );
            getSupportActionBar().setDisplayShowHomeEnabled( true );
        }

        btnBackward.setOnClickListener( this );
        btnForward.setOnClickListener( this );
        btnPlay.setOnClickListener( this );

        Retrofit retrofit = new Retrofit.Builder().baseUrl( "https://wcisantamaria.000webhostapp.com/" ).addConverterFactory( GsonConverterFactory.create() ).build();
        apiInterface = retrofit.create( ApiInterface.class );
        btnPlay.Pause();

        pgPlayPauseLayout.setOnClickListener( view -> {

        } );
        getRadio();
    }

    private void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance( this );
        streamingManager.setPlayMultiple();
        streamingManager.setMediaList( radio );
        streamingManager.setShowPlayerNotification();
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
        disposable = ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn( Schedulers.io() )
                .observeOn( Schedulers.newThread() )
                .subscribe( connectivity -> {
                            if (connectivity) {

                                call.enqueue( new Callback<List<Radio>>() {
                                    @Override
                                    public void onResponse(Call<List<Radio>> call, Response<List<Radio>> response) {
                              /*  if (!response.isSuccessful()) {
                                    return;
                                }*/

                                        radio = response.body();

                                        assert radio != null;
                                        for (Radio radio : RadioActivity.this.radio) {

                                            String image, title, link, subtitle, pastor;

                                            title = radio.getRadioTitle();
                                            image = radio.getRadioImage();
                                            link = radio.getRadioLink();

                                            Glide.with( RadioActivity.this ).load( image ).apply( options ).into( imgRadio );
                                            pastor = radio.getRadioPastor();
                                            subtitle = radio.getRadioSubtitle();

                                            tvPastor.setText( pastor );
                                            tvSubtitle.setText( subtitle );
                                            tvRadio.setText( title );

                                            shimmerFrameLayout.stopShimmer();
                                            shimmerFrameLayout.setVisibility( View.GONE );
                                            radio = new Radio( title, image, link, pastor, subtitle );
                                            radio1 = radio;
                                            playSong( radio );

//                                    checkAlreadyPlaying();
                                            configAudioStreamer();

                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Radio>> call, Throwable t) {
                                        Toast.makeText( RadioActivity.this, "Check internet connection and try again", Toast.LENGTH_LONG ).show();
                                        Logger.e( TAG, t, "Network" );
                                    }
                                } );
                            }
                        }, throwable -> Logger.i( TAG, throwable.getLocalizedMessage() )
                );
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
            Logger.e( TAG, e, "onStart" );
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e( TAG, e, "onStop" );
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                if (radio1 != null) {
                    playPauseEvent( view );
                } else {
                    Toast.makeText( this, "Error in link", Toast.LENGTH_SHORT ).show();
                }
                break;
        }
    }

    private void playPauseEvent(View v) {
        if (radio1 == null) {
            Toast.makeText( RadioActivity.this, "Check internet connection and try again", Toast.LENGTH_LONG ).show();
        } else if (streamingManager.isPlaying()) {
            streamingManager.onPause();
            ((PlayPauseView) v).Pause();
        } else {
            streamingManager.onPlay( radio1 );
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
                if (radio1 != null) {
                    radio1.setPlayState( PlaybackStateCompat.STATE_PLAYING );
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                pgPlayPauseLayout.setVisibility( View.INVISIBLE );
                btnPlay.Pause();
                if (radio1 != null) {
                    radio1.setPlayState( PlaybackStateCompat.STATE_PAUSED );
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                radio1.setPlayState( PlaybackStateCompat.STATE_NONE );
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                pgPlayPauseLayout.setVisibility( View.INVISIBLE );
                btnPlay.Pause();
                if (radio1 != null) {
                    radio1.setPlayState( PlaybackStateCompat.STATE_NONE );
                }
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                pgPlayPauseLayout.setVisibility( View.VISIBLE );
                if (radio1 != null) {
                    radio1.setPlayState( PlaybackStateCompat.STATE_NONE );
                }
                break;
        }
    }

    @Override
    public void playSongComplete() {
        String playsongcomplete;
    }

    public void currentSeekBarPosition(int progress) {
    }

    @Override
    public void playCurrent(int indexP, Radio currentAudio) {
        showMediaInfo( currentAudio );
    }

    private void showMediaInfo(Radio currentAudio) {
    }

    public void playNext(int indexP, Radio currentAudio) {
        int progress = 10;

    }

    public void playPrevious(int indexP, Radio currentAudio) {
        int progress = 10;

    }

    private void playSong(Radio media) {
        if (streamingManager != null) {
            streamingManager.onPlay( media );
            radio.add( media );
            configAudioStreamer();
        }
    }

   /* private void checkAlreadyPlaying() {
        if (streamingManager.isPlaying()) {
//            radio1 = streamingManager.getCurrentAudio();
            if (radio1 != null) {
                radio1.setPlayState( streamingManager.mLastPlaybackState );
            }
        }
    }
*/

    private void safelyDispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        safelyDispose( disposable );
    }


}
