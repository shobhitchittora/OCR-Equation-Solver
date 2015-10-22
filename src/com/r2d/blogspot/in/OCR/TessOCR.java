package com.r2d.blogspot.in.OCR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR extends Activity {

	private ImageView b_gal;
	private ImageView b_cam;
	private Button resButton;
	private static final int REQUEST_GALLERY = 0;
	private static final int REQUEST_CAMERA = 1;

	private Uri imageUri;
	private TessBaseAPI baseAPI;

	private String resText=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tess_ocr);
		baseAPI = new TessBaseAPI();

		File extDir = Environment.getExternalStorageDirectory();
		File appDir = new File(extDir,"TessOCR");
		if(!appDir.isDirectory())
			appDir.mkdir();
		final File baseDir = new File(appDir,"tessdata");
		if(!baseDir.isDirectory())
			baseDir.mkdir();


		//Initializing baseAPI
		baseAPI.setDebug(true);
		baseAPI.setPageSegMode(TessBaseAPI.OEM_CUBE_ONLY);
		boolean test = baseAPI.init(appDir.getPath(), "eng+equ"); //Equation training file
		if(test){Toast.makeText(getBaseContext(), "TESS Initialized", Toast.LENGTH_SHORT).show();}
		b_gal = (ImageView)findViewById(R.id.imageView2);
		b_gal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i= new Intent();
				i.setType("image/*");
				i.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(i, REQUEST_GALLERY);
			}
		});

		b_cam = (ImageView) findViewById(R.id.imageView1);
		b_cam.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String filename = "Tess - "+System.currentTimeMillis() + ".jpg";

				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.TITLE, filename);
				values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
				imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

				Intent i = new Intent();
				i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(i, REQUEST_CAMERA);
			}
		});
		resButton=(Button) findViewById(R.id.button1);
		
		resButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent resIntent= new Intent(getApplicationContext(),ResultAcitivity.class);
				if(resText!=null)
				resIntent.putExtra("res", resText);
				startActivity(resIntent);
		
			}
		});
	}

	private void inspectBMP(Bitmap bmp){
		baseAPI.setImage(bmp);
		String text = baseAPI.getUTF8Text();
		baseAPI.clear();

		//result_view = (TextView)findViewById(R.id.textView1);
		//result_view.setText(text);
		
		//text=text.replaceAll("[^a-zA-Z0-9]+", " ");
		//text=text.trim();
		
		this.resText=text;
		//bmp.recycle();
	}

	private void inspect (Uri uri){

		InputStream is = null;
		try{
			is = getContentResolver().openInputStream(uri);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inSampleSize = 4;
			options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
			Bitmap bmp= BitmapFactory.decodeStream(is, null, options);

			//Toast.makeText(getApplicationContext(), "Inside Inspect", Toast.LENGTH_SHORT).show();

			/*int pich=bmp.getHeight();
			int picw=bmp.getWidth();

			int[] pix= new int[picw * pich];
			bmp.getPixels(pix, 0, picw, 0, 0, picw,pich);

			Toast.makeText(getApplicationContext(), String.valueOf(pix.length), Toast.LENGTH_SHORT).show();
			for (int y = 0; y < pich; y++){
				for (int x = 0; x < picw; x++)
				{
					int index = y * picw + x;
					int R = (pix[index] >> 16) & 0xff;     
					int G = (pix[index] >> 8) & 0xff;
					int B = pix[index] & 0xff;

					int av=(R+G+B)/3;
					if(av <=127){R=G=B=0;}
					else{R=G=B=255;}

					pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
				}
			}
			EditText edit=(EditText) findViewById(R.id.editText1);
			String s=" ";
			 */
			/*for(int i=0;i<pix.length;i++){
				s+=String.valueOf(pix[i])+" ";
			}*/
			//edit.setText(String.valueOf(pix.length));
			ImageView image= (ImageView) findViewById(R.id.editedImage);
			image.setImageBitmap(bmp);
			BitmapDrawable abmp = (BitmapDrawable) image.getDrawable();
			bmp=abmp.getBitmap();
			Bitmap temp= Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),bmp.getConfig());

			int srcData[]= new int[bmp.getWidth() * bmp.getHeight()];
			int ptr=0;
			for(int i=0; i<bmp.getWidth(); i++){
				for(int j=0; j<bmp.getHeight(); j++){
					int p = bmp.getPixel(i, j);
					int r = (int) (Color.red(p) * 0.33);
					int g = (int) (Color.green(p) * 0.59);
					int b = (int) (Color.blue(p) * 0.11);

					int av=(r+g+b)/3;
					int n = r+g+b;
					r=g=b=n;

					srcData[ptr]= 0xFF & p;
					ptr++;

					temp.setPixel(i, j, Color.argb(Color.alpha(p) , r, g, b));
					/*if(av<=127){
			        	 temp.setPixel(i, j, Color.argb(Color.alpha(p), 0, 0, 0));
			         }else{
			        	 temp.setPixel(i, j, Color.argb(Color.alpha(p), 255, 255, 255));
			         }*/
				}
			}


			//call OTSU here
			int thresh=otsuThresholding(srcData);
			//Gamma Correction
			double gamma_val=1.25;
			int[] gamma_lut= gamma_LUT(gamma_val);

			Bitmap temp2= Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),bmp.getConfig());
			for(int i=0; i<temp.getWidth(); i++){
				for(int j=0; j<temp.getHeight(); j++){
					int p = temp.getPixel(i, j);
					
					int r = (int) (gamma_lut[Color.red(p)]);
					int g = (int) (gamma_lut[Color.green(p)]);
					int b = (int) (gamma_lut[Color.blue(p)]);
					 
					int n = (int)(r*0.33 + g*0.59 + b*0.11);
					

					if( n < thresh )
						temp2.setPixel(i, j, Color.argb(Color.alpha(p) , 255,255,255));
					else
						temp2.setPixel(i, j, Color.argb(Color.alpha(p) , 0,0,0));
				}
			}

			image.setImageBitmap(temp2);
			inspectBMP(temp);
			Toast.makeText(getApplicationContext(), "Threshlold="+String.valueOf(thresh),Toast.LENGTH_LONG).show();
			
			
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(IOException e){}
			}
		}
	}

	private int otsuThresholding(int[] src){
		int ptr=0;
		int hist[] = new int[256];
		while(ptr < src.length){
			int h = src[ptr];
			hist[h]++;
			ptr++;
		}
		int total = src.length;
		float sum = 0;
		for (int t=0 ; t<256 ; t++) sum += t * hist[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		int threshold = 0;

		for (int t=0 ; t<256 ; t++) {
			wB += hist[t];               // Weight Background
			if (wB == 0) continue;

			wF = total - wB;                 // Weight Foreground
			if (wF == 0) break;

			sumB += (float) (t * hist[t]);

			float mB = sumB / wB;            // Mean Background
			float mF = (sum - sumB) / wF;    // Mean Foreground

			// Calculate Between Class Variance
			float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}

		return threshold;

	}

	private static int[] gamma_LUT(double gamma_new) {
		int[] gamma_LUT = new int[256];

		for(int i=0; i<gamma_LUT.length; i++) {
			gamma_LUT[i] = (int) (255 * (Math.pow((double) i / (double) 255, gamma_new)));
		}

		return gamma_LUT;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch(requestCode){
		case REQUEST_GALLERY:

			if(resultCode==RESULT_OK){

				inspect(data.getData());
			}
			break;
		case REQUEST_CAMERA:
			if(resultCode == RESULT_OK){
				if(imageUri!=null){
					inspect(imageUri);
				}
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}
}
