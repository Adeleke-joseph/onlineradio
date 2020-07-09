package com.axionteq.onlineradio.radio;



public abstract class StreamingManager {

    public abstract void onPlay(Radio infoData);

    public abstract void onPause();

    public abstract void onStop();
/*    public abstract void onSeekTo(long position);
    public abstract int lastSeekPosition();
    public abstract void onSkipToNext();
    public abstract void onSkipToPrevious();*/
}
