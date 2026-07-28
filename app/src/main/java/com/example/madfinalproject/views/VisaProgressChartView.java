package com.example.madfinalproject.views;
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View;
public class VisaProgressChartView extends View {
 private final Paint p=new Paint(1); private float[] scores={0f};
 public VisaProgressChartView(Context c,AttributeSet a){super(c,a);}
 public void setScores(java.util.List<Integer> values){if(values==null||values.isEmpty())scores=new float[]{0};else{scores=new float[values.size()];for(int i=0;i<values.size();i++)scores[i]=values.get(i);}invalidate();}
 protected void onDraw(Canvas c){super.onDraw(c);float l=35,t=22,r=getWidth()-16,b=getHeight()-30;p.setStrokeWidth(1);p.setColor(0xffe8ecf3);for(int i=0;i<5;i++){float y=t+(b-t)*i/4;c.drawLine(l,y,r,y,p);}Path path=new Path();int d=Math.max(1,scores.length-1);for(int i=0;i<scores.length;i++){float x=l+(r-l)*i/d,y=b-scores[i]/100f*(b-t);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);p.setColor(0xff4158d8);p.setStrokeWidth(3);c.drawCircle(x,y,5,p);p.setTextSize(10);p.setColor(0xff243453);c.drawText(String.valueOf((int)scores[i]),x-8,y-10,p);}p.setStyle(Paint.Style.STROKE);p.setColor(0xff4158d8);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setTextSize(10);p.setColor(Color.DKGRAY);for(int i=0;i<scores.length;i++)c.drawText("M"+(i+1),l+(r-l)*i/d-6,getHeight()-8,p);}
}
