package com.schemetryme.potrcko.fragments;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.R;
import com.schemetryme.potrcko.activities.LauncherActivity;
import com.schemetryme.potrcko.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class LoginFragment extends Fragment {

    private EditText mEditTextEmail;
    private EditText mEditTextPassword;

    CallbackManager mCallbackManager;

    private ProgressDialog mProgressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize facebook sdk
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                logInWhitFacebook(loginResult);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mEditTextEmail = (EditText)getActivity().findViewById(R.id.editText_loginMail);
        mEditTextPassword = (EditText)getActivity().findViewById(R.id.editText_loginPassword);

        LoginButton imageButton_face = (LoginButton) view.findViewById(R.id.imageButton_facebookLogin);
        imageButton_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<String> permissions = Arrays.asList("email", "public_profile");
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), permissions);
            }
        });

        ImageButton imageButton_login = (ImageButton) view.findViewById(R.id.imageButton_loginLogin);
        imageButton_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        ImageButton imageButton_register = (ImageButton) view.findViewById(R.id.imageButton_loginRegister);
        imageButton_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupFragment fragment = new SignupFragment();
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_login,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private void login() {
        String email = mEditTextEmail.getText().toString();
        String password = mEditTextPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(),R.string.email_password_empty,Toast.LENGTH_SHORT);
            return;
        } else {
            //check for user
        }
        //take user from server
        startMainActiviry(new User());
    }
    public void startMainActiviry(User aUser) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        LauncherActivity launcherActivity = (LauncherActivity) getActivity();
        intent.putExtra(LauncherActivity.KEY_LOCATION, launcherActivity.getmLocation());
        intent.putExtra(LauncherActivity.CURRENT_USER, (Serializable) aUser );
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void logInWhitFacebook(LoginResult loginResult){
        showProgressDialog(getString(R.string.proggres_login));
        //final AccessToken _token=token;
        //final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        //take data from facebook and send on server.
        GraphRequest.newMeRequest(
                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (response.getError() != null) {
                            // handle error
                        } else {
                            try {
                                String email = response.getJSONObject().get("email").toString();
                                Log.e("Result", email);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String id = object.optString("id");
                            // send email and id to your web server
                            Log.e("Result1", response.getRawResponse());
                            Log.e("Result", object.toString());
                        }
                    }
                }).executeAsync();
    }

    // create user on server and set user in shared preferences
    private void createUserInServer(String email,String authMethod)
    {

        //User user=new User(getUid(),email,authMethod);

        Toast.makeText(getActivity().getApplicationContext(), "Authentication successful..",
                Toast.LENGTH_SHORT).show();
    }
    //region Progress dialog
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    //endregion
}
