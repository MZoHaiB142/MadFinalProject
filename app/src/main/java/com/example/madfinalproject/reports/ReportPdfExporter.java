package com.example.madfinalproject.reports;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.example.madfinalproject.utils.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class ReportPdfExporter {
    private static final String TAG="ReportPdf";
    private ReportPdfExporter(){}
    public static Uri export(Activity activity,InterviewReport report)throws Exception{String name="AbroadIQ-Interview-"+report.interviewId.substring(0,8)+".pdf";Uri uri;OutputStream output;if(Build.VERSION.SDK_INT>=29){ContentValues values=new ContentValues();values.put(MediaStore.MediaColumns.DISPLAY_NAME,name);values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/AbroadIQ");uri=activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IllegalStateException("Unable to create report file");output=activity.getContentResolver().openOutputStream(uri);}else{File file=new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),name);uri=Uri.fromFile(file);output=new FileOutputStream(file);}if(output==null)throw new IllegalStateException("Unable to open report file");PdfDocument pdf=create(report);pdf.writeTo(output);output.close();pdf.close();LogUtils.d(TAG,"PDF exported: "+uri);return uri;}
    public static void share(Activity activity,Uri uri){Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("application/pdf");intent.putExtra(Intent.EXTRA_STREAM,uri);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);activity.startActivity(Intent.createChooser(intent,"Share interview report"));}
    public static void print(Activity activity,InterviewReport report){android.print.PrintManager manager=(android.print.PrintManager)activity.getSystemService(Activity.PRINT_SERVICE);manager.print("AbroadIQ Interview Report",new android.print.PrintDocumentAdapter(){@Override public void onLayout(android.print.PrintAttributes oldA,android.print.PrintAttributes newA,android.os.CancellationSignal signal,LayoutResultCallback callback,android.os.Bundle extras){callback.onLayoutFinished(new android.print.PrintDocumentInfo.Builder("AbroadIQ-Interview-Report.pdf").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1).build(),true);}@Override public void onWrite(android.print.PageRange[] pages,android.os.ParcelFileDescriptor destination,android.os.CancellationSignal signal,WriteResultCallback callback){try{PdfDocument pdf=create(report);pdf.writeTo(new FileOutputStream(destination.getFileDescriptor()));pdf.close();callback.onWriteFinished(new android.print.PageRange[]{android.print.PageRange.ALL_PAGES});}catch(Exception e){callback.onWriteFailed("Could not print report");}}},null);}
    private static PdfDocument create(InterviewReport r){PdfDocument pdf=new PdfDocument();PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,1).create());Canvas c=page.getCanvas();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.rgb(7,26,72));p.setTextSize(22);p.setFakeBoldText(true);c.drawText("AbroadIQ Visa Interview Report",40,55,p);p.setFakeBoldText(false);p.setTextSize(13);int y=90;y=line(c,p,y,"Country: "+r.country+"     Grade: "+r.grade+"     Overall: "+r.overallScore+"/100");y=line(c,p,y,"Duration: "+(r.durationMillis/60000)+" min     Questions: "+r.questionsAnswered);y+=15;y=line(c,p,y,"Content: "+r.contentScore+"%   Grammar: "+r.grammarScore+"%   Fluency: "+r.fluencyScore+"%   Confidence: "+r.confidenceScore+"%");y+=15;y=line(c,p,y,"AI Summary: "+r.aiSummary);y+=15;y=line(c,p,y,"Strengths: "+android.text.TextUtils.join(", ",r.strongAreas));y=line(c,p,y,"Weak Areas: "+android.text.TextUtils.join(", ",r.weakAreas));y+=15;y=line(c,p,y,"Recommended Practice:");for(String value:r.aiSuggestions)y=line(c,p,y,"• "+value);pdf.finishPage(page);return pdf;}
    private static int line(Canvas c,Paint p,int y,String text){int max=80;for(int start=0;start<text.length();start+=max){c.drawText(text.substring(start,Math.min(text.length(),start+max)),40,y,p);y+=22;}return y;}
}
