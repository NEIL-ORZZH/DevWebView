/*
 * Copyright (C) 2008 ZXing authors
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

package com.czt.zxing;

import java.io.IOException;
import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.czt.devwebview.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.camera.BeepManager;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.camera.InactivityTimer;
import com.google.zxing.camera.decoding.CaptureActivityHandler;
import com.google.zxing.camera.decoding.FinishListener;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.ui.ViewfinderView;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = CaptureActivity.class.getSimpleName();


  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private boolean hasSurface;
  private Collection<BarcodeFormat> decodeFormats;
  private String characterSet;
  private InactivityTimer inactivityTimer;
  private BeepManager beepManager;

  public ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  public CameraManager getCameraManager() {
    return cameraManager;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Window window = getWindow();
    //设置屏幕亮 
    //as long as this window is visible to the user, keep the device's screen turned on and bright. 
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.capture);

    hasSurface = false;
    
    //用于静止时将Activity finish掉的定时器
    inactivityTimer = new InactivityTimer(this);
    //发声管理器
    beepManager = new BeepManager(this);


//    showHelpOnFirstLaunch();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = new CameraManager(getApplication());

    viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    viewfinderView.setCameraManager(cameraManager);


    handler = null;


    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
   // deprecated setting, but required on Android versions prior to 3.0
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
//  这个方法即使是通过home切回来的 也可以初始化播放器
    beepManager.updatePrefs();
//   
    inactivityTimer.onResume();
  }


  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    inactivityTimer.onPause();
    cameraManager.closeDriver();
    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
    super.onPause();
  }
/**
 * 停止异步任务，销毁广播接收者。
 * 在Activity上注册的广播接收者，所以Activity销毁，广播接收者也没了。
 */
  @Override
  protected void onDestroy() {
    inactivityTimer.shutdown();
    super.onDestroy();
  }
/**
 * 使用音量键控制是否打开闪光灯。
 */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_FOCUS:
      case KeyEvent.KEYCODE_CAMERA:
        // Handle these events so they don't launch the Camera app
        return true;
        
      // Use volume up/down to turn on light
      case KeyEvent.KEYCODE_VOLUME_DOWN:
    	  //打开闪光灯 手电筒 
        cameraManager.setTorch(false);
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
        cameraManager.setTorch(true);
        return true;
    }
    return super.onKeyDown(keyCode, event);
  }


  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }
/**
 * 处理解码。  对内容进行解码。先打印到log。
 * @param rawResult
 */
  //TODO:从这里返回。  setResult回Activity即可。
  public void handleDecode(Result rawResult) {
    inactivityTimer.onActivity();
    String contents = ResultParser.parseResult(rawResult).getDisplayResult().replace("\r", "");
    beepManager.playBeepSoundAndVibrate();
    Intent intent = new Intent();
    intent.putExtra("SCAN_RESULT", contents.toString());
    setResult(RESULT_OK, intent);
    finish();
  }
  public static String getScanResultFromIntent(Intent intent){
	  return intent.getStringExtra("SCAN_RESULT");
  }

  /**
   * 这个是很好的代码  每个版本第一次运行是显示帮助。
   * We want the help screen to be shown automatically the first time a new version of the app is
   * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
   * it to a value stored as a preference.
   */
//  private boolean showHelpOnFirstLaunch() {
//    try {
//      PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
//      int currentVersion = info.versionCode;
//      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//      int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
//      if (currentVersion > lastVersion) {
//        prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
//        Intent intent = new Intent(this, HelpActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        // Show the default page on a clean install, and the what's new page on an upgrade.
//        String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
//        intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
//        startActivity(intent);
//        return true;
//      }
//    } catch (PackageManager.NameNotFoundException e) {
//      Log.w(TAG, e);
//    }
//    return false;
//  }
/**
 * 初始化相机
 * @param surfaceHolder
 */
  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }
    //已开的话就不开了直接返回就可以
    if (cameraManager.isOpen()) {
      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
      return;
    }
    try {
      cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      displayFrameworkBugMessageAndExit();
    }
  }
  
  /**
   *  抱歉，Android相机出现问题。您可能需要重启设备
   */
  private void displayFrameworkBugMessageAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.app_name));
    builder.setMessage(getString(R.string.msg_camera_framework_bug));
    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }
}
