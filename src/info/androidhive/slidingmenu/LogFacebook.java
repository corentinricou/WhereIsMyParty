package info.androidhive.slidingmenu;

import java.io.InputStream;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

//import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LogFacebook extends Fragment {

public LogFacebook(){}

private static final String TAG = "MainFragment";

private TextView nom;

private TextView prenom;

private ImageView imageV;
	
	private UiLifecycleHelper uiHelper;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	public Session.StatusCallback getCallBack(){
		return callback;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	    ViewGroup container, 
	    Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_photos, container, false);
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setReadPermissions(Arrays.asList("user_likes", "user_status","user_events"));
	    authButton.setFragment(this);
	    
	    nom = (TextView)view.findViewById(R.id.textView1);
	    prenom = (TextView)view.findViewById(R.id.textView2);
	    imageV = (ImageView)view.findViewById(R.id.imageView1);
	    
	    return view;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	    	MainActivity.activity.fillEventList();
	    	
	    	setSessionInfo(session);
	    	
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	    	nom.setText("Nom :");
	    	prenom.setText("Prenom :");
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	private void setSessionInfo(Session session){
		Log.i("tag", "In setSessionsINFO");
		
		String fqlQuery = "SELECT name, pic_square FROM user WHERE uid = me()";
		  Bundle params = new Bundle();
	        params.putString("q", fqlQuery);		        
	        Request request = new Request(session,
	            "/fql",                         
	            params,                         
	            HttpMethod.GET,          
	            new Request.Callback(){
	                public void onCompleted(Response response) {
	                    try
	                    {
	                        GraphObject go  = response.getGraphObject();
	                        JSONObject  jso = go.getInnerJSONObject();
	                        JSONArray   arr = jso.getJSONArray( "data" );

	                        for ( int i = 0; i < ( arr.length() ); i++ )
	                        {
	                            JSONObject json_obj = arr.getJSONObject( i );
	                            String name = json_obj.getString("name");
	                            String image = json_obj.getString("pic_square");
	                            String[] bou = name.split(" ");
	                            nom.setText("Nom: "+bou[0]);
	                            prenom.setText("Prenom:  "+bou[1]);
	                            new DownloadImageTask(imageV)
	                            .execute(image);
	                        }
	                    }
	                    catch ( Throwable t ){t.printStackTrace();}
	                } 
	        }); 
	        Request.executeBatchAsync(request);
	}
	
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}
}
