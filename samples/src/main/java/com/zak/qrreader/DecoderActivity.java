package com.zak.qrreader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DecoderActivity extends AppCompatActivity
    implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {

  private static final int MY_PERMISSION_REQUEST_CAMERA = 0;

  private ViewGroup mainLayout;

  private TextView resultTextView;
  private QRCodeReaderView qrCodeReaderView;
  private CheckBox flashlightCheckBox;
  private CheckBox enableDecodingCheckBox;
  private PointsOverlayView pointsOverlayView;
private  int checkLoginLogout=0;
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_decoder);
    checkLoginLogout=getIntent().getIntExtra(MainActivity.SCAN,0);

    mainLayout = (ViewGroup) findViewById(R.id.main_layout);

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      initQRCodeReaderView();
    } else {
      requestCameraPermission();
    }
    //String json = "{\"mobile\": \"9873799571\",\"id\": \"2\"}";
    //sendData(json);
  }

  @Override protected void onResume() {
    super.onResume();

    if (qrCodeReaderView != null) {
      qrCodeReaderView.startCamera();
    }
  }

  @Override protected void onPause() {
    super.onPause();

    if (qrCodeReaderView != null) {
      qrCodeReaderView.stopCamera();
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
      return;
    }

    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
      initQRCodeReaderView();
    } else {
      Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
          .show();
    }
  }

  // Called when a QR is decoded
  // "text" : the text encoded in QR
  // "points" : points where QR control points are placed
  @Override public void onQRCodeRead(String text, PointF[] points) {
    resultTextView.setText(text);
    sendData(text);
    pointsOverlayView.setPoints(points);
  }
private void sendData(String jsonString) {
  JSONObject mainObject = null;
  JSONObject j = new JSONObject();
  final ProgressDialog pDialog = new ProgressDialog(this);
  pDialog.setMessage("Loading...");
  pDialog.show();

  AsyncHttpClient client = new AsyncHttpClient();
  StringEntity entity = null;
  try {
    mainObject = new JSONObject(jsonString);
    j.put("patientId", mainObject.getString("id"));
    j.put(getUrl(checkLoginLogout).get(0), mainObject.getString("mobile"));
    entity = new StringEntity(j.toString());
  } catch (JSONException e) {
    e.printStackTrace();
  } catch (UnsupportedEncodingException e) {
    e.printStackTrace();
  }
  client.setTimeout(3000);
  client.post(getApplicationContext(), getUrl(checkLoginLogout).get(1), entity, "application/json",
          new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              pDialog.hide();
              try {
                JSONObject responseJson=new JSONObject(new String(responseBody));
                String message = responseJson.getString("message");

                resultTextView.setText(message);
                resultTextView.setTextSize(28);


                if(responseJson.getString("status").equalsIgnoreCase("failure")){
                  resultTextView.setTextColor(Color.RED);
                  showAlert(message,false);
                }else {
                  resultTextView.setTextColor(Color.GREEN);
                  showAlert(message,true);
                }

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              pDialog.hide();
              resultTextView.setText("Please try again");
              resultTextView.setTextSize(28);
              resultTextView.setTextColor(Color.RED);
              showAlert("Please try again",false);
            }
          });

}

private void showAlert(String message, boolean valid ){
  AlertDialog.Builder builder;
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    if (valid) {
      builder = new AlertDialog.Builder(this, R.style.AlertDialogCustomValid);
    }else{
      builder = new AlertDialog.Builder(this, R.style.AlertDialogCustomInvalid);
    }
  } else {
    builder = new AlertDialog.Builder(this);
  }
  builder.setTitle("Scan Status")
          .setMessage(message)
          .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              // continue with delete
            }
          })
          .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              // do nothing
            }
          })
          .setIcon(android.R.drawable.ic_dialog_alert)
          .show();
}


  private void requestCameraPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
      Snackbar.make(mainLayout, "Camera access is required to display the camera preview.",
          Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
        @Override public void onClick(View view) {
          ActivityCompat.requestPermissions(DecoderActivity.this, new String[] {
              Manifest.permission.CAMERA
          }, MY_PERMISSION_REQUEST_CAMERA);
        }
      }).show();
    } else {
      Snackbar.make(mainLayout, "Permission is not available. Requesting camera permission.",
          Snackbar.LENGTH_SHORT).show();
      ActivityCompat.requestPermissions(this, new String[] {
          Manifest.permission.CAMERA
      }, MY_PERMISSION_REQUEST_CAMERA);
    }
  }

  private void initQRCodeReaderView() {
    View content = getLayoutInflater().inflate(R.layout.content_decoder, mainLayout, true);

    qrCodeReaderView = (QRCodeReaderView) content.findViewById(R.id.qrdecoderview);
    resultTextView = (TextView) content.findViewById(R.id.result_text_view);
    flashlightCheckBox = (CheckBox) content.findViewById(R.id.flashlight_checkbox);
    enableDecodingCheckBox = (CheckBox) content.findViewById(R.id.enable_decoding_checkbox);
    pointsOverlayView = (PointsOverlayView) content.findViewById(R.id.points_overlay_view);

    qrCodeReaderView.setAutofocusInterval(2000L);
    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setBackCamera();
    flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        qrCodeReaderView.setTorchEnabled(isChecked);
      }
    });
    enableDecodingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        qrCodeReaderView.setQRDecodingEnabled(isChecked);
      }
    });
    qrCodeReaderView.startCamera();
  }

  private List<String> getUrl(int value) {
    List<String> list = new ArrayList<>();
    switch (value) {
      case 1:
        list.add("attendentContactNo");
        list.add("http://demo-ramnath.rhcloud.com/loginAttendent.do");
        break;
      case 2:
        list.add("attendentContactNo");
        list.add("http://demo-ramnath.rhcloud.com/logoutAttendent.do");
        break;
      case 3:
        list.add("contactNumber");
        list.add("http://demo-ramnath.rhcloud.com/loginVisitors.do");
        break;
      case 4:
        list.add("contactNumber");
        list.add("http://demo-ramnath.rhcloud.com/logoutVisitors.do");
        break;

    }
    return list;
  }}