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

package com.google.zxing.common.executor;

import android.os.AsyncTask;
/**
 * 异步任务的执行接口  实现的功能是传入异步任务以及执行时需要的参数
 * @author 陈潼
 *
 */
public interface AsyncTaskExecInterface {

  <T> void execute(AsyncTask<T,?,?> task, T... args);

}
