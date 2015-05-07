package com.nhn.android.oauth.ui;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginDefine;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.nhn.android.oauth.R;

/// 네이버 아이디로 로그인 샘플앱
/**
 * <br/> OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제, 
 * <br/> 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 * @author naver
 * 
 */
public class OAuthSampleActivity extends Activity {

	private static final String TAG = "OAuthSampleActivity";
	
	private static final String TAG2 = "git Test";
	private static final String TAG3 = "오리지널";
	/**
	 * client 정보를 넣어준다.
	 */
	private static String OAUTH_CLIENT_ID = "0fuxgAtAN1J4xGJEKBZy";  // Y2WVjwgjeO32kdtj4QPR      jyvqXeaVOVmV
	private static String OAUTH_CLIENT_SECRET = "Ei4YwTdqaz"; // 8sp4bCiJs1    527300A0_COq1_XV33cf
	private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인"; 
	private static String OAUTH_CALLBACK_INTENT = "http://static.nid.naver.com/oauth/naverOAuthExp.nhn";	


	private static OAuthLogin mOAuthLoginInstance;
	private static Context mContext;

	/** UI 요소들 */
	private TextView mApiResultText;
	private static TextView mOauthAT;
	private static TextView mOauthRT;
	private static TextView mOauthExpires;
	private static TextView mOauthTokenType;
	private static TextView mOAuthState;  

	private OAuthLoginButton mOAuthLoginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.naveroauthlogin_sample_main);

		OAuthLoginDefine.DEVELOPER_VERSION = true;

		mContext = this;

		initData();
		initView(); 

		this.setTitle("OAuthLoginSample Ver." + OAuthLogin.getVersion());
	}


	private void initData() {
		mOAuthLoginInstance = OAuthLogin.getInstance();
		mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_CALLBACK_INTENT);
	}

	private void initView() {
		mApiResultText = (TextView) findViewById(R.id.api_result_text);

		mOauthAT = (TextView) findViewById(R.id.oauth_access_token);
		mOauthRT = (TextView) findViewById(R.id.oauth_refresh_token);
		mOauthExpires = (TextView) findViewById(R.id.oauth_expires);
		mOauthTokenType = (TextView) findViewById(R.id.oauth_type);
		mOAuthState = (TextView) findViewById(R.id.oauth_state);

		mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
		mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);	

		updateView();
	}


	private void updateView() {
		mOauthAT.setText(mOAuthLoginInstance.getAccessToken(mContext));
		mOauthRT.setText(mOAuthLoginInstance.getRefreshToken(mContext));
		mOauthExpires.setText(String.valueOf(mOAuthLoginInstance.getExpiresAt(mContext)));
		mOauthTokenType.setText(mOAuthLoginInstance.getTokenType(mContext));
		mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
	}

	@Override
	protected void onResume() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		super.onResume();

	}

	/**
	 * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는 걸 알 수 있다. 
	 */
	static private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
		@Override
		public void run(boolean success) {
			if (success) {
				Log.e("TAG", "로그인된거임???");
				String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
				String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
				long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
				String tokenType = mOAuthLoginInstance.getTokenType(mContext);
				mOauthAT.setText(accessToken);
				mOauthRT.setText(refreshToken);
				mOauthExpires.setText(String.valueOf(expiresAt));
				mOauthTokenType.setText(tokenType);
				mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
			} else {
				String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
				String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
				Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
			}
		};
	};

	public void onButtonClick(View v) throws Throwable {

		switch (v.getId()) {
		case R.id.buttonOAuth: {
			Log.e("TAG", "인증하기");
			mOAuthLoginInstance.startOauthLoginActivity(OAuthSampleActivity.this, mOAuthLoginHandler);
			break;
		}
		case R.id.buttonVerifier: {
			Log.e("TAG", "API호출");
			new RequestApiTask().execute();
			break;
		}
		case R.id.buttonRefresh: {
			Log.e("TAG", "토큰다시받기");
			new RefreshTokenTask().execute();
			break;  
		}
		case R.id.buttonOAuthLogout: {  
			Log.e("TAG", "로그아웃");
			mOAuthLoginInstance.logout(mContext);
			updateView();
			break;
		}
		case R.id.buttonOAuthDeleteToken: {
			Log.e("TAG", "연동끊기");
			new DeleteTokenTask().execute();
			break;
		}
		default:
			break;
		}
	}  


	private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			boolean isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(mContext);

			if (!isSuccessDeleteToken) {
				// 서버에서 token 삭제에 실패했어도 클라이언트에 있는 token 은 삭제되어 로그아웃된 상태이다 
				// 실패했어도 클라이언트 상에 token 정보가 없기 때문에 추가적으로 해줄 수 있는 것은 없음 
				Log.d(TAG, "errorCode:" + mOAuthLoginInstance.getLastErrorCode(mContext));
				Log.d(TAG, "errorDesc:" + mOAuthLoginInstance.getLastErrorDesc(mContext));
			}

			return null;
		}
		protected void onPostExecute(Void v) {
			updateView();
		}
	}

	private class RequestApiTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			mApiResultText.setText((String) "");
		}
		@Override
		protected String doInBackground(Void... params) {
			String url = "https://apis.naver.com/nidlogin/nid/getUserProfile.xml";
			String at = mOAuthLoginInstance.getAccessToken(mContext);
			return mOAuthLoginInstance.requestApi(mContext, at, url);
		}
		protected void onPostExecute(String content) {
			mApiResultText.setText((String) content);
			Log.e("TAG" , "API호출 = " + content);
			test(content);
		}
	}

	private void test(String data) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			XmlPullParser parser = factory.newPullParser();
			String sTag = "";
			parser.setInput(new StringReader(data));
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.e("TAG", " START_DOCUMENT" );
					break;

				case XmlPullParser.END_DOCUMENT: 
					Log.e("TAG", " END_DOCUMENT" );
					break;

				case XmlPullParser.START_TAG:
					sTag = parser.getName();
//					Log.e("TAG", " START_TAG1 = " + sTag );
					break;

				case XmlPullParser.END_TAG:
					Log.e("TAG", " END_TAG" );
					break;

				case XmlPullParser.TEXT:
					Log.e("TAG", " START_TAG2 = " + sTag );
					Log.e("TAG", " getText = " + parser.getText());
					break;

				default:
					break;
				}

				eventType = parser.next();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class RefreshTokenTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			return mOAuthLoginInstance.refreshAccessToken(mContext);
		}
		protected void onPostExecute(String res) {
			updateView();
		}
	}
}