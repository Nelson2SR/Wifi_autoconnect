package com.appliedmesh.merchantapp.module;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;
import com.appliedmesh.merchantapp.R;

import java.io.IOException;

/**
 * Manages beeps and vibrations
 */
final public class BeepManager {

    private static final String TAG = BeepManager.class.getSimpleName();

    public static final int TYPE_ALARM = 0;
    public static final int TYPE_NEW_ORDER = 1;
    private static final float BEEP_VOLUME = 1.0f;
    private static final long[]VIBRATE_PATTERN = {0L,100L,100L,100L,500L,100L,100L,100L,500L,100L,100L,100L};

    private final Activity activity;
    private MediaPlayer mediaPlayerAlarm;
    private MediaPlayer mediaPlayerNewOrder;

    public BeepManager(Activity activity) {
        this.activity = activity;
        // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
        // so we now play on the music stream.
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.mediaPlayerAlarm = buildMediaPlayer(activity, TYPE_ALARM);
        this.mediaPlayerNewOrder = buildMediaPlayer(activity, TYPE_NEW_ORDER);
    }

    public void playBeepSoundAndVibrate(int type) {
        switch (type) {
            case TYPE_ALARM:
                if (mediaPlayerAlarm != null) {
                    mediaPlayerAlarm.start();
                }
                break;
            case TYPE_NEW_ORDER:
                if (mediaPlayerNewOrder != null) {
                    mediaPlayerNewOrder.start();
                }
            default:
                break;
        }
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_PATTERN,-1);
    }

    private static MediaPlayer buildMediaPlayer(Context activity, int type) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer player) {
                                 player.seekTo(0);
                    }});

        AssetFileDescriptor file;
        switch (type) {
            case TYPE_ALARM:
                file = activity.getResources().openRawResourceFd(R.raw.alarm_beep);
                break;
            case TYPE_NEW_ORDER:
            default:
                file = activity.getResources().openRawResourceFd(R.raw.ring3);
                break;
        }
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        }
        return mediaPlayer;
    }

}
