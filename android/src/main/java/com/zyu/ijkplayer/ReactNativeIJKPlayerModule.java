package com.zyu.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

// 确保正确引入FijkPlayer类
import com.zyu.ijkplayer.FijkPlayer ;

public class ReactNativeIJKPlayerModule extends ReactContextBaseJavaModule implements LifecycleEventListener, FijkEngine, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "ReactNativeIJKPlayer";
    private static final String MODULE_NAME = "ReactNativeIJKPlayerModule";

    private final ReactApplicationContext reactContext;
    private final SparseArray<FijkPlayer> fijkPlayers = new SparseArray<>();
    private int playableCnt = 0;
    private int playingCnt = 0;
    private boolean mAudioFocusRequested = false;
    private Object mAudioFocusRequest;
    private final QueuingEventSink mEventSink = new QueuingEventSink();

    public ReactNativeIJKPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
        
        // 初始化一个播放器并释放，确保库被正确加载
        final FijkPlayer player = new FijkPlayer(this, true);
        player.setupSurface();
        player.release();
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("STATE_IDLE", 0);
        constants.put("STATE_INITIALIZED", 1);
        constants.put("STATE_PREPARING", 2);
        constants.put("STATE_PREPARED", 3);
        constants.put("STATE_STARTED", 4);
        constants.put("STATE_PAUSED", 5);
        constants.put("STATE_COMPLETED", 6);
        constants.put("STATE_STOPPED", 7);
        constants.put("STATE_ERROR", 8);
        constants.put("STATE_END", 9);
        return constants;
    }

    @ReactMethod
    public void createPlayer(Promise promise) {
        FijkPlayer player = new FijkPlayer(this, false);
        player.setup();
        fijkPlayers.put(player.getPlayerId(), player);
        
        WritableMap result = Arguments.createMap();
        result.putInt("id", player.getPlayerId());
        promise.resolve(result);
    }

    // 添加一个方法用于获取播放器实例，供ReactNativeIJKPlayerView使用
    public FijkPlayer getPlayer(int playerId) {
        return fijkPlayers.get(playerId);
    }

    @ReactMethod
    public void releasePlayer(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            player.release();
            fijkPlayers.remove(playerId);
            promise.resolve(null);
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void setDataSource(int playerId, String url, ReadableMap options, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                // 调用FijkPlayer的setDataSource方法
                player.setDataSource(url);
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("set_data_source_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void prepareAsync(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.prepareAsync();
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("prepare_async_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void start(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.start();
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("start_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void pause(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.pause();
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("pause_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void stop(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.stop();
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("stop_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void reset(int playerId, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.reset();
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("reset_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void seekTo(int playerId, int msec, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.seekTo(msec);
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("seek_to_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void setVolume(int playerId, float volume, Promise promise) {
        FijkPlayer player = fijkPlayers.get(playerId);
        if (player != null) {
            try {
                player.setVolume(volume, volume);
                promise.resolve(null);
            } catch (Exception e) {
                promise.reject("set_volume_error", e.getMessage());
            }
        } else {
            promise.reject("player_not_found", "Player with ID " + playerId + " not found");
        }
    }

    @ReactMethod
    public void setScreenOn(boolean on, Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            if (on) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            promise.resolve(null);
        } else {
            promise.reject("activity_not_found", "Current activity not found");
        }
    }

    @ReactMethod
    public void setAudioFocus(boolean request, Promise promise) {
        audioFocus(request);
        promise.resolve(null);
    }

    @ReactMethod
    public void getSystemVolume(Promise promise) {
        float volume = systemVolume();
        promise.resolve(volume);
    }

    @ReactMethod
    public void setSystemVolume(float volume, Promise promise) {
        float vol = setSystemVolume(volume);
        promise.resolve(vol);
    }

    private void sendEvent(String eventName, WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    // FijkEngine接口实现
    @Nullable
    @Override
    public Object createSurfaceEntry() {
        // 在React Native中，我们不使用TextureRegistry
        return null;
    }

    @Nullable
    @Override
    public Object messenger() {
        // 在React Native中，我们使用ReactContext
        return reactContext;
    }

    @Nullable
    @Override
    public Context context() {
        return reactContext;
    }

    @Nullable
    @Override
    public String lookupKeyForAsset(@NonNull String asset, @Nullable String packageName) {
        // 在React Native中，我们使用不同的方式处理资源
        if (packageName == null) {
            return asset;
        } else {
            return asset;
        }
    }

    @Override
    public void onPlayingChange(int delta) {
        playingCnt += delta;
        Log.d(TAG, "onPlayingChange: " + playingCnt);
    }

    @Override
    public void onPlayableChange(int delta) {
        playableCnt += delta;
        Log.d(TAG, "onPlayableChange: " + playableCnt);
    }

    @Override
    public void setScreenOn(boolean on) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            if (on) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public void audioFocus(boolean request) {
        if (request && !mAudioFocusRequested) {
            requestAudioFocus();
        } else if (!request && mAudioFocusRequested) {
            abandonAudioFocus();
        }
    }

    private void requestAudioFocus() {
        AudioManager am = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioManager.OnAudioFocusChangeListener afChangeListener = this;
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();
            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(afChangeListener)
                    .build();
            int res = am.requestAudioFocus(focusRequest);
            mAudioFocusRequest = focusRequest;
            mAudioFocusRequested = res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            AudioManager.OnAudioFocusChangeListener afChangeListener = this;
            int res = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mAudioFocusRequested = res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mAudioFocusRequest != null) {
                am.abandonAudioFocusRequest((AudioFocusRequest) mAudioFocusRequest);
            }
        } else {
            am.abandonAudioFocus(this);
        }
        mAudioFocusRequested = false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mAudioFocusRequested = false;
                mAudioFocusRequest = null;
                break;
        }
        Log.i(TAG, "onAudioFocusChange: " + focusChange);
    }

    private float systemVolume() {
        AudioManager audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            float max = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float vol = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            return vol / max;
        } else {
            return 0.0f;
        }
    }

    private float setSystemVolume(float vol) {
        AudioManager audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int volIndex = (int) (vol * max);
            volIndex = Math.min(volIndex, max);
            volIndex = Math.max(volIndex, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volIndex, AudioManager.FLAG_SHOW_UI);
            return (float) volIndex / (float) max;
        } else {
            return vol;
        }
    }

    // LifecycleEventListener接口实现
    @Override
    public void onHostResume() {
        // 应用恢复时的处理
    }

    @Override
    public void onHostPause() {
        // 应用暂停时的处理
    }

    @Override
    public void onHostDestroy() {
        // 应用销毁时的处理
        for (int i = 0; i < fijkPlayers.size(); i++) {
            FijkPlayer player = fijkPlayers.valueAt(i);
            if (player != null) {
                player.release();
            }
        }
        fijkPlayers.clear();
    }
} 