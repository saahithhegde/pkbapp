package sdi.com.pkb;

import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViolationActivity extends AppCompatActivity {
    private static AsyncHttpClient client = new AsyncHttpClient();
    private TextView regNo,fineAmount,name,noticeNo,noticeDate,offenseDescription,
    location,violationTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation);
        Intent i = getIntent();
        RequestParams rp = new RequestParams();
        rp.add("vehicleNo",i.getStringExtra("reg"));
        regNo = findViewById(R.id.regViolation);
        fineAmount = findViewById(R.id.fineAmount);
        name = findViewById(R.id.person_name);
        noticeDate = findViewById(R.id.notice_date);
        noticeNo = findViewById(R.id.notice_no);
        offenseDescription = findViewById(R.id.offense);
        location = findViewById(R.id.location);
        violationTime = findViewById(R.id.violation_time);
        client.get("http://3.17.68.219/api",rp,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println(response.toString());
                try {
                    JSONArray policeFineList = response.getJSONArray("PoliceFineDetailsList");
                    JSONObject result = policeFineList.getJSONObject(0);
                    regNo.setText("Vehicle Registration Number: "+result.getString("RegistrationNo"));
                    offenseDescription.setText("Offense: "+result.getString("OffenceDescription"));
                    fineAmount.setText("Fine: "+result.getString("FineAmount"));
                    name.setText("Name: "+result.getString("Name"));
                    noticeDate.setText("Notice Date: "+result.getString("NoticeGenerationDate"));
                    noticeNo.setText("Notice number: "+result.getString("NoticeNo"));
                    location.setText("location:"+result.getString("PointName"));
                    } catch (JSONException e1) {
                    e1.printStackTrace();
                    regNo.setText("No violations");
                }
            }
        });
    }
}
