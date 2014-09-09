//TODO:发布的时候放开   并以4.0编译
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

import android.annotation.TargetApi;
import android.os.AsyncTask;


/**
 * Honeycomb Android 3.x 系统以后,
 *     public final AsyncTask<Params, Progress, Result> execute(Params... params) {
 *            return executeOnExecutor(sDefaultExecutor, params);
 *  }
 * sDefaultExecutor是串行的Executor。所以需要自己使用AsyncTask.THREAD_POOL_EXECUTOR来使用并行Executor
 * 不能向DefaultAsyncTaskExecInterface中的一样，直接使用task.execute(args);
 * 
 * @author 陈潼
 *
 */
@TargetApi(11)
public final class HoneycombAsyncTaskExecInterface implements AsyncTaskExecInterface {

  @Override
  public <T> void execute(AsyncTask<T,?,?> task, T... args) {
	//使用异步任务建立线程池的方式     args为doInBackgroud的参数。
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
  }

}
