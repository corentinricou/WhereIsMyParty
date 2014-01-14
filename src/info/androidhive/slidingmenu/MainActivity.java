package info.androidhive.slidingmenu;

import info.androidhive.slidingmenu.adapter.NavDrawerListAdapter;
import info.androidhive.slidingmenu.model.NavDrawerItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements 	LocationListener,
																OnClickListener{
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	public Fragment mapFrag;
	private Fragment paramFrag;
	public static Fragment logFrag;
	private Fragment currentFrag;
	public Fragment infoEvent;
	
	public Location currentLocation;
	
	public static Marker currentMarker = null;
	
	final Context context = this;
	
	public Intent mServiceIntent;
	
	public LocationManager locationManager;
	public static final long MIN_TIME = 400;
	public static final float MIN_DISTANCE = 0;
	
	public static Double DIST_TRIGGER = 0.100;
	
	public Vibrator vib;
	
	public NotificationManager mNotificationManager ;
	
	public static MainActivity activity = new MainActivity();
	
	public static Map<String, Event> listeEvent = new HashMap<String, Event>();
	
	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	public FragmentManager fragmentManager;
	
	public static GoogleMap map = null;
	
	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fragmentManager = getSupportFragmentManager();
		
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Retour à la carte
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Find People
		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
//		// Log FB
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
	

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			mapFrag = new HomeFragment();
			paramFrag = new FenetreParam();
			logFrag = new  LogFacebook();
			infoEvent = new InfoEvent();
			
			currentFrag = mapFrag;
			fragmentManager.beginTransaction().add(R.id.frame_container, mapFrag,"map").commit();
			fragmentManager.beginTransaction().add(R.id.frame_container, paramFrag,"param").hide(paramFrag).commit();
			fragmentManager.beginTransaction().add(R.id.frame_container, logFrag,"logf").hide(logFrag).commit();
			fragmentManager.beginTransaction().add(R.id.map, infoEvent,"infoEvt").commit();
			
			Log.i("wimp", "MF : "+fragmentManager);
		}
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		vib=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);		
		
