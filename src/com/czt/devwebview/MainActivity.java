package com.czt.devwebview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.czt.zxing.CaptureActivity;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		method1();
	}
	private EditText mUrlText;
	private WebView mWebView;
	private void method1() {
		setContentView(R.layout.activity_main);
		
		mUrlText = (EditText) findViewById(R.id.url_text);
		
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		findViewById(R.id.button_view).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						CharSequence url = mUrlText.getText();
						if (!TextUtils.isEmpty(url)) {
							mWebView.loadUrl(url.toString());
						}
					}
				});
		findViewById(R.id.qrcode_camera).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivityForResult(intent, REQUEST_CODE_CAPTURE_ACTIVITY);
			}
		});
	}
	private static final int REQUEST_CODE_CAPTURE_ACTIVITY = 10;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_CAPTURE_ACTIVITY && resultCode == RESULT_OK){
			String url = CaptureActivity.getScanResultFromIntent(data);
			mUrlText.setText(url);
			mWebView.loadUrl(url);
		}
	}
	private void method2() {
		setContentView(R.layout.activity_main2);

		WebView webview = (WebView) findViewById(R.id.webview);
		webview.setWebViewClient(new WebViewClient());
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webview.loadUrl("http://192.168.1.20/pages/client/comment.html?kb_guid=ba77a399-c923-46f7-b7c4-9d86a8908852&document_guid=231923e6-882f-47dd-96f6-4653b12881d5&token=2268fe989c0764c87499edb696b22403a1r4vo8x9y93dw");
	}
}
