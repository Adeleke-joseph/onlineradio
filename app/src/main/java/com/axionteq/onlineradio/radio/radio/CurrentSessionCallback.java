/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package com.axionteq.onlineradio.radio.radio;


public interface CurrentSessionCallback {
    void updatePlaybackState(int state);

    void playSongComplete();

    void currentSeekBarPosition(int progress);

    void playCurrent(int indexP, RadioType currentAudio);

    void playNext(int indexP, RadioType currentAudio);

    void playPrevious(int indexP, RadioType currentAudio);
}
