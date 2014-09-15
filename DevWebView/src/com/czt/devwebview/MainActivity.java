package com.czt.devwebview;

import net.simonvt.menudrawer.MenuDrawer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.czt.zxing.CaptureActivity;

public class MainActivity extends Activity {
	private MenuDrawer mDrawer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDrawer = MenuDrawer.attach(this);
		mDrawer.setContentView(R.layout.activity_main);
		mDrawer.setMenuView(R.layout.menu_drawer_left);
		initView();
	}
	@SuppressLint("SetJavaScriptEnabled")
	private void initView() {
		initViewById();
		mClearView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mUrlView.setText("");
			}
		});
		mUrlView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				if(str.length() > 0){
					showClearAndForward();
				}else{
					dismissClearAndForward();
				}
			}
		});
		mUrlView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = mUrlView.getText().toString();
				
				if(str.length() > 0){
					showClearAndForward();
				}else{
					dismissClearAndForward();
				}
			}
		});
		mUrlView.setOnEditorActionListener(new OnEditorActionListener() {  
	        @Override  
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
	        	loadUrl();
	            return true;  
	        }  
	    });
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mLoadingView.setVisibility(View.VISIBLE);
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				mLoadingView.setVisibility(View.GONE);
				showRefresh();
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				String consoleStr = consoleMessage.lineNumber() + ":" + consoleMessage.message();
				appendConsoleLog(consoleStr);
				return super.onConsoleMessage(consoleMessage);
			}

			private void appendConsoleLog(String consoleStr) {
				mLog.append(consoleStr);
				mLog.append("\n");
				mConsoleLog.setText(mLog.toString());
			}
		});
		mForwardView.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						loadUrl();
					}
				});
		mQRCodeScanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivityForResult(intent, REQUEST_CODE_CAPTURE_ACTIVITY);
			}
		});
		mRefreshView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mWebView.reload();
			}
		});
		mClearLog.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsoleLog.setText("");
				mLog = new StringBuilder();
			}
		});
	}
	
	private void loadUrl() {
		CharSequence url = mUrlView.getText();
		if (!TextUtils.isEmpty(url)) {
			mCurrentUrl = url.toString();
			if(!mCurrentUrl.contains("http://")){
				mCurrentUrl = "http://" + mCurrentUrl;
			}
			mWebView.loadUrl(mCurrentUrl);
		}
	}
	
	private EditText mUrlView;
	private WebView mWebView;
	private View mClearView;
	private View mForwardView;
	private View mQRCodeScanner;
	private View mRefreshView;
	private View mLoadingView;
	private TextView mConsoleLog;
	private View mClearLog;
	private StringBuilder mLog = new StringBuilder();
	private String mCurrentUrl = "";
	private void initViewById() {
		mUrlView = (EditText) findViewById(R.id.url_text);
		mWebView = (WebView) findViewById(R.id.webview);
		mClearView = findViewById(R.id.clear);
		mForwardView = findViewById(R.id.forward);
		mRefreshView = findViewById(R.id.refresh);
		mQRCodeScanner = findViewById(R.id.qrcode_scanner);
		mLoadingView = findViewById(R.id.loading);
		mConsoleLog = (TextView) findViewById(R.id.console_log);
		mClearLog =  findViewById(R.id.clear_log);
	}
	private void showRefresh(){
		mForwardView.setVisibility(View.GONE);
		mClearView.setVisibility(View.GONE);
		mRefreshView.setVisibility(View.VISIBLE);
	}
	private void showClearAndForward(){
		mForwardView.setVisibility(View.VISIBLE);
		mClearView.setVisibility(View.VISIBLE);
		mRefreshView.setVisibility(View.GONE);
	}
	private void dismissClearAndForward(){
		mForwardView.setVisibility(View.GONE);
		mClearView.setVisibility(View.GONE);
		mRefreshView.setVisibility(View.GONE);
	}
	private static final int REQUEST_CODE_CAPTURE_ACTIVITY = 10;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_CAPTURE_ACTIVITY && resultCode == RESULT_OK){
			mCurrentUrl = CaptureActivity.getScanResultFromIntent(data);
			mUrlView.setText(mCurrentUrl);
			mWebView.loadUrl(mCurrentUrl);
		}
	}
}
