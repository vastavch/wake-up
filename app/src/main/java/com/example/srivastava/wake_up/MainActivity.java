package com.example.srivastava.wake_up;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Provider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GeoTask.Geo {
    EditText edttxt_from, edttxt_to, edttxt_name, edttxt_phone, edttxt_hours, edttxt_minutes;
    TextView txtVw_result;
    Button get;
    String from, to, name, phone, to_modify, from_modify;
    int hrs, mins;
//    Double duration;
    LocationManager locationManager;
    LocationListener locationListener;
    StringBuilder sb;
    Boolean alreadyRequested = false, isVibrating = false;
    Vibrator vibrator;
    CountDownTimer cdt;
    Double minLeft;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIntialLaunch();
        setContentView(R.layout.activity_main);
        initialize();
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getData();
                if (validateInput()) {
                    alreadyRequested = false;
                    to_modify = modifyString(to);
                    from_modify = modifyString(from);
                    String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + from_modify + "&destinations=" + to_modify + "&mode=driving&language=fr-FR&key=AIzaSyBxMn6YvAun6ZzEgMY2bWXtfhHcpceSWdQ";
                    new GeoTask(MainActivity.this).execute(url);


                }
            }

        });


    }
//This function checks whether the app is launched for first time.If yes,it launches the InformationActivity else,launches the MainActivity
     void checkIntialLaunch() {
         sharedPreferences=getApplicationContext().getSharedPreferences("wake_up",0);
         SharedPreferences.Editor  editor=sharedPreferences.edit();
         if(!(sharedPreferences.getBoolean("isalreadyLaunched",false)))
         {
             editor.putBoolean("isalreadyLaunched",true);
             editor.commit();
             Intent intent=new Intent(MainActivity.this,InformationActvity.class);
             startActivity(intent);
             finish();
         }

    }
/*This function checks whether the user has enabled the GPS provider.If not enabled,asks user to whether enable GPS Provider,if user clicks yes button,directs user to settings
page.If user clicks  "No" MainActivity will finish and application closes.*/
    @Override
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("GPS Provider not enabled");
            builder.setMessage("Want to enable GPS Provider?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (alreadyRequested) {
                        //to_modify = modifyString(to);
                        from_modify = location.getLatitude() + "," + location.getLongitude();
                        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + from_modify + "&destinations=" + to_modify + "&mode=driving&language=fr-FR&key=AIzaSyBxMn6YvAun6ZzEgMY2bWXtfhHcpceSWdQ";
                        new GeoTask(MainActivity.this).execute(url);

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
        }


    }
//This function takes the string and split it on "," and concat them with "+" to make it suitable for the API call and returns the modified string
    public String modifyString(String in) {
        String[] array = in.split(",");

        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length) {
                sb.append("+");
            }
        }
        return sb.toString();
    }
//This function validates the input entered by user and prompts the user to correct the input issues,if there are any.
    public boolean validateInput() {
        getData();
        if (isValid(name, "str")) {
            if (isValid(phone, "number")) {
                if (hrs >= 0 && hrs <= 23) {
                    if (mins >= 0 && mins < 60) {
                        if (!(to.equals(null) || to.equals("")))

                        {

                            if (!(from.equals(null) || from.equals("")))

                            {
                                return true;
                            } else {
                                edttxt_from.setError("Enter valid starting point details");
                                return false;
                            }
                        } else {
                            edttxt_to.setError("Enter valid starting point details");
                            return false;
                        }

                    } else {
                        edttxt_minutes.setError("Enter valid minutes");
                        return false;
                    }
                } else {
                    edttxt_hours.setError("Enter valid hours");
                    return false;
                }
            } else {
                edttxt_phone.setError("Enter valid phone number");
                return false;
            }

        } else {
            edttxt_name.setError("Name must contain only characters");
            return false;
        }

    }
/*This function takes CharSequence "cs" and String "s" as input.
 Depending on the value of "s",if it is "str" then,
                                        checks whether received "cs" contains only alphabets and ",".if yes, returns true
                                                                                                     else,returns false.
                               else,checks whether received "cs" is of length "10" and contains only numbers.If yes,returns true
                                                                                                                   else,returns false.*/

    public boolean isValid(CharSequence cs, String s) {
        String expression = null;
        if (s.equals("str")) {
            expression = "^[a-zA-Z,]+$";

        } else {
            expression = "^[0-9]+$";
            if (cs.length() != 10)
                return false;
        }

        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(cs);
        if (matcher.matches()) {
            return true;
        } else
            return false;
    }
//This function is called when the user clicks "Get Duration" and assigns the entered input to appropiate variables.
    public void getData() {
        name = edttxt_name.getText().toString();
        from = edttxt_from.getText().toString();
        to = edttxt_to.getText().toString();
        phone = edttxt_phone.getText().toString();
        hrs = Integer.parseInt(edttxt_hours.getText().toString());
        mins = Integer.parseInt(edttxt_minutes.getText().toString());
    }
//This function is called to assign variables to views in the screen.
    public void initialize() {

        edttxt_from = (EditText) findViewById(R.id.editText_from);
        edttxt_to = (EditText) findViewById(R.id.editText_to);
        edttxt_name = (EditText) findViewById(R.id.editText_name);
        edttxt_phone = (EditText) findViewById(R.id.editText_phone);
        edttxt_hours = (EditText) findViewById(R.id.editText_hours);
        edttxt_minutes = (EditText) findViewById(R.id.editText_minutes);
        txtVw_result = (TextView) findViewById(R.id.textView_result);
        get = (Button) findViewById(R.id.button_get);
        sb = new StringBuilder();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        cdt = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                makeACall();
            }
        };

    }
/*This function is called from "GeoTask.class", to display the time to reach the destination(string in  "to" field).
    checks whether the time is less than or equal to the user entered alert if yes,
                                                                vibrates the phone and starts countdown timer for "1min" and displays the alert dialog.
            if user clicks positive button,dismisses alert dialog,stops vibration of phone and calls "sendMessage()".
                     else,dismisses the alert dialog and calls "makeACall()"*/
    @Override
    public void setDouble(Double min) {
        Log.d("minutes",min+"");
        txtVw_result.setText("Destination is " + (int) (min / 60) + " hr " + (int) (min % 60) + " min away!");
        alreadyRequested = true;
        if (((hrs * 60) + mins) >= min) {
            //Toast.makeText(MainActivity.this,"given"+(hrs*60)+mins +" derived "+min, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(60000);
            cdt.start();
            isVibrating = true;
            minLeft = min;
            AlertDialog.Builder builder_result = new AlertDialog.Builder(MainActivity.this);
            builder_result.setTitle("Destination is arriving!!")
                    .setMessage("Have You Noticed It?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("debug", "yes button pressed");
                            if (isVibrating) {
                                vibrator.cancel();
                                isVibrating = false;
                                cdt.cancel();
                            }
                            sendMessage();
                        }
                    });
            AlertDialog dialog_result=builder_result.create();
            dialog_result.show();
            locationManager.removeUpdates(locationListener);
        }
    }
//This function sends a message to the phone number that user entered at the time of clicking the "Get Duration" button
    public void sendMessage() {
        Toast.makeText(MainActivity.this, "sending message", Toast.LENGTH_SHORT).show();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, name + "will be reaching in" + (int) (minLeft / 60) + " hrs " + (int) (minLeft % 60) + " mins ", null, null);

    }
////This function makes a call to the phone number that user entered at the time of clicking the "Get Duration" button
    public void makeACall() {
        Toast.makeText(MainActivity.this, "Making a Call", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phone));
        startActivity(intent);

    }
}
