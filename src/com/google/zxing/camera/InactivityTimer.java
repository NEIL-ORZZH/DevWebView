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
	// Ĭ��5���ӣ���������ҳ������Ӻ��Զ��رա�ʡ�硣
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
	 * ִ���첽��������Ƿ��ھ�ֹ״̬��
	 */
	public synchronized void onActivity() {
		// ִ��ǰ��ȡ��֮ǰ���첽����
		cancel();
		inactivityTask = new InactivityAsyncTask();
		// ��Task���룬�����ڲ�����task��execute��executeOnExecutor����������ִ������
		taskExec.execute(inactivityTask);
	}

	/**
	 * ֹͣ��ȡ���㲥�����߼�����ȡ���첽����
	 */
	public void onPause() {
		// ȡ��������㲥�����ߡ�
		cancel();
		activity.unregisterReceiver(powerStatusReceiver);
	}

	/**
	 * ���ע��㲥�����ߡ������첽��������
	 */
	public void onResume() {
		// ����ʱע��㲥�����ߡ�
		activity.registerReceiver(powerStatusReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		onActivity();
	}

	/**
	 * ֱ��ȡ���첽���񣬲����첽����ֻΪnull
	 */
	private synchronized void cancel() {
		AsyncTask<?, ?, ?> task = inactivityTask;
		if (task != null) {
			task.cancel(true);
			inactivityTask = null;
		}
	}

	/**
	 * �رգ���ûȡ����Դ״̬�㲥������
	 */
	public void shutdown() {
		cancel();
	}

	/**
	 * ��Դ״̬�����Ĺ㲥�����ߡ�] �����Ƿ���ʹ�õ��
	 * 
	 * @author ����
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
					// ʹ�õ��
					InactivityTimer.this.onActivity();
				} else {
					// ����ʹ�õ��
					InactivityTimer.this.cancel();
				}
			}
		}
	}

	/**
	 * Ĭ��5���ӣ���������ҳ������Ӻ��Զ��رա�ʡ�硣
	 * 
	 * @author ����
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
