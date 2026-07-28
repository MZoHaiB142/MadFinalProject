package com.example.madfinalproject;
import android.graphics.Color; import android.view.View; import android.view.ViewGroup; import android.widget.ProgressBar; import android.widget.TextView;
final class PteUiAdapter {
 static final int TEAL=Color.rgb(8,145,156); private PteUiAdapter(){}
 static void apply(View root){if(root instanceof TextView){TextView t=(TextView)root;String s=t.getText()==null?"":t.getText().toString();t.setText(s.replace("IELTS Academic","PTE Academic").replace("IELTS","PTE").replace("Overall Band","Overall Score").replace("Band","Score").replace("7.5","79").replace("7.0","78").replace("6.5","72").replace("6.0","65"));if(t.getCurrentTextColor()==Color.rgb(233,31,50))t.setTextColor(TEAL);}else if(root instanceof ProgressBar)((ProgressBar)root).setProgressTintList(android.content.res.ColorStateList.valueOf(TEAL));if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++)apply(g.getChildAt(i));}}
}
