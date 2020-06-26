package com.axionteq.onlineradio.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;


public class AudioStreamingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioStreamingManager audioStreamingManager = AudioStreamingManager.getInstance( context );
        if(audioStreamingManager ==null){
            return;
        }
        if (intent.getAction().equals( Intent.ACTION_MEDIA_BUTTON)) {
            if (intent.getExtras() == null) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get( Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (audioStreamingManager.isPlaying()) {
                        audioStreamingManager.onPause();
                    } else {
                        audioStreamingManager.onPlay( audioStreamingManager.getCurrentAudio());
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    audioStreamingManager.onPlay( audioStreamingManager.getCurrentAudio());
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    audioStreamingManager.onPause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    audioStreamingManager.onSkipToNext();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    audioStreamingManager.onSkipToPrevious();
                    break;
            }
        } else {
            audioStreamingManager = AudioStreamingManager.getInstance(context);
            switch (intent.getAction()) {
                case AudioStreamingService.NOTIFY_PLAY:
                    audioStreamingManager.onPlay( audioStreamingManager.getCurrentAudio() );
                    break;
                case AudioStreamingService.NOTIFY_PAUSE:
                case android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    audioStreamingManager.onPause();
                    break;
                case AudioStreamingService.NOTIFY_NEXT:
                    audioStreamingManager.onSkipToNext();
                    break;
                case AudioStreamingService.NOTIFY_CLOSE:
                    audioStreamingManager.cleanupPlayer( context, true, true );
                    break;
                case AudioStreamingService.NOTIFY_PREVIOUS:
                    audioStreamingManager.onSkipToPrevious();
                    break;
            }
        }
    }
}
