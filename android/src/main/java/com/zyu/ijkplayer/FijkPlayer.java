//MIT License
//
//Copyright (c) [2019-2020] [Befovy]
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package com.zyu.ijkplayer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

public class FijkPlayer implements IMediaPlayer.OnPreparedListener, 
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnBufferingUpdateListener, 
        IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener, 
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "FijkPlayer";
    final private static AtomicInteger atomicId = new AtomicInteger(0);

    // 播放器状态常量
    final public static int STATE_IDLE = 0;
    final public static int STATE_INITIALIZED = 1;
    final public static int STATE_ASYNC_PREPARING = 2;
    final public static int STATE_PREPARED = 3;
    final public static int STATE_STARTED = 4;
    final public static int STATE_PAUSED = 5;
    final public static int STATE_COMPLETED = 6;
    final public static int STATE_STOPPED = 7;
    final public static int STATE_ERROR = 8;
    final public static int STATE_END = 9;

    final private int mPlayerId;
    final private IjkMediaPlayer mIjkMediaPlayer;
    final private FijkEngine mEngine;
    final private HostOption mHostOptions = new HostOption();

    private int mState;
    private int mRotate = -1;
    private int mWidth = 0;
    private int mHeight = 0;
    private Surface mSurface;
    
    // 播放器事件监听器
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public FijkPlayer(@NonNull FijkEngine engine, boolean justSurface) {
        mEngine = engine;
        mPlayerId = atomicId.incrementAndGet();
        mState = STATE_IDLE;
        
        if (justSurface) {
            mIjkMediaPlayer = null;
        } else {
            mIjkMediaPlayer = new IjkMediaPlayer();
            mIjkMediaPlayer.setOnPreparedListener(this);
            mIjkMediaPlayer.setOnCompletionListener(this);
            mIjkMediaPlayer.setOnBufferingUpdateListener(this);
            mIjkMediaPlayer.setOnSeekCompleteListener(this);
            mIjkMediaPlayer.setOnErrorListener(this);
            mIjkMediaPlayer.setOnInfoListener(this);
            mIjkMediaPlayer.setOnVideoSizeChangedListener(this);
            
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-position-notify", 1);
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        }
    }

    public int getPlayerId() {
        return mPlayerId;
    }

    public void setup() {
        if (mIjkMediaPlayer == null)
            return;
        if (mHostOptions.getIntOption(HostOption.ENABLE_SNAPSHOT, 0) > 0) {
            mIjkMediaPlayer.setAmcGlesRender();
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", "fcc-_es2");
        }
    }

    public void setupSurface() {
        setup();
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.setSurface(surface);
        }
    }

    public void release() {
        if (mIjkMediaPlayer != null) {
            mState = STATE_END;
            mIjkMediaPlayer.release();
        }
        
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    private boolean isPlayable(int state) {
        return state == STATE_STARTED || state == STATE_PAUSED || state == STATE_COMPLETED || state == STATE_PREPARED;
    }

    private void onStateChanged(int newState, int oldState) {
        mState = newState;
        
        if (newState == STATE_STARTED && oldState != STATE_STARTED) {
            mEngine.onPlayingChange(1);

            if (mHostOptions.getIntOption(HostOption.REQUEST_AUDIOFOCUS, 0) == 1) {
                mEngine.audioFocus(true);
            }

            if (mHostOptions.getIntOption(HostOption.REQUEST_SCREENON, 0) == 1) {
                mEngine.setScreenOn(true);
            }
        } else if (newState != STATE_STARTED && oldState == STATE_STARTED) {
            mEngine.onPlayingChange(-1);

            if (mHostOptions.getIntOption(HostOption.RELEASE_AUDIOFOCUS, 0) == 1) {
                mEngine.audioFocus(false);
            }

            if (mHostOptions.getIntOption(HostOption.REQUEST_SCREENON, 0) == 1) {
                mEngine.setScreenOn(false);
            }
        }

        if (isPlayable(newState) && !isPlayable(oldState)) {
            mEngine.onPlayableChange(1);
        } else if (!isPlayable(newState) && isPlayable(oldState)) {
            mEngine.onPlayableChange(-1);
        }
    }

    public void setDataSource(String url) throws IOException {
        if (mIjkMediaPlayer == null)
            return;
            
        Uri uri = Uri.parse(url);
        boolean openAsset = false;
        if ("asset".equals(uri.getScheme())) {
            openAsset = true;
            String host = uri.getHost();
            String path = uri.getPath() != null ? uri.getPath().substring(1) : "";
            String asset = mEngine.lookupKeyForAsset(path, host);
            if (!TextUtils.isEmpty(asset)) {
                uri = Uri.parse(asset);
            }
        }
        
        Context context = mEngine.context();
        if (openAsset && context != null) {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(uri.getPath() != null ? uri.getPath() : "", AssetManager.ACCESS_RANDOM);
            mIjkMediaPlayer.setDataSource(new RawMediaDataSource(is));
        } else if (context != null){
            if (TextUtils.isEmpty(uri.getScheme()) || "file".equals(uri.getScheme())) {
                String path = uri.getPath() != null ? uri.getPath() : "";
                IMediaDataSource dataSource = new FileMediaDataSource(new File(path));
                mIjkMediaPlayer.setDataSource(dataSource);
            } else {
                mIjkMediaPlayer.setDataSource(context, uri);
            }
        } else {
            throw new IOException("Context is null, can't setDataSource");
        }
        
        onStateChanged(STATE_INITIALIZED, mState);
    }

    public void prepareAsync() {
        if (mIjkMediaPlayer == null)
            return;
            
        setup();
        mIjkMediaPlayer.prepareAsync();
        onStateChanged(STATE_ASYNC_PREPARING, mState);
    }

    public void start() {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.start();
        onStateChanged(STATE_STARTED, mState);
    }

    public void pause() {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.pause();
        onStateChanged(STATE_PAUSED, mState);
    }

    public void stop() {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.stop();
        onStateChanged(STATE_STOPPED, mState);
    }

    public void reset() {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.reset();
        onStateChanged(STATE_IDLE, mState);
    }

    public long getCurrentPosition() {
        if (mIjkMediaPlayer == null)
            return 0;
            
        return mIjkMediaPlayer.getCurrentPosition();
    }

    public long getDuration() {
        if (mIjkMediaPlayer == null)
            return 0;
            
        return mIjkMediaPlayer.getDuration();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void seekTo(long msec) {
        if (mIjkMediaPlayer == null)
            return;
            
        if (mState == STATE_COMPLETED)
            onStateChanged(STATE_PAUSED, mState);
            
        mIjkMediaPlayer.seekTo(msec);
    }

    public void setLoopCount(int loopCount) {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.setLoopCount(loopCount);
    }

    public void setSpeed(float speed) {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.setSpeed(speed);
    }

    public int getVideoWidth() {
        if (mIjkMediaPlayer == null)
            return 0;
            
        return mIjkMediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        if (mIjkMediaPlayer == null)
            return 0;
            
        return mIjkMediaPlayer.getVideoHeight();
    }

    public void setOption(int category, String name, String value) {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.setOption(category, name, value);
    }

    public void setOption(int category, String name, long value) {
        if (mIjkMediaPlayer == null)
            return;
            
        mIjkMediaPlayer.setOption(category, name, value);
    }

    public void setDisplayAspectRatio(int ratio) {
        if (mIjkMediaPlayer == null)
            return;
        
        // 尝试不同的方法名
        // try {
        //     // 方法1: 直接使用setDisplayAspectRatio
        //     mIjkMediaPlayer.setDisplayAspectRatio(ratio);
        // } catch (NoSuchMethodError e1) {
        //     try {
        //         // 方法2: 使用setAspectRatio
        //         mIjkMediaPlayer.setAspectRatio(ratio);
        //     } catch (NoSuchMethodError e2) {
        //         try {
        //             // 方法3: 使用setVideoAspectRatio
        //             mIjkMediaPlayer.setVideoAspectRatio(ratio);
        //         } catch (NoSuchMethodError e3) {
        //             Log.e(TAG, "无法设置宽高比，找不到合适的方法: " + e3.getMessage());
        //         }
        //     }
        // }
    }

    public void setRenderType(int type) {
        // 在React Native中，我们使用SurfaceView
    }

    // 设置监听器
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    // IMediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(IMediaPlayer mp) {
        onStateChanged(STATE_PREPARED, mState);
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mp);
        }
    }

    // IMediaPlayer.OnCompletionListener
    @Override
    public void onCompletion(IMediaPlayer mp) {
        onStateChanged(STATE_COMPLETED, mState);
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
    }

    // IMediaPlayer.OnBufferingUpdateListener
    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    }

    // IMediaPlayer.OnSeekCompleteListener
    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(mp);
        }
    }

    // IMediaPlayer.OnErrorListener
    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        onStateChanged(STATE_ERROR, mState);
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(mp, what, extra);
        }
        return true;
    }

    // IMediaPlayer.OnInfoListener
    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (mOnInfoListener != null) {
            return mOnInfoListener.onInfo(mp, what, extra);
        }
        return false;
    }

    // IMediaPlayer.OnVideoSizeChangedListener
    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
        mWidth = width;
        mHeight = height;
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sarNum, sarDen);
        }
    }
}
