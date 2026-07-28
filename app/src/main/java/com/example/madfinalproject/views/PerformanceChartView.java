package com.example.madfinalproject.views;
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View;
public class PerformanceChartView extends View {
 private final Paint p=new Paint(1); private final int[] colors={Color.rgb(230,32,49),Color.rgb(244,133,32),Color.rgb(30,166,91),Color.rgb(102,69,220),Color.rgb(52,120,225)};
 private final float[][] data={{6.2f,6.6f,5.9f,6.3f,6.5f,6.2f,6.6f,7.2f},{5.8f,6.1f,5.4f,5.9f,5.7f,5.3f,5.5f,6.2f},{6.0f,6.5f,6.1f,5.8f,6.0f,5.9f,6.2f,6.8f},{6.5f,6.8f,6.1f,6.0f,6.3f,5.9f,6.7f,7.1f},{5.2f,5.2f,4.6f,5.0f,5.0f,4.6f,4.9f,5.6f}};
 public PerformanceChartView(Context c, AttributeSet a){super(c,a);}
 protected void onDraw(Canvas c){super.onDraw(c);float l=38,t=18,r=getWidth()-14,b=getHeight()-28;p.setStrokeWidth(1);p.setColor(0xffe8ecf3);for(int i=0;i<5;i++){float y=t+(b-t)*i/4;c.drawLine(l,y,r,y,p);}for(int s=0;s<data.length;s++){p.setColor(colors[s]);p.setStrokeWidth(3);Path path=new Path();for(int i=0;i<8;i++){float x=l+(r-l)*i/7;float y=b-(data[s][i]-4f)/4f*(b-t);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);c.drawCircle(x,y,4,p);}c.drawPath(path,p);}p.setTextSize(10);p.setColor(0xff52617c);for(int i=0;i<8;i++)c.drawText("T"+(i+1),l+(r-l)*i/7-5,getHeight()-8,p);}
}
