package com.zyu.ijkplayer;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

// 确保正确引入FijkPlayer类
import com.zyu.ijkplayer.FijkPlayer ;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class ReactNativeIJKPlayerView extends FrameLayout implements IMediaPlayer.OnPreparedListener, 
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnBufferingUpdateListener, 
        IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener, 
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener {
    
    private static final String TAG = "ReactNativeIJKPlayerView";

    private final SurfaceView surfaceView;
    private int playerId = -1;
    private FijkPlayer player;
    private boolean isBuffering = false;
    private int currentBufferPercentage = 0;

    public ReactNativeIJKPlayerView(@NonNull Context context) {
        super(context);
        
        // 创建SurfaceView用于显示视频
        surfaceView = new SurfaceView(context);
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        addView(surfaceView);
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        
        // 从ReactNativeIJKPlayerModule获取对应的播放器实例
        ReactContext reactContext = (ReactContext) getContext();
        ReactNativeIJKPlayerModule module = reactContext.getNativeModule(ReactNativeIJKPlayerModule.class);
        
        if (module != null) {
            // 使用新添加的getPlayer方法获取播放器实例
            player = module.getPlayer(playerId);
            
            if (player != null) {
                // 设置Surface
                player.setSurface(surfaceView.getHolder().getSurface());
                
                // 设置监听器
                player.setOnPreparedListener(this);
                player.setOnCompletionListener(this);
                player.setOnBufferingUpdateListener(this);
                player.setOnSeekCompleteListener(this);
                player.setOnErrorListener(this);
                player.setOnInfoListener(this);
                player.setOnVideoSizeChangedListener(this);
            }
        }
    }

    public void setScaleType(String scaleType) {
        if (player != null) {
            // 设置视频缩放类型
            int scaleTypeValue = 1; // 默认为fitParent
            
            switch (scaleType) {
                case "fitParent":
                    scaleTypeValue = 1;
                    break;
                case "fillParent":
                    scaleTypeValue = 2;
                    break;
                case "wrapContent":
                    scaleTypeValue = 3;
                    break;
                case "fitXY":
                    scaleTypeValue = 4;
                    break;
                case "centerCrop":
                    scaleTypeValue = 5;
                    break;
            }
            
            player.setRenderType(0); // RENDER_SURFACE_VIEW
            player.setDisplayAspectRatio(scaleTypeValue);
        }
    }

    public void setVolume(float volume) {
        if (player != null) {
            // 设置音量
            player.setVolume(volume, volume);
        }
    }

    public void setMute(boolean mute) {
        if (player != null) {
            // 设置静音
            player.setVolume(mute ? 0 : 1, mute ? 0 : 1);
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                eventName,
                params
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        Log.d(TAG, "onViewAdded");
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        Log.d(TAG, "onViewRemoved");
    }

    // IMediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        WritableMap event = Arguments.createMap();
        event.putInt("duration", (int) player.getDuration());
        event.putInt("width", player.getVideoWidth());
        event.putInt("height", player.getVideoHeight());
        sendEvent("onLoad", event);
    }

    // IMediaPlayer.OnCompletionListener
    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        WritableMap event = Arguments.createMap();
        sendEvent("onEnd", event);
    }

    // IMediaPlayer.OnBufferingUpdateListener
    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        currentBufferPercentage = percent;
        
        // 发送进度事件
        WritableMap event = Arguments.createMap();
        event.putInt("currentPosition", (int) player.getCurrentPosition());
        event.putInt("duration", (int) player.getDuration());
        event.putInt("bufferedPosition", (int) (player.getDuration() * percent / 100.0));
        sendEvent("onProgress", event);
    }

    // IMediaPlayer.OnSeekCompleteListener
    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        WritableMap event = Arguments.createMap();
        event.putInt("currentPosition", (int) player.getCurrentPosition());
        sendEvent("onSeekComplete", event);
    }

    // IMediaPlayer.OnErrorListener
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        WritableMap event = Arguments.createMap();
        event.putInt("code", what);
        event.putString("message", "Error: " + what + ", " + extra);
        sendEvent("onError", event);
        return true; // 返回true表示错误已处理
    }

    // IMediaPlayer.OnInfoListener
    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                isBuffering = true;
                WritableMap bufferingStartEvent = Arguments.createMap();
                bufferingStartEvent.putBoolean("buffering", true);
                sendEvent("onBuffering", bufferingStartEvent);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                isBuffering = false;
                WritableMap bufferingEndEvent = Arguments.createMap();
                bufferingEndEvent.putBoolean("buffering", false);
                sendEvent("onBuffering", bufferingEndEvent);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                WritableMap renderingStartEvent = Arguments.createMap();
                renderingStartEvent.putInt("state", 4); // STATE_STARTED
                sendEvent("onStateChanged", renderingStartEvent);
                break;
        }
        return false;
    }

    // IMediaPlayer.OnVideoSizeChangedListener
    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sarNum, int sarDen) {
        WritableMap event = Arguments.createMap();
        event.putInt("width", width);
        event.putInt("height", height);
        sendEvent("onVideoSizeChanged", event);
    }
} 