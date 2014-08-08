package com.example.pulseglass;


import java.util.Random;

import com.example.pulseglass.DeviceControlActivity;
import com.example.pulseglass.R;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class PulseGlassActivity extends BlunoLibrary {
	
    public final static String TAG = DeviceControlActivity.class.getSimpleName();

    private connectionStateEnum mConnectionState=connectionStateEnum.isNull;
	private PlainProtocol mPlainProtocol= new PlainProtocol();
	private ImageView titleImageView;

    private TextView label;
    private MediaPlayer beat;
    private GraphView graph;
    private ImageView img;
    private Random iRandom;
	
    private void pumpHeart() {
	img.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).setListener(scaleUpListener);
	playBeat();
	graph.setDrawPulse(true);
    }
    
    
    
    private AnimatorListener moveListener = new AnimatorListener() {

    	@Override
    	public void onAnimationStart(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationRepeat(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationEnd(Animator animation) {
    		
    		
    		
    	    img.animate().translationX(iRandom.nextInt(20)).translationY(iRandom.nextInt(20)).setDuration(1000).setListener(moveListener);
    	}

    	@Override
    	public void onAnimationCancel(Animator animation) {
    	    // TODO Auto-generated method stub

    	}
        };

    
    
    
    private AnimatorListener scaleDownListener = new AnimatorListener() {

    	@Override
    	public void onAnimationStart(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationRepeat(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationEnd(Animator animation) {
//    	     img.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).setListener(scaleUpListener);
    		img.animate().translationX(iRandom.nextInt(20)).translationY(iRandom.nextInt(20)).setDuration(1000).setListener(moveListener);
    	}

    	@Override
    	public void onAnimationCancel(Animator animation) {
    	    // TODO Auto-generated method stub

    	}
        };

        private AnimatorListener scaleUpListener = new AnimatorListener() {

    	@Override
    	public void onAnimationStart(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationRepeat(Animator animation) {
    	    // TODO Auto-generated method stub

    	}

    	@Override
    	public void onAnimationEnd(Animator animation) {
    	    img.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50).setListener(scaleDownListener);

    	}

    	@Override
    	public void onAnimationCancel(Animator animation) {
    	    // TODO Auto-generated method stub

    	}
        };
    
    private void playBeat() {
	if(!beat.isPlaying()){
	    beat.start();
	}else{
	    beat.stop();
	    beat.release();
	    beat = MediaPlayer.create(this, R.raw.beat);
	    beat.start();
	}
    }
    
	public void onSerialReceived(String theString) {
		System.out.println("displayData "+theString);
    	
    	mPlainProtocol.mReceivedframe.append(theString) ;
    	System.out.print("mPlainProtocol.mReceivedframe:");
    	System.out.println(mPlainProtocol.mReceivedframe.toString());

    	while(mPlainProtocol.available())
    	{
        	System.out.print("receivedCommand:");
        	System.out.println(mPlainProtocol.receivedCommand);
    		
    		if(mPlainProtocol.receivedCommand.equals("BPM"))
    		{
    			label.setText(String.valueOf(mPlainProtocol.receivedContent[0]));
    			
    			if(mPlainProtocol.receivedContent[0]<60)
    			{
    				img.setImageResource(R.drawable.emo1);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=60 && mPlainProtocol.receivedContent[0]<75)
    			{
    				img.setImageResource(R.drawable.emo2);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=75 && mPlainProtocol.receivedContent[0]<90)
    			{
    				img.setImageResource(R.drawable.emo3);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=90 && mPlainProtocol.receivedContent[0]<100)
    			{
    				img.setImageResource(R.drawable.emo4);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=100 && mPlainProtocol.receivedContent[0]<110)
    			{
    				img.setImageResource(R.drawable.emo5);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=110 && mPlainProtocol.receivedContent[0]<120)
    			{
    				img.setImageResource(R.drawable.emo6);
    			}
    			else if(mPlainProtocol.receivedContent[0]>=120)
    			{
    				img.setImageResource(R.drawable.emo7);
    			}
    			
    			
    			
        		System.out.println("received BPM");

    		}
    		else if(mPlainProtocol.receivedCommand.equals("QS"))
    		{
        		System.out.println("received QS");
        		pumpHeart();
    		}
    	}

	}
	

	
	

	//configure the title image which shows the connection state
	void titleImageConfig()
	{
        titleImageView =  (ImageView)findViewById(R.id.title_image_view);
        titleImageView.setImageResource(R.drawable.title_scan);
        titleImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				buttonScanOnClickProcess();
			}
		});
	}
	

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("BLUNOActivity onCreate");
		setContentView(R.layout.activity_bluno);
		
		
		 beat = MediaPlayer.create(this, R.raw.beat);
		 graph = (GraphView) findViewById(R.id.graph);

		img = (ImageView) findViewById(R.id.indicator);
		label = (TextView) findViewById(R.id.label);
		
		iRandom=new Random();
		
		img.setImageResource(R.drawable.emo1);
		//set the Baudrate of the Serial port
		serialBegin(115200);
		
        onCreateProcess();
		titleImageConfig();
		img.animate().translationX(iRandom.nextInt(20)).translationY(iRandom.nextInt(20)).setDuration(1000).setListener(moveListener);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		onActivityResultProcess(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("BLUNOActivity onPause");
        onPauseProcess();
	}
	
	protected void onStop() {
		super.onStop();
		onStopProcess();
		System.out.println("MiUnoActivity onStop");
		

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("MiUnoActivity onDestroy");
        onDestroyProcess();

	}

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {
	
		mConnectionState=theConnectionState;
		switch (mConnectionState) {
		case isScanning:
	        titleImageView.setImageResource(R.drawable.title_scanning);
			break;

		case isConnected:
	        titleImageView.setImageResource(R.drawable.title_connected);

			break;
		case isConnecting:
	        titleImageView.setImageResource(R.drawable.title_connecting);
			break;
		case isToScan:
	        titleImageView.setImageResource(R.drawable.title_scan);
	        

			break;
		default:
			break;
		}
	}
	

	
	
}
