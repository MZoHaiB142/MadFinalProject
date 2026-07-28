package com.example.madfinalproject;
import android.os.Bundle; import android.widget.Toast; import androidx.appcompat.app.AppCompatActivity;
public class SatDetailActivity extends AppCompatActivity {
 protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_sat_detail);findViewById(R.id.btnBack).setOnClickListener(v->finish());findViewById(R.id.btnFavorite).setOnClickListener(v->v.setSelected(!v.isSelected()));findViewById(R.id.btnStartSat).setOnClickListener(v->Toast.makeText(this,"SAT preparation started",Toast.LENGTH_SHORT).show());}
}
