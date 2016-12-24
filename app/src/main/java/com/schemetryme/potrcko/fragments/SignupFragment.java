package com.schemetryme.potrcko.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.R;
import com.schemetryme.potrcko.activities.LauncherActivity;
import com.schemetryme.potrcko.activities.MainActivity;

import java.io.Serializable;

public class SignupFragment extends Fragment {

    public SignupFragment() {
        // Required empty public constructor
    }

    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        ImageButton imageButton_register = (ImageButton) view.findViewById(R.id.imageButton_signupRegister);
        imageButton_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                LauncherActivity launcherActivity = (LauncherActivity) getActivity();
                intent.putExtra(LauncherActivity.KEY_LOCATION, launcherActivity.getmLocation());
                intent.putExtra(LauncherActivity.CURRENT_USER, (Serializable) new User() );
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
