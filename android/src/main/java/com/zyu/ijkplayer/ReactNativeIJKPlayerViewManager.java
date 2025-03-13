package com.zyu.ijkplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

// 导入FijkPlayer类
import com.zyu.ijkplayer.FijkPlayer ;

public class ReactNativeIJKPlayerViewManager extends SimpleViewManager<ReactNativeIJKPlayerView> {
    private static final String REACT_CLASS = "ReactNativeIJKPlayerView";

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected ReactNativeIJKPlayerView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new ReactNativeIJKPlayerView(reactContext);
    }

    @ReactProp(name = "playerId")
    public void setPlayerId(ReactNativeIJKPlayerView view, int playerId) {
        view.setPlayerId(playerId);
    }

    @ReactProp(name = "scaleType")
    public void setScaleType(ReactNativeIJKPlayerView view, String scaleType) {
        view.setScaleType(scaleType);
    }

    @ReactProp(name = "volume")
    public void setVolume(ReactNativeIJKPlayerView view, float volume) {
        view.setVolume(volume);
    }

    @ReactProp(name = "mute")
    public void setMute(ReactNativeIJKPlayerView view, boolean mute) {
        view.setMute(mute);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onLoad", MapBuilder.of("registrationName", "onLoad"))
                .put("onError", MapBuilder.of("registrationName", "onError"))
                .put("onProgress", MapBuilder.of("registrationName", "onProgress"))
                .put("onEnd", MapBuilder.of("registrationName", "onEnd"))
                .put("onBuffering", MapBuilder.of("registrationName", "onBuffering"))
                .put("onStateChanged", MapBuilder.of("registrationName", "onStateChanged"))
                .put("onSeekComplete", MapBuilder.of("registrationName", "onSeekComplete"))
                .put("onVideoSizeChanged", MapBuilder.of("registrationName", "onVideoSizeChanged"))
                .build();
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "play", 1,
                "pause", 2,
                "stop", 3,
                "seekTo", 4,
                "snapshot", 5
        );
    }

    @Override
    public void receiveCommand(@NonNull ReactNativeIJKPlayerView view, int commandId, @Nullable ReadableArray args) {
        ReactContext reactContext = (ReactContext) view.getContext();
        ReactNativeIJKPlayerModule module = reactContext.getNativeModule(ReactNativeIJKPlayerModule.class);
        if (module == null) return;
        
        int playerId = -1;
        if (args != null && args.size() > 0) {
            playerId = args.getInt(0);
        }
        
        FijkPlayer player = module.getPlayer(playerId);
        if (player == null) return;
        
        switch (commandId) {
            case 1: // play
                player.start();
                break;
            case 2: // pause
                player.pause();
                break;
            case 3: // stop
                player.stop();
                break;
            case 4: // seekTo
                if (args != null && args.size() > 1) {
                    player.seekTo(args.getInt(1));
                }
                break;
            case 5: // snapshot
                // 暂不实现截图功能
                break;
        }
    }
} 