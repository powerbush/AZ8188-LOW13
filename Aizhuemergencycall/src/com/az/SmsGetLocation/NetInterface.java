package com.az.SmsGetLocation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class NetInterface {

	// private Context context;
	// public NetInterface(Context context){
	// this.context=context;
	// }

	public void SendInfoToNet(String URIString, List<NameValuePair> paramss) {
		String strResult = "";// ȡ����Ӧ�Ľ��
		HttpPost httpRequest = new HttpPost(URIString);
		Log.i("log_via_liao", ""+URIString);
		try {
			/* ����HTTP request */
			httpRequest
					.setEntity(new UrlEncodedFormEntity(paramss, HTTP.UTF_8));
			/* ȡ��HTTP response */
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			Log.i("log_via_liao", ""+httpResponse);
			/* ��״̬��Ϊ200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* ȡ����Ӧ�ַ��� */
				strResult = EntityUtils.toString(httpResponse.getEntity());

				Log.i("log_via_liao", ""+strResult);
				Pattern p = Pattern.compile("true");
				Matcher m = p.matcher(strResult);
				while (m.find()) {
					Log.i("log_via_liao", "�ϴ��ɹ�");
					// Toast.makeText(this, "Login successfully",
					// Toast.LENGTH_LONG).show();
				}
			} else {

			}
		} catch (ClientProtocolException e) {
			// Toast.makeText( APNActivity.this, "Login Failed",
			// Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			// Toast.makeText( APNActivity.this, "Login Failed",
			// Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (Exception e) {
			// Toast.makeText( APNActivity.this, "Login Failed",
			// Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

	}
}
