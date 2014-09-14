//TODO:������ʱ��ſ�   ����4.0����
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
 * Honeycomb Android 3.x ϵͳ�Ժ�,
 *     public final AsyncTask<Params, Progress, Result> execute(Params... params) {
 *            return executeOnExecutor(sDefaultExecutor, params);
 *  }
 * sDefaultExecutor�Ǵ��е�Executor��������Ҫ�Լ�ʹ��AsyncTask.THREAD_POOL_EXECUTOR��ʹ�ò���Executor
 * ������DefaultAsyncTaskExecInterface�е�һ����ֱ��ʹ��task.execute(args);
 * 
 * @author ����
 *
 */
@TargetApi(11)
public final class HoneycombAsyncTaskExecInterface implements AsyncTaskExecInterface {

  @Override
  public <T> void execute(AsyncTask<T,?,?> task, T... args) {
	//ʹ���첽�������̳߳صķ�ʽ     argsΪdoInBackgroud�Ĳ�����
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
  }

}
