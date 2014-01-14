package info.androidhive.slidingmenu;


import java.lang.reflect.Field;

import com.facebook.Session;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment{
	
	public HomeFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        
        Session session = Session.getActiveSession();
		
        MainActivity.map = ((SupportMapFragment)  getFragmentManager().findFragmentById(R.id.map)).getMap();
        MainActivity.map.setMyLocationEnabled(true);
        
        MainActivity.map.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(MainActivity.currentMarker != null){
					MainActivity.currentMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.marker_red)));
				}
		    	marker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.marker_yellow)));
		    	MainActivity.currentMarker = marker;
				return false;
			}
		});
        
        return rootView;
    }
	
	

}
