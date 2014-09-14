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

package com.google.zxing.camera.open;

import com.google.zxing.common.PlatformSupportManager;

/**
 * Selects an appropriate implementation of {@link OpenCameraInterface} based on the device's
 * API level.
 */
/**
 * 通过版本控制，打开后置摄像头。    2.3版本及以后开始支持前置和后置摄像头
 * @author 陈潼
 *
 */
public final class OpenCameraManager extends PlatformSupportManager<OpenCameraInterface> {

  public OpenCameraManager() {
    super(OpenCameraInterface.class, new DefaultOpenCameraInterface());
    addImplementationClass(9, "com.google.zxing.camera.open.GingerbreadOpenCameraInterface");
  }

}
