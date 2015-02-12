package com.soapwang.simplemp3player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,OnSeekBarChangeListener {

	private static final int UPDATE_UI = 1;
	private static final int REPEAT_OFF = 10;
	private static final int REPEAT_CURRENT = 11;
	private static final int REPEAT_LIST = 12;
	
	static final String TAG ="mp3player";
	private ImageButton playBtn;
	private ImageButton backwardBtn;
	private ImageButton forwardBtn;
	private ImageButton repeatBtn;
	private ImageButton menuBtn;
	private TextView durationText;
	private TextView nameText;
	private TextView currentText;
	private TextView seekText;
	private TextView number;
	private SeekBar seekBar;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private ArrayList<String> pathList = new ArrayList<String>();
	private ArrayList<String> nameList = new ArrayList<String>();
	private boolean isPlaying = false;
	private boolean trackingTouch = false; 
	private int seekToPostion;
	private int currentProgress;
	private int currentPosition = 0;
	private int duration = 0;
	private int index = 0;
	private int listSize = 0;
	private int repeatMode;
	private File musicDir;
	RelativeLayout panel;
	Bitmap albumCover = getLoacalBitmap("/sdcard/AirStream/cover.jpg");
	private PopupWindow popupWindow; 
	ContentResolver resolver;
	
	AnimationSet showAnimSet = new AnimationSet(true);
	AnimationSet hideAnimSet = new AnimationSet(true);  
	TranslateAnimation tranShowAction;
	TranslateAnimation tranHideAction;
	TranslateAnimation panelShowAction;
	AlphaAnimation alShowAction;
	AlphaAnimation alHideAction;	
	
	private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case UPDATE_UI:
    			number.setText(index+1+"/"+listSize);
    			currentPosition = (int) Math.floor(mediaPlayer.getCurrentPosition()/1000);
    			int currentMin = currentPosition/60;
    			int currentSec = currentPosition - 60*currentMin;
    			if(currentSec < 10)
    				currentText.setText(currentMin + ":0" + currentSec +"/");
    			else 
    				currentText.setText(currentMin + ":" + currentSec +"/");
    			double progress = ((currentPosition/1.0)/(duration/1.0));
    			//Log.d("TAG","progress=" + progress);
    			if(!trackingTouch) {  				
    				if(progress > 2)
    					progress = 0;
    				seekBar.setProgress((int)(progress*200));
    			}
    			break;
    		default:
    			break;
    			
    		}
    	}
    };

    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        initPopupWindow();
        resolver = MainActivity.this.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
        		  MediaStore.Audio.Media.DEFAULT_SORT_ORDER); 
        panel = (RelativeLayout) findViewById(R.id.panel);
        menuBtn = (ImageButton)findViewById(R.id.menu);
        menuBtn.setOnClickListener(this);
        playBtn = (ImageButton)findViewById(R.id.play);
        playBtn.setOnClickListener(this);
        backwardBtn = (ImageButton)findViewById(R.id.backward);
        backwardBtn.setOnClickListener(this);
        forwardBtn = (ImageButton)findViewById(R.id.forward);       
        forwardBtn.setOnClickListener(this);
        repeatBtn = (ImageButton)findViewById(R.id.repeat);
        repeatBtn.setOnClickListener(this);
        repeatMode = REPEAT_OFF;
		repeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.repeat_off));
        durationText = (TextView)findViewById(R.id.duration);
        nameText = (TextView)findViewById(R.id.track_name);
        currentText = (TextView)findViewById(R.id.current_position);
        seekText = (TextView)findViewById(R.id.seek_text);
        number = (TextView)findViewById(R.id.number);
        //albumCoverView = (ImageButton)findViewById(R.id.album_cover);
        /*
        LayoutParams params = seekText.getLayoutParams();

        MarginLayoutParams marginParams = new MarginLayoutParams(seekText.getLayoutParams());
         
        marginParams.setMargins(currentPosition, currentPosition, currentPosition, currentPosition);
        */ 
        seekText.setVisibility(View.GONE);
        seekBar = (SeekBar)findViewById(R.id.seekBar1);
        seekBar.setMax(200);
        seekBar.setOnSeekBarChangeListener(this);
        
        tranShowAction = new TranslateAnimation(
        		Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,
        		0.7f,Animation.RELATIVE_TO_SELF,0);
        tranShowAction.setDuration(300);
        tranHideAction = new TranslateAnimation(
        		Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,
        		0,Animation.RELATIVE_TO_SELF,1.0f);
        tranHideAction.setDuration(300);
        alShowAction = new AlphaAnimation(0, 1);
        alShowAction.setDuration(300);
        alHideAction = new AlphaAnimation(1, 0);
        alHideAction.setDuration(300);
        showAnimSet.addAnimation(tranShowAction);
        showAnimSet.addAnimation(alShowAction);
        hideAnimSet.addAnimation(tranHideAction);
        hideAnimSet.addAnimation(alHideAction);
        
        panelShowAction = new TranslateAnimation(
        		Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,
        		2.0f,Animation.RELATIVE_TO_SELF,0);
        panelShowAction.setInterpolator(new DecelerateInterpolator());
        panelShowAction.setDuration(750);
        panelShowAction.setStartTime(1100);
        panel.startAnimation(panelShowAction);   
        
        File exStorageDirectory = Environment.getExternalStorageDirectory();
        File[] files = exStorageDirectory.listFiles();
        for(int i = 0; i < files.length; i++)
    	{
        	if(files[i].getName().equals("music")) {  
    			musicDir = files[i];
        	}
    	}
        scan(musicDir);
        listSize = pathList.size();
        //albumCoverView.setImageBitmap(albumCover);
        if(listSize != 0) {
        	number.setText("1/"+listSize); 
        	Log.d("","Number="+listSize);
        	initMediaPlayer(index);
        }
    }
	
	protected void onDestory() {
		super.onDestroy();
		if(mediaPlayer != null) {
			isPlaying = false;
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}

	public void initMediaPlayer(int i) {
		mediaPlayer.reset();
		try {
			//File file = new File(Environment.getExternalStorageDirectory(),"test.mp3");
			String trackName = nameList.get(i);
			nameText.setText(trackName);
			mediaPlayer.setDataSource(pathList.get(i));
			mediaPlayer.prepare();
			duration = (int) Math.floor(mediaPlayer.getDuration()/1000);
			int durationMin = duration/60;
			int durationSec = duration - durationMin*60;
			if(durationSec < 10)
				durationText.setText(durationMin + ":0" + durationSec);
			else
				durationText.setText(durationMin + ":" + durationSec);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//music file scan in directory music
    public void scan(File file)
    {       
        if(file.isDirectory())
        {
        	File[] files = file.listFiles();
        	for(int i = 0; i < files.length; i++)
        	{
        		scan(files[i]);
        	}

        }
        else
        {
            if(isAudio(file))
            {
            	pathList.add(file.getPath());
            	nameList.add(file.getName());
            }
        }       
              
    }
    public boolean isAudio(File file)
    {       
        if(file.getName().endsWith(".mp3") || file.getName().endsWith(".m4a") ||
        		file.getName().endsWith(".wav"))
        {
            return true;
        }
        else
        {
            return false;
        }       
    }
    public static Bitmap getLoacalBitmap(String url) {
        try {
             FileInputStream fis = new FileInputStream(url);
             return BitmapFactory.decodeStream(fis);

          } catch (FileNotFoundException e) {
             e.printStackTrace();
             return null;
        }
   }
	
    //
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.play:
			if(listSize == 0)
				break;
			if(!mediaPlayer.isPlaying()) {
				playBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause));
				mediaPlayer.start();
				isPlaying = true;					
			    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){  
			        public void onCompletion(MediaPlayer mPlyaer) {  
			        	//seekBar.setProgress(0);
			        	//currentText.setText("0:00/");
			        	mPlyaer.stop();
			        	mPlyaer.reset();
			        	if(repeatMode == REPEAT_CURRENT) {
				        	initMediaPlayer(index);
				        	mediaPlayer.start();
				        	currentPosition = (int) Math.floor(mediaPlayer.getCurrentPosition()/1000);
			        	} else if(repeatMode == REPEAT_LIST) {
			        		if(index < (nameList.size()-1))
			        			index++;			
			        		else {						
			        			index = 0;
			        		}
			        		initMediaPlayer(index);
			        		mediaPlayer.start();
			        		currentPosition = (int) Math.floor(mediaPlayer.getCurrentPosition()/1000);
			        	} else {
			        		if(index < (nameList.size()-1)) {
			        			index++;			
				        		initMediaPlayer(index);
				        		mediaPlayer.start();
				        		currentPosition = (int) Math.floor(mediaPlayer.getCurrentPosition()/1000);
			        		} else {
			        			index = 0;
				        		initMediaPlayer(index);
				        		playBtn.setImageDrawable(getResources().getDrawable(R.drawable.play));
								isPlaying = false;
			        		}
			        	}
			         } 
			    });			    
				new Thread(new Runnable() {
					public void run() {
						while (isPlaying) {							
							Message message = new Message();
							message.what = UPDATE_UI;
							handler.sendMessage(message);
							try {
				                Thread.sleep(200);		                
				            } catch (InterruptedException e) {
				                e.printStackTrace();
				            }
						} 
					}
						
				}).start();
			}
			else {
				playBtn.setImageDrawable(getResources().getDrawable(R.drawable.play));
				mediaPlayer.pause();
				isPlaying = false;
			}
			break;
			
		case R.id.backward:
			if(listSize == 0)
				break;
			if(index != 0)
				index--;
			else {
				//Toast.makeText(MainActivity.this, "It is the first track！", Toast.LENGTH_SHORT).show();
				index = nameList.size()-1;
			}
			seekBar.setProgress(0);
			currentText.setText("0:00/");
			initMediaPlayer(index);
			number.setText(index+1+"/"+listSize);
			if(isPlaying)
				mediaPlayer.start();
			break;
			
		case R.id.forward:
			if(listSize == 0)
				break;
			if(index < (nameList.size()-1))
				index++;			
			else {
				index = 0;
			}
			seekBar.setProgress(0);
			currentText.setText("0:00/");
			number.setText(index+1+"/"+listSize);
			initMediaPlayer(index);
			if(isPlaying)
				mediaPlayer.start();
			break;
			//repeat off->list->current->off
		case R.id.repeat:
			switch(repeatMode) {
			case REPEAT_OFF:
				repeatMode = REPEAT_LIST;
				repeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.repeat_all));
				break;
			case REPEAT_CURRENT:
				repeatMode = REPEAT_OFF;
				repeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.repeat_off));
				break;
			case REPEAT_LIST:
				repeatMode = REPEAT_CURRENT;
				repeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.repeat_1));
				break;
			}
			break;
			
		case R.id.menu:
			showPopupWindow();
			break;
		}
		
	}
	
	public void onStartTrackingTouch(SeekBar seekBar) {
        switch(seekBar.getId()) {
        case R.id.seekBar1:
        	trackingTouch = true;     	
        	seekText.setVisibility(View.VISIBLE);
        	seekText.startAnimation(showAnimSet);     	
            break;
        default:
            break;
        }
    }
    
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch(seekBar.getId()) {
        case R.id.seekBar1:
        	trackingTouch = false;
        	mediaPlayer.seekTo(seekToPostion);          	
        	seekText.setVisibility(View.GONE);
        	seekText.startAnimation(hideAnimSet);
        	Message message = new Message();
			message.what = UPDATE_UI;
			handler.sendMessage(message);
            break;
        default:
            break;
        }
    }
    
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
        //Log.d(TAG, "seekid:"+seekBar.getId()+", progess"+progress);
        switch(seekBar.getId()) {
            case R.id.seekBar1:
            	if(trackingTouch) {
            		double tmp = progress/200.0;
            		double seekToTime;
            		double seekTimeInSec;
            		seekToTime = tmp * (mediaPlayer.getDuration());  
            		seekTimeInSec = seekToTime/1000;
            		seekToPostion = (int)seekToTime;
            		int currentMin = (int) (seekTimeInSec/60);
            		int currentSec = (int) (seekTimeInSec - 60*currentMin);
            		if(currentSec < 10)
            			seekText.setText(currentMin+":0"+currentSec);
            		else
            			seekText.setText(currentMin+":"+currentSec);
            		Log.d("TAG","seekToTime="+currentMin + ":" + currentSec);
            	}
                break;
            default:
                break;
        }
    }

    private void showPopupWindow() {
		if(!popupWindow.isShowing()){  
            popupWindow.showAsDropDown(menuBtn, -150, -500);
		} else {
			 popupWindow.dismiss();
		}
    }
    
    private void initPopupWindow() {
    	View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);
    	popupWindow = new PopupWindow(popupView, 250, 400, true);
    	popupWindow.setTouchable(true);
    	popupWindow.setOutsideTouchable(true);
    	//can not dismiss() without setting background
    	popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
