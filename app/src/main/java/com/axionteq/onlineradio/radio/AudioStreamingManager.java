package com.axionteq.onlineradio.radio;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class AudioStreamingManager extends StreamingManager {

    private static final String TAG = Logger.makeLogTag( AudioStreamingManager.class );
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    static volatile Handler applicationHandler = null;
    @SuppressLint("StaticFieldLeak")
    private static volatile AudioStreamingManager instance = null;
    private final Handler mHandler = new Handler();
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private PendingIntent pendingIntent;
    private int mLastPlaybackState;
    private AudioPlaybackListener audioPlayback;
    private CurrentSessionCallback currentSessionCallback;
//    private final Runnable mUpdateProgressTask = this::updateProgress;
    private Context context;
    private int index = 0;
    private boolean playMultiple = false;
    private boolean showPlayerNotification = true;
    private Radio currentAudio;
    private List<Radio> mediaList = new ArrayList<>();
    private ScheduledFuture<?> mScheduleFuture;
    private long currentPosition = 0;
    private int duration = 0;


    static AudioStreamingManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AudioStreamingManager.class) {
                instance = new AudioStreamingManager();
                instance.context = context;
                instance.audioPlayback = new AudioPlaybackListener( context );
                instance.audioPlayback.setCallback( new MyStatusCallback() );
                applicationHandler = new Handler( context.getMainLooper() );
            }
        }
        return instance;
    }

    void subscribesCallBack(CurrentSessionCallback callback) {
        this.currentSessionCallback = callback;
    }

    public int getCurrentIndex() {
        return this.index;
    }

    void unSubscribeCallBack() {
        this.currentSessionCallback = null;
    }

    Radio getCurrentAudio() {
        return currentAudio;
    }

 /*   public int getDuration(){
        duration = audioPlayback.getCurrentTotalDuration();
        return duration;
}*/

    public String getCurrentAudioId() {
        return currentAudio != null ? currentAudio.getId() : "";
    }

    public boolean isPlayMultiple() {
        return playMultiple;
    }

    void setPlayMultiple() {
        this.playMultiple = false;
    }

    boolean isPlaying() {
        return instance.audioPlayback.isPlaying();
    }

    void setPendingIntentAct(PendingIntent mPendingIntent) {
        this.pendingIntent = mPendingIntent;
    }

    void setShowPlayerNotification() {
        this.showPlayerNotification = true;
    }

    void setMediaList(List<Radio> currentAudioList) {
        if (this.mediaList != null) {
            this.mediaList.clear();
            this.mediaList.addAll( currentAudioList  );
        }
    }

    public void clearList() {
        if (this.mediaList != null && mediaList.size() > 0) {
            this.mediaList.clear();
            this.index = 0;
            this.onPause();
        }
    }

    @Override
    public void onPlay(Radio infoData) {
        if (infoData == null) {
            return;
        }
        if (playMultiple && !isMediaListEmpty()) {
            index = mediaList.indexOf( infoData );
        }
        if (this.currentAudio != null
              //  && this.currentAudio.getId().equalsIgnoreCase( infoData.getId() )
                && instance.audioPlayback != null && instance.audioPlayback.isPlaying()) {
            onPause();
        } else {
            this.currentAudio = infoData;
            handlePlayRequest();
            if (currentSessionCallback != null)
                currentSessionCallback.playCurrent( index, currentAudio );
        }
    }

    @Override
    public void onPause() {
        handlePauseRequest();
    }

    @Override
    public void onStop() {
        handleStopRequest( null );
    }

 /*   public void onSeekTo(long position) {
//        audioPlayback.seekTo( (int) position );
    }

    public int lastSeekPosition() {
//        return (audioPlayback == null) ? 0 : (int) audioPlayback.getCurrentStreamPosition();
        return 0;
    }

    public void onSkipToNext() {
        int nextIndex = index + 1;
        if (isValidIndex( true, nextIndex )) {
            Radio metaData = mediaList.get( nextIndex );
            onPlay( metaData );
            if (instance.currentSessionCallback != null) {
                currentSessionCallback.playNext( nextIndex, metaData );
            }
        }
    }

    void onSkipToPrevious() {
       int prvIndex = index - 1;
        if (isValidIndex( false, prvIndex )) {
            Radio metaData = mediaList.get( prvIndex );
            onPlay( metaData );
            if (instance.currentSessionCallback != null) {
                currentSessionCallback.playPrevious( prvIndex, metaData );
            }
        }*//*
    }*/

    /**
     * @return
     */
    public boolean isMediaListEmpty() {
        return (mediaList == null || mediaList.size() == 0);
    }

     /**
     * @param isIncremental
     * @return
     */
    /**
    private boolean isValidIndex(boolean isIncremental, int index) {
        if (isIncremental) {
            return (playMultiple && !isMediaListEmpty() && mediaList.size() > index);
        } else {
            return (playMultiple && !isMediaListEmpty() && index >= 0);
        }
    }
     **/


    private void handlePlayRequest() {
        Logger.d( TAG, "handlePlayRequest: mState=" + audioPlayback.getState() );
        if (audioPlayback != null && currentAudio != null) {
            audioPlayback.play( currentAudio );
            if (showPlayerNotification) {
                if (context != null) {
                    Intent intent = new Intent( context, AudioStreamingService.class );
                    context.startService( intent );
                } else {
                    Intent intent = new Intent( context, AudioStreamingService.class );
                    context.stopService( intent );
                }

                NotificationManager.getInstance().postNotificationName( NotificationManager.audioDidStarted, currentAudio );
                NotificationManager.getInstance().postNotificationName( NotificationManager.audioPlayStateChanged, getCurrentAudio().getId() );
                setPendingIntent();
            }
        }
    }

    private void setPendingIntent() {
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                if (pendingIntent != null) {
                    NotificationManager.getInstance().postNotificationName( NotificationManager.setAnyPendingIntent, pendingIntent );
                }
            }
        }, 400 );
    }

    void handlePauseRequest() {
        Logger.d( TAG, "handlePauseRequest: mState=" + audioPlayback.getState() );
        if (audioPlayback != null && audioPlayback.isPlaying()) {
            audioPlayback.pause();
            if (showPlayerNotification) {
                NotificationManager.getInstance().postNotificationName( NotificationManager.audioPlayStateChanged, getCurrentAudio().getId() );
            }
        }
    }

    private void handleStopRequest(String withError) {
        Logger.d( TAG, "handleStopRequest: mState=" + audioPlayback.getState() + " error=", withError );
        audioPlayback.stop( true );
    }

    void cleanupPlayer(Context context, boolean notify, boolean stopService) {
        cleanupPlayer( notify, stopService );
    }

    private void cleanupPlayer(boolean notify, boolean stopService) {
        handlePauseRequest();
        audioPlayback.stop( true );
        if (stopService) {
            Intent intent = new Intent( context, AudioStreamingService.class );
            context.stopService( intent );
        }
    }

    /*public void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post( mUpdateProgressTask );
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS );
        }
    }   private void stopSeekBarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel( false );
        }
    }

    private void updateProgress() {
        if (instance.mLastPlaybackState == 0 || instance.mLastPlaybackState < 0) {
            return;
        }
        if (instance.mLastPlaybackState != PlaybackStateCompat.STATE_PAUSED && instance.currentSessionCallback != null) {
            instance.currentSessionCallback.currentSeekBarPosition( (int) audioPlayback.getCurrentStreamPosition() );
        }
    }*/

    static class MyStatusCallback implements PlaybackListener.Callback {
        @Override
        public void onCompletion() {
            if (instance.playMultiple && !instance.isMediaListEmpty()) {
                if (instance.currentSessionCallback != null) {
                    instance.currentSessionCallback.playSongComplete();
                }
               // instance.onSkipToNext();
            } else {
                instance.handleStopRequest( null );
            }
        }

        @Override
        public void onPlaybackStatusChanged(int state) {
            try {
                if (instance.currentSessionCallback != null) {
                    instance.currentSessionCallback.updatePlaybackState( state );
                }

                instance.mLastPlaybackState = state;
                if (instance.currentAudio != null) {
                    instance.currentAudio.setPlayState( state );
                }
                if (instance.showPlayerNotification) {
                    NotificationManager.getInstance().postNotificationName( NotificationManager.audioPlayStateChanged, instance.getCurrentAudio().getId() );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            //TODO FOR ERROR
        }

        @Override
        public void setCurrentMediaId(String mediaId) {

        }

    }

}
