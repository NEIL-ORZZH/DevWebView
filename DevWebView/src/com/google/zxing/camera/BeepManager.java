/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.camera;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.czt.devwebview.R;
import com.czt.zxing.CaptureActivity;

/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 * 管理声音和震动
 */
public final class BeepManager {

  private static final String TAG = BeepManager.class.getSimpleName();
  //是否需要在扫描到二维条形码并进行解析时震动。
  private static final boolean  VIBRATE = true;

  private static final float BEEP_VOLUME = 0.10f;
  private static final long VIBRATE_DURATION = 200L;

  private final Activity activity;
  private MediaPlayer mediaPlayer;
  private boolean playBeep;
  private boolean vibrate;

  public BeepManager(Activity activity) {
    this.activity = activity;
    this.mediaPlayer = null;
    updatePrefs();
  }
  
  /**
   * 建立媒体播放器
   * @param activity
   * @return
   */
  private static MediaPlayer buildMediaPlayer(Context activity) {
	  MediaPlayer mediaPlayer = new MediaPlayer();
	  mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	  // When the beep has finished playing, rewind to queue up another one.
	  mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		  @Override
		  public void onCompletion(MediaPlayer player) {
			  player.seekTo(0);
		  }
	  });
	  
	  AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep);
	  try {
		  mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
		  file.close();
		  mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
		  mediaPlayer.prepare();
	  } catch (IOException ioe) {
		  Log.w(TAG, ioe);
		  mediaPlayer = null;
	  }
	  return mediaPlayer;
  }
/**
 * 判断当前是否可以发声    shouldBeep
 * 
 * @param prefs
 * @param activity
 * @return
 */
  private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
	  boolean shouldPlayBeep = true;
	  if (shouldPlayBeep) {
		  // See if sound settings overrides this
		  AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		  if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			  shouldPlayBeep = false;
		  }
	  }
	  return shouldPlayBeep;
  }
  /**
   * 更新信息，如果可以发声，初始化播放器。  调用了shouldBeep和buildMediaPlayer
   */
  public void updatePrefs() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    playBeep = shouldBeep(prefs, activity);
    vibrate = VIBRATE;
    if (playBeep && mediaPlayer == null) {
      // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
      // so we now play on the music stream.
      activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
      mediaPlayer = buildMediaPlayer(activity);
    }
  }

  /**
   * 利用媒体播放器播放声音并产生震动。
   */
  public void playBeepSoundAndVibrate() {
	  if (playBeep && mediaPlayer != null) {
		  mediaPlayer.start();
	  }
	  if (vibrate) {
		  Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
		  vibrator.vibrate(VIBRATE_DURATION);
	  }
  }

}