//		mServiceIntent = new Intent(this, RSSPullService.class);
//		mServiceIntent.setData(null);

	}
	
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
				if(position == 1){
					Log.i("wimp", "SAVE");
					mDrawerLayout.closeDrawer(mDrawerList);
				}else{
					displayView(position);
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		fragmentManager.beginTransaction().hide(currentFrag).commit();
		switch (position) {
		case 0:
			fragmentManager.beginTransaction().show(mapFrag).commit();
			currentFrag = mapFrag;
			break;
		case 2:
			fragmentManager.beginTransaction().show(paramFrag).commit();
			currentFrag =paramFrag;
			break;
		case 3:
			fragmentManager.beginTransaction().show(logFrag).commit();
			currentFrag = logFrag;
			break;
		default:
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
		setTitle(navMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
		
		
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void sendMessage(View view) {
		locateEventList();
	}
	
	public void fillEventList(){
		final Session session = Session.getActiveSession();

		Log.i("TAG", "Dans fonction FILL"+session);
		
		if(session == null){
			Session.openActiveSession(activity, true, ((LogFacebook)logFrag).getCallBack());
		}
		
		if(session != null && session.isOpened()){
			String fqlQuery = "SELECT name,location,venue,eid from event where eid IN (SELECT eid from event_member where uid = me())";
			  Bundle params = new Bundle();
		        params.putString("q", fqlQuery);		        
		        Request request = new Request(session,
		            "/fql",                         
		            params,                         
		            HttpMethod.GET,          
		            new Request.Callback(){
		                public void onCompleted(Response response) {
		                			 
		                	Log.i("wimp", "RES: "+response);
		                	
		                    try
		                    {
		                        GraphObject go  = response.getGraphObject();
		                        JSONObject  jso = go.getInnerJSONObject();
		                        JSONArray   arr = jso.getJSONArray( "data" );

		                        for ( int i = 0; i < ( arr.length() ); i++ )
		                        {
		                            JSONObject json_obj = arr.getJSONObject( i );
		                            String name = json_obj.getString("name");
		                            Double longitude = null;
		                            Double latitude = null;
		                            String location = json_obj.getString("location");
		                            
		                            try{
		                            	longitude = json_obj.getJSONObject("venue").getDouble("longitude");
		                            	latitude = json_obj.getJSONObject("venue").getDouble("latitude");
		                            }catch(JSONException e){}
		                            
		                            if( (longitude == null) && (latitude == null)){
		                            	if(location != null){
		                            		Log.i("tag", "Nom premier = "+name);
//		                            		Event evt = new Event(name, location, null, null);
		                            		MainActivity.listeEvent.put(name, new Event(name, location, latitude, longitude,json_obj.getInt("eid")));
		                            		//locateEventList(evt);
		                            	}
		                            }else{
		                            	MainActivity.listeEvent.put(name, new Event(name, location, latitude, longitude,json_obj.getInt("eid")));
		                            	map.addMarker(new MarkerOptions()
		                                .title(name)
		                        		.snippet("The most populous city in Australia.")
		                                .position(new LatLng(latitude, longitude)));
		                            }
		                        }
		                    }
		                    catch ( Throwable t ){t.printStackTrace();}
		                } 
		        }); 
		        Request.executeBatchAsync(request);
		        
		}else{
			Log.i("TAG", "SESSION NUL ou PAS COM"+ session);
		}
	}
	
	public void locateEventList(){

		final Session session = Session.getActiveSession();

		Log.i("TAG", "Dans fonction Locate"+session);
		
		if(session == null){
			Session.openActiveSession(activity, true, ((LogFacebook)logFrag).getCallBack());
		}
		
		if(session != null && session.isOpened()){
			for(Entry<String, Event> entry : MainActivity.listeEvent.entrySet()) {
			    String name = entry.getKey();
			    Event evt = entry.getValue();
			    if(evt.getLatitude() == null){
			    	Log.i("tag", "Nom deuxiéme = "+name);
			    	
			    	Geocoder geocoder = new Geocoder(this);
					double Nlatitude;
					double Nlongitude;
					List<Address> addresses = null;
					try {
						addresses = geocoder.getFromLocationName(evt.getLocation(), 3);
						
						if( addresses.size() != 0 ){
							Address addr = addresses.get(0);
							Nlatitude = addr.getLatitude();
							Nlongitude = addr.getLongitude(); //
							
							evt.setLatitude(Nlatitude);
							evt.setLongitude(Nlongitude);
							
							map.addMarker(new MarkerOptions()
			                .title(name)
			        		.snippet("The most populous city in Australia.")
			                .position(new LatLng(Nlatitude, Nlongitude)));
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
			    }
			}
		}
		
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		locationManager.removeUpdates(this);
		vib.cancel();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MainActivity.MIN_TIME, MainActivity.MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MainActivity.MIN_TIME, MainActivity.MIN_DISTANCE, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		
		currentLocation = location;
		
	    MainActivity.map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()) , 16, 45, 0)));

	    for(Entry<String, Event> entry : listeEvent.entrySet()) {
		    Event evt = entry.getValue();
		    if( (evt.getLatitude()) != null && (!evt.IsAlreadyTriggered()) ){
		    	Location nlocation = new Location("event");
		    	nlocation.setLatitude(evt.getLatitude());
		    	
		    	nlocation.setLongitude(evt.getLongitude());
		    	if( (location.distanceTo(nlocation) / 1000f) <= DIST_TRIGGER ){
			    	evt.setIsAlreadyTriggered(true);
			    	Toast.makeText(this, "LOCATION TRIGGERED", Toast.LENGTH_SHORT).show();
			    	
			    	NotificationCompat.Builder mBuilder =
					        new NotificationCompat.Builder(this)
					        .setSmallIcon(R.drawable.ic_launcher)
					        .setContentTitle("Vous n'etes pas loi de : "+evt.getName())
					        .setContentText("Appuyez pour accéder à la carte");
					Intent resultIntent = new Intent(this, MainActivity.class);
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
					stackBuilder.addParentStack(MainActivity.class);
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent =
					        stackBuilder.getPendingIntent(
					            0,
					            PendingIntent.FLAG_UPDATE_CURRENT
					        );
					mBuilder.setContentIntent(resultPendingIntent);
			    	mNotificationManager.notify(1, mBuilder.build());
			    }
		    }
	    }
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onClick(View v) {
		fragmentManager.beginTransaction().hide(infoEvent).commit();
	}

	public void partagerEvent(View view) {
		if(currentMarker != null){
			
		}else{
			Toast.makeText(this, "Pas d'evenement selectionné", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void afficherEvent(View view) {
		if(currentMarker != null){
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				    Uri.parse("https://www.facebook.com/events/"+listeEvent.get(currentMarker.getTitle()).getId()));
				startActivity(intent);
		}else{
			Toast.makeText(this, "Pas d'evenement selectionné", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void chargerItineraire(View view) {
		if(currentMarker != null){
			
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				    Uri.parse("http://maps.google.com/maps?saddr="+currentLocation.getLatitude()+","+currentLocation.getLongitude()+"&daddr="+currentMarker.getPosition().latitude+","+currentMarker.getPosition().longitude));
				startActivity(intent);
		}else{
			Toast.makeText(this, "Pas d'evenement selectionné", Toast.LENGTH_SHORT).show();
		}
	}
}
