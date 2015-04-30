package com.okan.tcdogrulama;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.Locale;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String NAMESPACE = "http://tckimlik.nvi.gov.tr/WS";
	private static final String URL = "https://tckimlik.nvi.gov.tr/Service/KPSPublic.asmx?WSDL";	
	private static final String SOAP_ACTION = "http://tckimlik.nvi.gov.tr/WS/TCKimlikNoDogrula";
	private static final String METHOD_NAME = "TCKimlikNoDogrula";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Uyarı!");
		alertDialog.setMessage("Lütfen Büyük ve Türkçe karakter ile yazın. \nör: OKANCAN COŞAR");
		alertDialog.setButton("Anladım..", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}});
		alertDialog.show();
	}



	public void controlTCKN (View view) {

		EditText tcknArea = (EditText) findViewById(R.id.tcknArea);
		String tckn = tcknArea.getText().toString().trim();
		EditText nameArea = (EditText) findViewById(R.id.nameArea);
		String name = nameArea.getText().toString().trim().toUpperCase(new Locale("tr_TR"));   	
		EditText surnameArea = (EditText) findViewById(R.id.surnameArea);
		String surname = surnameArea.getText().toString().trim().toUpperCase(new Locale("tr_TR"));   	
		EditText birthDateArea = (EditText) findViewById(R.id.yil);
		String year = birthDateArea.getText().toString().trim();

		if(checkInternetConnection()){
			if(name.isEmpty() || surname.isEmpty() ||  tckn.isEmpty()){
				Toast.makeText(MainActivity.this,"Lütfen gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();   		
			}else {
				new controlTCKNService().execute(tckn, name, surname, year);
			}       	
		} else {
			Toast.makeText(MainActivity.this,"Lütfen internet bağlantınızı kontrol ediniz", Toast.LENGTH_SHORT).show(); 
		}
	}

	private boolean checkInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean result = false;
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	private class controlTCKNService extends AsyncTask<String, Void, Void> {

		private String resultText;
		private boolean result;
		private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
		private AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		AlertDialog alert;
		protected void onPreExecute() {
			progressDialog.setMessage("Kontrol ediliyor...");
			progressDialog.show();


		}

		protected Void doInBackground(String... urls) {
			SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
			Request.addProperty("TCKimlikNo",urls[0]);
			Request.addProperty("Ad", urls[1]);
			Request.addProperty("Soyad", urls[2]);
			Request.addProperty("DogumYili", urls[3]);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(Request);
			AndroidHttpTransport androidHttpTransport = new AndroidHttpTransport(URL);

			try
			{
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject response = (SoapObject) envelope.bodyIn;
				result = Boolean.parseBoolean( response.getProperty(0).toString());
				if(result) {
					resultText ="TC Kimlik Numarası Geçerli";       	
				}else{
					resultText = "TC Kimlik Numarası Geçersiz";  

				}
			}
			catch(ClassCastException e){
				result = false;
				resultText = "TC Kimlik Numarası Geçersiz"; 
			}
			catch(ConnectException e){
				result = false;
				resultText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir."; 
			}
			catch (UnknownHostException e) {
				result = false;
				resultText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
			}
			catch (UnknownServiceException e) {
				result = false;
				resultText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
			}            
			catch(Exception e){
				result = false;
				resultText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
			}	    	

			return null;
		}

		protected void onPostExecute(Void unused) {

			progressDialog.dismiss();
			alert = builder.setMessage(resultText)
					.setCancelable(true)
					.setTitle("Sonuç")
					.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					}).create();
			alert.show();

		}

	}



}