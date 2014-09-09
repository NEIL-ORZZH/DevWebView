/*
 * Copyright (C) 2012 ZXing authors
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

package com.google.zxing.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import android.os.Build;
import android.util.Log;

/**
 * <p>
 * Sometimes the application wants to access advanced functionality exposed by
 * Android APIs that are only available in later versions of the platform. While
 * {@code Build.VERSION} can be used to determine the device's API level and
 * alter behavior accordingly, and it is possible to write code that uses both
 * old and new APIs selectively, such code would fail to load on older devices
 * that do not have the new API methods.
 * </p>
 * 
 * <p>
 * It is necessary to only load classes that use newer APIs than the device may
 * support after the app has checked the API level. This requires reflection,
 * loading one of several implementations based on the API level.
 * </p>
 * 
 * <p>
 * This class manages that process. Subclasses of this class manage access to
 * implementations of a given interface in an API-level-aware way. Subclasses
 * implementation classes <em>by name</em>, and the minimum API level that the
 * implementation is compatible with. They also provide a default
 * implementation.
 * </p>
 * 
 * <p>
 * At runtime an appropriate implementation is then chosen, instantiated and
 * returned from {@link #build()}.
 * </p>
 * 
 * @param <T>
 *            the interface which managed implementations implement
 */
/**
 * ͨ���汾���Ʒ��ص��첽����ִ��������֤�ǲ���ִ�е��첽����ִ������
 * �����ǰ��첽����ִ��������PlatformSupportManager<AsyncTaskExecInterface>���н��͵ġ�
 * ����ʵ����һ��ƽ̨�汾�жϵĹ����ࡣ ����ͨ�������̳���������չ��  ���������Ŀ�е�com.google.zxing.camera.open���ڵ�Ҳ��ͨ������������Ƶġ�
 * ��Ϊ2.3��ʼ֧��ǰ������ͷ���������˰汾�жϡ�
 * @author ����
 *
 * @param <T>
 */
public abstract class PlatformSupportManager<T> {

	private static final String TAG = PlatformSupportManager.class
			.getSimpleName();

	private final Class<T> managedInterface;
	private final T defaultImplementation;
	private final SortedMap<Integer, String> implementations;

	protected PlatformSupportManager(Class<T> managedInterface,
			T defaultImplementation) {
		if (!managedInterface.isInterface()) {
			throw new IllegalArgumentException();
		}
		if (!managedInterface.isInstance(defaultImplementation)) {
			throw new IllegalArgumentException();
		}
		this.managedInterface = managedInterface;
		this.defaultImplementation = defaultImplementation;
		// �������հ汾�ŴӴ�С����
		this.implementations = new TreeMap<Integer, String>(
				Collections.reverseOrder());
	}
	/**
	 * ����ʵ�ֵ��첽������ִ����ӡ�
	 * @param minVersion ֧�ֵ���С�汾
	 * @param className �첽������ִ�� ����
	 */
	protected void addImplementationClass(int minVersion, String className) {
		implementations.put(minVersion, className);
	}
/**
 * ͨ���汾�жϷ��ز�������ִ������
 * @return ��������ִ����
 */
	public T build() {
		for (Integer minVersion : implementations.keySet()) {
			// �жϵ�ǰ�ֻ��汾�Ƿ����implementations��ŵ�minVersion���ǵĻ�ִ�У����򷵻�Ĭ�ϵ��첽ִ������
			// Ŀǰֻ�ж����ǲ��Ǵ���3.0
			// Ϊʲôimplementationsʹ���˴Ӵ�С����
			// ���ڽ����������������µĸ߰汾�жϣ�ֱ�ӷ��ء�
			// ����ֵ��ѧϰ �汾���ơ�
			if (Build.VERSION.SDK_INT >= minVersion) {
				String className = implementations.get(minVersion);
				try {
					// asSubclass��Casts this Class to represent a subclass of
					// the specified
					// class. If successful, this Class is returned;
					// otherwise a ClassCastException is thrown.
					Class<? extends T> clazz = Class.forName(className)
							.asSubclass(managedInterface);
					Log.i(TAG, "Using implementation " + clazz + " of "
							+ managedInterface + " for SDK " + minVersion);
					return clazz.getConstructor().newInstance();
				} catch (ClassNotFoundException cnfe) {
					Log.w(TAG, cnfe);
				} catch (IllegalAccessException iae) {
					Log.w(TAG, iae);
				} catch (InstantiationException ie) {
					Log.w(TAG, ie);
				} catch (NoSuchMethodException nsme) {
					Log.w(TAG, nsme);
				} catch (InvocationTargetException ite) {
					Log.w(TAG, ite);
				}
			}
		}
		Log.i(TAG,
				"Using default implementation "
						+ defaultImplementation.getClass() + " of "
						+ managedInterface);
		return defaultImplementation;
	}

}
