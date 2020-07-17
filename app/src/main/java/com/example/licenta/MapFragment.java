package com.example.licenta;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    Button buttonAccess;
    View mapViewFrag;
    private FirebaseAuth myAuth;

    String currentUserId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mapViewFrag = inflater.inflate(R.layout.fragment_map, container, false);
        myAuth = FirebaseAuth.getInstance();

        currentUserId = myAuth.getCurrentUser().getUid();

        buttonAccess = (Button) mapViewFrag.findViewById(R.id.button_location);

        buttonAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getContext(), MapActivity.class);
                mapIntent.putExtra("visit_user_id", currentUserId);

                startActivity(mapIntent);
            }
        });

        return mapViewFrag;
    }
}
