/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.axionteq.onlineradio.radio;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;


public class NotificationManager {

    private static boolean DEBUG_VERSION = false;
    private static int totalEvents = 1;
    public static final int didReceivedNewMessages = totalEvents++;
    public static final int updateInterfaces = totalEvents++;
    static final int audioProgressDidChanged = totalEvents++;
    public static final int audioDidReset = totalEvents++;
    static final int audioPlayStateChanged = totalEvents++;

    public static final int screenshotTook = totalEvents++;
    public static final int albumsDidLoaded = totalEvents++;
    public static final int audioDidSent = totalEvents++;
    static final int audioDidStarted = totalEvents++;
    public static final int audioRouteChanged = totalEvents++;
    public static final int newaudioloaded = totalEvents++;
    static final int setAnyPendingIntent = totalEvents++;

    private SparseArray<ArrayList<Object>> observers = new SparseArray<>();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray<>();
    private ArrayList<DelayedPost> delayedPosts = new ArrayList<>( 10 );

    private int broadcasting = 0;
    private boolean animationInProgress;
    private static volatile NotificationManager Instance = null;


    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);

        void newSongLoaded(Object... args);
    }

    private class DelayedPost {
        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        private int id;
        private Object[] args;
    }


    static NotificationManager getInstance() {
        NotificationManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationManager();
                }
            }
        }
        return localInstance;
    }

    public void setAnimationInProgress(boolean value) {
        animationInProgress = value;
        if (!animationInProgress && !delayedPosts.isEmpty()) {
            for (DelayedPost delayedPost : delayedPosts) {
                postNotificationNameInternal( delayedPost.id, true, delayedPost.args );
            }
            delayedPosts.clear();
        }
    }

    void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        postNotificationNameInternal( id, allowDuringAnimation, args );
    }

    private void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (DEBUG_VERSION) {
            if (Thread.currentThread() != AudioStreamingManager.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException( "postNotificationName allowed only from MAIN thread" );
            }
        }
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPost delayedPost = new DelayedPost( id, args );
            delayedPosts.add( delayedPost );
            if (DEBUG_VERSION) {
                Log.e( "tmessages", "delay post notification " + id + " with args count = " + args.length );
            }
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get( id );
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get( a );
                ((NotificationCenterDelegate) obj).didReceivedNotification( id, args );
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int a = 0; a < removeAfterBroadcast.size(); a++) {
                    int key = removeAfterBroadcast.keyAt( a );
                    ArrayList<Object> arrayList = removeAfterBroadcast.get( key );
                    for (int b = 0; b < arrayList.size(); b++) {
                        removeObserver( arrayList.get( b ), key );
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int a = 0; a < addAfterBroadcast.size(); a++) {
                    int key = addAfterBroadcast.keyAt( a );
                    ArrayList<Object> arrayList = addAfterBroadcast.get( key );
                    for (int b = 0; b < arrayList.size(); b++) {
                        addObserver( arrayList.get( b ), key );
                    }
                }
                addAfterBroadcast.clear();
            }
        }
    }

    void addObserver(Object observer, int id) {
        if (DEBUG_VERSION) {
            if (Thread.currentThread() != AudioStreamingManager.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException( "addObserver allowed only from MAIN thread" );
            }
        }
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addAfterBroadcast.get( id );
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addAfterBroadcast.put( id, arrayList );
            }
            arrayList.add( observer );
            return;
        }
        ArrayList<Object> objects = observers.get( id );
        if (objects == null) {
            observers.put( id, (objects = new ArrayList<>()) );
        }
        if (objects.contains( observer )) {
            return;
        }
        objects.add( observer );
    }

    void removeObserver(Object observer, int id) {
        if (DEBUG_VERSION) {
            if (Thread.currentThread() != AudioStreamingManager.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException( "removeObserver allowed only from MAIN thread" );
            }
        }
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removeAfterBroadcast.get( id );
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeAfterBroadcast.put( id, arrayList );
            }
            arrayList.add( observer );
            return;
        }
        ArrayList<Object> objects = observers.get( id );
        if (objects != null) {
            objects.remove( observer );
        }
    }

    public void notifyNewSongLoaded(int id, Object... args) {
        ArrayList<Object> objects = observers.get( id );
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get( a );
                ((NotificationCenterDelegate) obj).newSongLoaded( args );
            }
        }
    }
}
