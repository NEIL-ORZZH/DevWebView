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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import com.google.zxing.common.executor.AsyncTaskExecInterface;
import com.google.zxing.common.executor.AsyncTaskExecManager;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 */
public final class InactivityTimer {

	private static final String TAG = InactivityTimer.class.getSimpleName();
	// 默认5分钟，即在拍照页面五分钟后自动关闭。省电。
	private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

	private final Activity activity;
	private final AsyncTaskExecInterface taskExec;
	private final BroadcastReceiver powerStatusReceiver;
	private InactivityAsyncTask inactivityTask;

	public InactivityTimer(Activity activity) {
		this.activity = activity;
		taskExec = new AsyncTaskExecManager().build();
		powerStatusReceiver = new PowerStatusReceiver();
		onActivity();
	}

	/**
	 * 执行异步任务监听是否处于静止状态。
	 */
	public synchronized void onActivity() {
		// 执行前先取消之前的异步任务。
		cancel();
		inactivityTask = new InactivityAsyncTask();
		// 将Task传入，并在内部调用task的execute或executeOnExecutor方法，并行执行任务。
		taskExec.execute(inactivityTask);
	}

	/**
	 * 停止。取消广播接受者监听、取消异步任务。
	 */
	public void onPause() {
		// 取消并解除广播接收者。
		cancel();
		activity.unregisterReceiver(powerStatusReceiver);
	}

	/**
	 * 激活：注册广播接受者。调用异步任务开启。
	 */
	public void onResume() {
		// 激活时注册广播接收者。
		activity.registerReceiver(powerStatusReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		onActivity();
	}

	/**
	 * 直接取消异步任务，并将异步任务只为null
	 */
	private synchronized void cancel() {
		AsyncTask<?, ?, ?> task = inactivityTask;
		if (task != null) {
			task.cancel(true);
			inactivityTask = null;
		}
	}

	/**
	 * 关闭，但没取消电源状态广播接收者
	 */
	public void shutdown() {
		cancel();
	}

	/**
	 * 电源状态监听的广播接收者。] 监听是否是使用电池
	 * 
	 * @author 陈潼
	 * 
	 */
	private final class PowerStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 0 indicates that we're on battery
				// values of the "plugged" field in the ACTION_BATTERY_CHANGED
				// intent.
				// These must be powers of 2.
				// /** Power source is an AC charger. */
				// public static final int BATTERY_PLUGGED_AC = 1;
				// /** Power source is a USB port. */
				// public static final int BATTERY_PLUGGED_USB = 2;
				boolean onBatteryNow = intent.getIntExtra(
						BatteryManager.EXTRA_PLUGGED, -1) <= 0;
				if (onBatteryNow) {
					// 使用电池
					InactivityTimer.this.onActivity();
				} else {
					// 不是使用电池
					InactivityTimer.this.cancel();
				}
			}
		}
	}

	/**
	 * 默认5分钟，即在拍照页面五分钟后自动关闭。省电。
	 * 
	 * @author 陈潼
	 * 
	 */
	private final class InactivityAsyncTask extends
			AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... objects) {
			try {
				Thread.sleep(INACTIVITY_DELAY_MS);
				Log.i(TAG, "Finishing activity due to inactivity");
				activity.finish();
			} catch (InterruptedException e) {
				// continue without killing
			}
			return null;
		}
	}

}
