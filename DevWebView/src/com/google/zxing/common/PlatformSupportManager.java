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
 * 通过版本控制返回的异步任务执行器。保证是并行执行的异步任务执行器。
 * 这里是按异步任务执行器，即PlatformSupportManager<AsyncTaskExecInterface>进行解释的。
 * 但其实这是一个平台版本判断的工具类。 可以通过其他继承来继续扩展。  例如这个项目中的com.google.zxing.camera.open包内的也是通过这个类来控制的。
 * 因为2.3开始支持前置摄像头，所以有了版本判断。
 * @author 陈潼
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
		// 逆序：依照版本号从大到小排列
		this.implementations = new TreeMap<Integer, String>(
				Collections.reverseOrder());
	}
	/**
	 * 其他实现的异步任务并行执行添加。
	 * @param minVersion 支持的最小版本
	 * @param className 异步任务并行执行 类名
	 */
	protected void addImplementationClass(int minVersion, String className) {
		implementations.put(minVersion, className);
	}
/**
 * 通过版本判断返回并行任务执行器。
 * @return 并行任务执行器
 */
	public T build() {
		for (Integer minVersion : implementations.keySet()) {
			// 判断当前手机版本是否大于implementations存放的minVersion，是的话执行，否则返回默认的异步执行器。
			// 目前只判断了是不是大于3.0
			// 为什么implementations使用了从大到小排序？
			// 便于今后在这里继续加入新的高版本判断，直接返回。
			// 这里值得学习 版本控制。
			if (Build.VERSION.SDK_INT >= minVersion) {
				String className = implementations.get(minVersion);
				try {
					// asSubclass：Casts this Class to represent a subclass of
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
