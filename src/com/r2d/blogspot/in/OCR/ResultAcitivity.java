package com.r2d.blogspot.in.OCR;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class ResultAcitivity extends Activity {
	private TextView resultview;
	private static String appid="##############"; //Enter your App ID
	String inputText="";
	String res="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_acitivity);

		inputText=getIntent().getStringExtra("res");
		resultview = (TextView) findViewById(R.id.textView1);

		new WolframFeed().execute();

	}


	private class WolframFeed extends AsyncTask<Void, Void, String>{
		private WAException exception;

		@Override
		protected String doInBackground(Void... params) {
			String result="";
			try {

				Log.e("TRYing", "wolfram try/");
				//System.out.println("Query URL:");
				//System.out.println(engine.toURL(query));
				//System.out.println("");

				WAEngine engine = new WAEngine();

				engine.setAppID(appid);
				engine.addFormat("plaintext");

				WAQuery query = engine.createQuery();
				query.setInput(inputText);


				WAQueryResult queryResult = engine.performQuery(query);


				if (queryResult.isError()) {

					String err= "Query error" + "  error code: " + queryResult.getErrorCode() + "  error message: " + queryResult.getErrorMessage();
					Log.e("err: ",err);

				} else if (!queryResult.isSuccess()) {
					Log.e("err: " ,"Query was not understood; no results available.");

				} else {
					// Got a result.
					Log.e("err: ","Successful query. Pods follow:\n");

					for (WAPod pod : queryResult.getPods()) {

						if (!pod.isError()) {
							result+="\n";
							for (WASubpod subpod : pod.getSubpods()) {
								for (Object element : subpod.getContents()) {
									if (element instanceof WAPlainText) {

										if(((WAPlainText) element).getText()!=""){
											result+=pod.getTitle();
											result+= ((WAPlainText) element).getText();
											result+="\n";
										}
									}
								}
							}
						}
					}

				}
			} catch (WAException e) {
				e.printStackTrace();

			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			resultview.setText(result);
		}

	}

}



