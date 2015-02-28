package com.powerall.fxhelper.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.powerall.fxhelper.R;
import com.powerall.fxhelper.SDKInitializer;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.StringUtil;


public class ShowUrlActivity extends ActionBarActivity {
    private String indexUrl = "http://weizhan360.com/site/index.jsp?site=13225&fn=13225";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取到notification传过来的url，然后显示出来
        String url = getIntent().getStringExtra("url");

        initView();
        init(url);

        SDKInitializer.initialize("larsonzhong", "123456", "nikAir", "fx88a", this);//这个初始化动作应该在其他地方调用，为了调试方便先在这里使用
    }

    private void initView() {
        TextView nameTV = (TextView) findViewById(R.id.title_name);
        String nickName = PreferenceUtil.getInstance(this).getPrefString(Constant.Preference.FX_NICKNAME, "");
        if (StringUtil.empty(nickName))
            nickName = this.getResources().getString(R.string.noName);
        nameTV.setText(nickName);
    }


    /**
     * @param url
     */
    private void init(String url) {
        if (StringUtil.empty(url))
            return;

        //1.获取一个webView
        webView = (WebView) findViewById(R.id.webView);
        //2.设置webView
        webView.getSettings().setJavaScriptEnabled(true);
        //3.设置显示的网页
        webView.loadUrl(url);
        //设置web视图,市值能够响应超链接
        webView.setWebViewClient(new CSWebViewClient());
    }

    private class CSWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
