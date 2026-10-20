package in.xagi.tvstream;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout page, list; TextView status, time; FrameLayout root;
    MediaPlayer player; boolean prepared, inPlayer; int generation;
    String currentUrl="", currentTitle=""; SurfaceView surface;
    final Handler handler=new Handler();
    ImageView illustration;
    TextView caption;
    JSONArray storyBeats, storyImages;
    double[] beatStarts;
    String artBase="", wantedArt="";
    int artGeneration;
    boolean artBusy;
    final Set<String> failedArt=new HashSet<>();
    final ExecutorService artWorker=Executors.newSingleThreadExecutor();
    final LruCache<String,Bitmap> artwork=new LruCache<String,Bitmap>(4*1024*1024){
        @Override protected int sizeOf(String key,Bitmap value){return value.getByteCount();}
    };
    boolean ramen(){return getPackageName().contains("ramen");}
    String base(){return ramen()?"https://ramen-api.sauravalgs.workers.dev":"https://bookreels-api.sauravalgs.workers.dev";}
    interface Done { void accept(Object value) throws Exception; }
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); home();}
    TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(12,8,12,8);return t;}
    Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextSize(19);b.setAllCaps(false);b.setPadding(20,10,20,10);b.setOnClickListener(v->action.run()); return b;}
    void layout(String title){root=new FrameLayout(this);root.setBackgroundColor(0xff101724);page=new LinearLayout(this);page.setPadding(42,24,42,24);page.setOrientation(1);root.addView(page,new FrameLayout.LayoutParams(-1,-1));setContentView(root);page.addView(text(title,30));status=text("",16);page.addView(status);}
    void rows(){ScrollView sc=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(1);sc.addView(list);page.addView(sc,new LinearLayout.LayoutParams(-1,0,1));}
    void row(String title,Runnable action){Button b=button(title,action);list.addView(b,new LinearLayout.LayoutParams(-1,-2));if(list.getChildCount()==1)b.requestFocus();}
    Object json(String path)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(base()+path).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("User-Agent","XagiTV/1.0");c.setUseCaches(false);
        try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>4*1024*1024)throw new IOException("Catalogue too large");out.write(buf,0,n);}return new JSONTokener(out.toString("UTF-8")).nextValue();}finally{c.disconnect();}
    }
    void fetch(String path,Done done){int g=++generation;status.setText("Loading…");new Thread(()->{try{Object value=json(path);runOnUiThread(()->{if(g!=generation||isDestroyed())return;try{status.setText("D-pad: browse · OK: select · Back: return");done.accept(value);}catch(Exception e){error();}});}catch(Exception e){runOnUiThread(()->{if(g==generation)error();});}}).start();}
    void error(){status.setText("Unable to load. Check the connection and try again."); if(list!=null)row("Retry",()->home());}
    void home(){release();inPlayer=false;layout(ramen()?"Ramen":"BookReels");rows();
        if(ramen())fetch("/api/series",value->{JSONArray a=(JSONArray)value;for(int i=0;i<a.length();i++){JSONObject s=a.getJSONObject(i);row(s.optString("title"),()->episodes(s));}});
        else {row("Illustrated stories · English / हिन्दी",()->stories());row("Audiobooks · LibriVox",()->books());status.setText("Streaming only · no downloads · D-pad and OK to browse");}
    }
    void episodes(JSONObject series){layout(series.optString("title"));rows();JSONArray a=series.optJSONArray("episodes");if(a==null||a.length()==0){status.setText("No published episodes yet.");return;}for(int i=0;i<a.length();i++){JSONObject e=a.optJSONObject(i);if(e==null||e.optString("videoUrl").isEmpty())continue;row(e.optString("title"),()->play(e.optString("videoUrl"),e.optString("title"),true));}}
    void books(){layout("BookReels · Audiobooks");rows();fetch("/v1/books?limit=50",value->{JSONArray a=((JSONObject)value).getJSONArray("items");for(int i=0;i<a.length();i++){JSONObject b=a.getJSONObject(i);row(b.optString("title"),()->chapters(b));}});}
    void chapters(JSONObject book){layout(book.optString("title"));rows();JSONArray a=book.optJSONArray("chapters");if(a==null)return;for(int i=0;i<a.length();i++){JSONObject c=a.optJSONObject(i);if(c!=null)row(c.optString("title"),()->play(c.optString("mp3Url"),c.optString("title"),false));}}
    void stories(){layout("BookReels · Stories");rows();fetch("/v1/stories",value->{JSONArray a=((JSONObject)value).getJSONArray("items");for(int i=0;i<a.length();i++){JSONObject s=a.getJSONObject(i);row(s.optString("title"),()->story(s.optString("id"),s.optString("title")));}});}
    void story(String id,String title){layout(title);rows();fetch("/v1/stories/"+id,value->{JSONObject m=(JSONObject)value;JSONObject langs=m.getJSONObject("languages");Iterator<String> keys=langs.keys();while(keys.hasNext()){String key=keys.next();JSONObject l=langs.getJSONObject(key);String url=new URL(new URL(m.getString("assetBaseUrl")),l.getString("audio")).toString();row(key.equals("hi")?"हिन्दी में सुनें":"Listen in "+key,()->playStory(url,l.optString("title",title),m,l));}});}
    void playStory(String url,String title,JSONObject manifest,JSONObject language) {
        play(url,title,false);
        storyBeats=language.optJSONArray("beats");
        storyImages=manifest.optJSONArray("images");
        artBase=manifest.optString("assetBaseUrl");
        if(storyBeats==null||storyImages==null){caption.setText("Illustrations unavailable for this story");return;}
        beatStarts=new double[storyBeats.length()];
        for(int i=0;i<beatStarts.length;i++)beatStarts[i]=storyBeats.optJSONObject(i).optDouble("start",0);
        syncIllustration(0);
    }
    String imageForBeat(int beat,double seconds) {
        if(beat<0||storyBeats==null||storyImages==null)return "";
        JSONObject b=storyBeats.optJSONObject(beat);
        if(b==null)return "";
        JSONArray indexes=b.optJSONArray("images");
        if(indexes==null)return "";
        int[] images=new int[indexes.length()];
        for(int i=0;i<images.length;i++)images[i]=indexes.optInt(i,-1);
        int selected=StoryTimeline.imageAt(images,b.optDouble("start"),b.optDouble("duration"),seconds);
        JSONObject image=storyImages.optJSONObject(selected);
        if(image==null)return "";
        try{return new URL(new URL(artBase),image.getString("file")).toString();}catch(Exception e){return "";}
    }
    void syncIllustration(int position) {
        if(beatStarts==null||illustration==null)return;
        double seconds=position/1000.0;
        int beat=StoryTimeline.beatAt(beatStarts,seconds);
        if(beat<0)return;
        JSONObject b=storyBeats.optJSONObject(beat);
        String url=imageForBeat(beat,seconds);
        if(!url.equals(wantedArt)){
            wantedArt=url;
            illustration.setImageBitmap(artwork.get(url));
        }
        Bitmap image=artwork.get(url);
        if(image!=null){illustration.setImageBitmap(image);caption.setText(b.optString("caption"));}
        else caption.setText(failedArt.contains(url)?"Illustration unavailable · "+b.optString("caption"):"Loading illustration… · "+b.optString("caption"));
        if(image==null)requestArt(url);
        else {
            String next=imageForBeat(beat,seconds+1);
            if(next.equals(url)&&beat+1<beatStarts.length)next=imageForBeat(beat+1,beatStarts[beat+1]);
            requestArt(next);
        }
    }
    void requestArt(String url) {
        if(url.isEmpty()||artBusy||artwork.get(url)!=null||failedArt.contains(url)||artWorker.isShutdown())return;
        artBusy=true;
        final int request=artGeneration;
        artWorker.execute(()->{
            Bitmap bitmap=null;
            HttpURLConnection connection=null;
            try {
                URL target=new URL(url);
                if(!target.getProtocol().equals("https"))throw new IOException("HTTPS required");
                connection=(HttpURLConnection)target.openConnection();
                connection.setUseCaches(false);connection.setConnectTimeout(8000);connection.setReadTimeout(8000);
                byte[] bytes;
                try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] buffer=new byte[8192];int count;
                    while((count=in.read(buffer))!=-1){if(out.size()+count>3*1024*1024)throw new IOException("Image too large");out.write(buffer,0,count);}
                    bytes=out.toByteArray();
                }
                BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                options.inSampleSize=1;
                while(options.outWidth/options.inSampleSize>1280||options.outHeight/options.inSampleSize>720)options.inSampleSize*=2;
                options.inJustDecodeBounds=false;options.inPreferredConfig=Bitmap.Config.RGB_565;
                bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            }catch(Exception|OutOfMemoryError ignored){}finally{if(connection!=null)connection.disconnect();}
            final Bitmap result=bitmap;
            runOnUiThread(()->{
                artBusy=false;
                if(request!=artGeneration||isDestroyed()){if(result!=null)result.recycle();return;}
                if(result!=null)artwork.put(url,result);else failedArt.add(url);
                if(player!=null&&prepared)syncIllustration(player.getCurrentPosition());
            });
        });
    }
    void play(String url,String title,boolean video){release();++generation;inPlayer=true;currentUrl=url;currentTitle=title;layout(title);status.setText("Connecting…");
        if(video){surface=new SurfaceView(this);page.addView(surface,new LinearLayout.LayoutParams(-1,0,1));surface.getHolder().addCallback(new SurfaceHolder.Callback(){public void surfaceCreated(SurfaceHolder h){start(url,h);}public void surfaceChanged(SurfaceHolder h,int f,int w,int he){}public void surfaceDestroyed(SurfaceHolder h){if(player!=null)player.setDisplay(null);}});}
        else {
            illustration=new ImageView(this);
            illustration.setScaleType(ImageView.ScaleType.FIT_CENTER);
            illustration.setContentDescription("Story illustration");
            page.addView(illustration,new LinearLayout.LayoutParams(-1,0,1));
            caption=text("♫ Streaming audio",18);
            caption.setSingleLine(true);
            caption.setGravity(Gravity.CENTER);
            page.addView(caption);
        }
        time=text("",16);page.addView(time);LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);page.addView(controls);Button pause=button("Play / Pause",()->toggle());controls.addView(button("−15 sec",()->seek(-15000)));controls.addView(pause);controls.addView(button("+15 sec",()->seek(15000)));controls.addView(button("Back to library",()->home()));pause.requestFocus();if(!video)start(url,null);
    }
    void start(String url,SurfaceHolder holder){if(player!=null){if(holder!=null)player.setDisplay(holder);return;}try{MediaPlayer p=new MediaPlayer();player=p;p.setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(1).setContentType(2).build());if(holder!=null)p.setDisplay(holder);p.setDataSource(this,Uri.parse(url));p.setOnPreparedListener(mp->{if(player!=mp)return;prepared=true;int pos=getPreferences(0).getInt("pos-"+url.hashCode(),0);if(pos>0&&pos<mp.getDuration()-3000)mp.seekTo(pos);mp.start();status.setText("Streaming · Left/Right: seek · OK: play/pause · Back: library");tick();});p.setOnVideoSizeChangedListener((mp,w,h)->{if(surface!=null&&w>0&&h>0){int available=getResources().getDisplayMetrics().heightPixels-340;int width=Math.min(getResources().getDisplayMetrics().widthPixels-84,available*w/h);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(width,Math.min(available,width*h/w));lp.gravity=Gravity.CENTER;surface.setLayoutParams(lp);}});p.setOnErrorListener((mp,a,b)->{prepared=false;status.setText("Stream unavailable ("+a+", "+b+"). Press Back and try again.");return true;});p.setOnCompletionListener(mp->{getPreferences(0).edit().remove("pos-"+url.hashCode()).apply();status.setText("Finished · OK: replay · Back: library");});p.prepareAsync();}catch(Exception e){status.setText("Could not open stream. Press Back to retry.");}}
    void tick(){handler.removeCallbacks(update);handler.post(update);}
    final Runnable update=new Runnable(){public void run(){if(player==null||!prepared)return;try{int pos=player.getCurrentPosition(),duration=player.getDuration();time.setText(format(pos)+" / "+format(duration));syncIllustration(pos);}catch(IllegalStateException ignored){}handler.postDelayed(this,250);}};
    String format(int ms){return String.format(Locale.US,"%d:%02d",Math.max(0,ms)/60000,Math.max(0,ms)/1000%60);}
    void toggle(){if(player!=null&&prepared){if(player.isPlaying()){player.pause();status.setText("Paused · OK: resume · Left/Right: seek · Back: library");}else{player.start();status.setText("Playing · OK: pause · Left/Right: seek · Back: library");}}}
    void seek(int delta){if(player!=null&&prepared)player.seekTo(Math.max(0,Math.min(player.getDuration(),player.getCurrentPosition()+delta)));}
    void release(){
        ++artGeneration;
        storyBeats=null;storyImages=null;beatStarts=null;wantedArt="";
        if(illustration!=null)illustration.setImageDrawable(null);
        illustration=null;caption=null;artwork.evictAll();failedArt.clear();
        handler.removeCallbacks(update);if(player!=null){if(prepared)try{getPreferences(0).edit().putInt("pos-"+currentUrl.hashCode(),player.getCurrentPosition()).apply();}catch(Exception ignored){}player.release();player=null;}prepared=false;surface=null;}
    @Override protected void onPause(){super.onPause();if(player!=null&&prepared){player.pause();getPreferences(0).edit().putInt("pos-"+currentUrl.hashCode(),player.getCurrentPosition()).apply();}}
    @Override protected void onDestroy(){++generation;release();artWorker.shutdownNow();super.onDestroy();}
    @Override public void onBackPressed(){home();}
    @Override public boolean dispatchKeyEvent(KeyEvent e){if(inPlayer&&e.getAction()==KeyEvent.ACTION_DOWN){int k=e.getKeyCode();if(k==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE||k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER){toggle();return true;}if(k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_MEDIA_REWIND){seek(-15000);return true;}if(k==KeyEvent.KEYCODE_DPAD_RIGHT||k==KeyEvent.KEYCODE_MEDIA_FAST_FORWARD){seek(15000);return true;}}return super.dispatchKeyEvent(e);}
}
