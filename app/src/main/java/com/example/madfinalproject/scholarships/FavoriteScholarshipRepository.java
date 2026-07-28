package com.example.madfinalproject.scholarships;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.madfinalproject.models.ScholarshipModel;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.utils.LogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class FavoriteScholarshipRepository {
    private static final String TAG="ScholarshipFavorites",COLLECTION="userFavoriteScholarships";
    private static final String[] FORMATS={"yyyy-MM-dd","dd-MM-yyyy","dd/MM/yyyy","MMM d, yyyy","MMMM d, yyyy","MMM d yyyy","MMMM d yyyy","MMM yyyy","MMMM yyyy"};
    public interface ToggleCallback { void complete(boolean favourite,String message); }
    public interface FavoritesCallback { void loaded(Set<String> ids); }
    public void load(FavoritesCallback callback){String uid=uid();FirebaseFirestore.getInstance().collection(COLLECTION).whereEqualTo("userId",uid).get().addOnSuccessListener(snapshot->{Set<String>ids=new HashSet<>();snapshot.getDocuments().forEach(d->ids.add(d.getString("scholarshipId")));callback.loaded(ids);}).addOnFailureListener(e->callback.loaded(new HashSet<>()));}
    public void toggle(Context context,ScholarshipModel item,boolean currentlyFavourite,ToggleCallback callback){toggle(context,item.getId(),item.getTitle(),item.getUniversity(),item.getStart_date(),item.getDeadline(),item.getLink(),currentlyFavourite,callback);}
    public void toggle(Context context,String universityId,University.Scholarship item,boolean currentlyFavourite,ToggleCallback callback){String id=universityId+"_"+String.valueOf((item.getTitle()+item.getLink()).hashCode());toggle(context,id,item.getTitle(),"",null,item.getDeadline(),item.getLink(),currentlyFavourite,callback);}
    private void toggle(Context context,String id,String title,String university,String start,String deadline,String link,boolean current,ToggleCallback callback){String docId=uid()+"_"+id;if(current){FirebaseFirestore.getInstance().collection(COLLECTION).document(docId).delete().addOnSuccessListener(v->{cancel(context,id);callback.complete(false,"Removed from favourites");}).addOnFailureListener(e->callback.complete(true,"Could not remove favourite"));return;}Map<String,Object>d=new HashMap<>();d.put("userId",uid());d.put("scholarshipId",id);d.put("title",title);d.put("university",university);d.put("startDate",start);d.put("deadline",deadline);d.put("link",link);d.put("createdAt",FieldValue.serverTimestamp());FirebaseFirestore.getInstance().collection(COLLECTION).document(docId).set(d).addOnSuccessListener(v->{String reminder=schedule(context,id,title,university,start,deadline,link);LogUtils.d(TAG,"Favourite saved: "+id+"; "+reminder);callback.complete(true,reminder);}).addOnFailureListener(e->{LogUtils.e(TAG,"Favourite save failed",e);callback.complete(false,"Favourite could not be saved");});}
    private String schedule(Context context,String id,String title,String university,String start,String deadline,String link){Date now=new Date(),when=parse(start);String kind="open";boolean annualDateAdjusted=false;if(when!=null&&!when.after(now)){when=rollForwardAnnually(when,now);annualDateAdjusted=true;}if(when==null){Date due=parse(deadline);if(due==null||!due.after(now))return "Added to favourites. Reminder date is not available.";Calendar c=Calendar.getInstance();c.setTime(due);c.add(Calendar.DAY_OF_MONTH,-7);when=c.getTime().after(now)?c.getTime():due;kind="deadline";}Calendar at=Calendar.getInstance();at.setTime(when);at.set(Calendar.HOUR_OF_DAY,9);at.set(Calendar.MINUTE,0);at.set(Calendar.SECOND,0);int code=id.hashCode()&0x7fffffff;Intent intent=new Intent(context,ScholarshipReminderReceiver.class).putExtra("title",title).putExtra("university",university).putExtra("link",link).putExtra("kind",kind).putExtra("requestCode",code);PendingIntent pending=PendingIntent.getBroadcast(context,code,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pending);if(annualDateAdjusted)return "Favourite saved. Past opening date was moved to the same date next year.";return "open".equals(kind)?"Favourite saved. Opening reminder scheduled.":"Favourite saved. Deadline reminder scheduled.";}
    private Date rollForwardAnnually(Date original,Date now){Calendar next=Calendar.getInstance();next.setTime(original);Calendar current=Calendar.getInstance();current.setTime(now);while(!next.after(current))next.add(Calendar.YEAR,1);return next.getTime();}
    private void cancel(Context context,String id){int code=id.hashCode()&0x7fffffff;PendingIntent p=PendingIntent.getBroadcast(context,code,new Intent(context,ScholarshipReminderReceiver.class),PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(p!=null){((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(p);p.cancel();}}
    private Date parse(String value){if(value==null||value.trim().isEmpty())return null;for(String format:FORMATS)try{SimpleDateFormat parser=new SimpleDateFormat(format,Locale.US);parser.setLenient(false);return parser.parse(value.trim());}catch(ParseException ignored){}return null;}
    private String uid(){return FirebaseAuth.getInstance().getUid()==null?"guest":FirebaseAuth.getInstance().getUid();}
}
