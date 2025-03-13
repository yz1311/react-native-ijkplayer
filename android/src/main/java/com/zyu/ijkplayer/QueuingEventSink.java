//MIT License
//
//Copyright (c) [2019] [Befovy]
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

import androidx.annotation.Nullable;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件队列，用于缓存事件直到有监听器连接
 */
public class QueuingEventSink {
    private final List<Object> eventQueue = new ArrayList<>();
    private DeviceEventManagerModule.RCTDeviceEventEmitter delegate;

    public void setDelegate(@Nullable DeviceEventManagerModule.RCTDeviceEventEmitter delegate) {
        this.delegate = delegate;
        if (delegate == null) {
            return;
        }
        
        // 发送队列中的事件
        for (Object event : eventQueue) {
            if (event instanceof WritableMap) {
                delegate.emit("fijkplayer", (WritableMap) event);
            }
        }
        eventQueue.clear();
    }

    public void success(Object event) {
        if (delegate != null) {
            if (event instanceof WritableMap) {
                delegate.emit("fijkplayer", (WritableMap) event);
            }
        } else {
            eventQueue.add(event);
        }
    }

    public void error(String errorCode, String errorMessage, Object errorDetails) {
        // 在React Native中，我们可以通过发送带有错误信息的事件来处理错误
        if (delegate != null) {
            // 这里可以创建一个包含错误信息的WritableMap并发送
        } else {
            // 可以将错误信息添加到队列中
        }
    }

    public void endOfStream() {
        // 在React Native中，我们可以发送一个特殊的事件表示流结束
        if (delegate != null) {
            // 发送流结束事件
        } else {
            // 可以将流结束事件添加到队列中
        }
    }
}
