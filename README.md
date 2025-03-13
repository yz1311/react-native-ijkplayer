# React Native IJKPlayer

基于ijkplayer的React Native视频播放器组件，支持Android平台。

## 安装

```bash
npm install @yz1311/react-native-ijkplayer --save
# 或者
yarn add @yz1311/react-native-ijkplayer
```

## 链接

React Native 0.60及以上版本会自动链接原生模块，无需手动操作。

对于React Native 0.59及以下版本，需要手动链接：

```bash
react-native link @yz1311/react-native-ijkplayer
```

## 使用方法

```javascript
import React, { Component } from 'react';
import { View, Button, StyleSheet } from 'react-native';
import { IJKPlayer } from '@yz1311/react-native-ijkplayer';

export default class VideoPlayer extends Component {
  player = null;

  async componentDidMount() {
    // 组件挂载后，播放器会自动创建
    // 可以在这里设置一些初始化配置
  }

  playVideo = async () => {
    try {
      // 设置视频源
      await this.player.setDataSource('https://example.com/video.mp4');
      // 准备播放
      await this.player.prepareAsync();
      // 开始播放
      await this.player.start();
    } catch (e) {
      console.error('播放失败', e);
    }
  };

  pauseVideo = async () => {
    try {
      await this.player.pause();
    } catch (e) {
      console.error('暂停失败', e);
    }
  };

  stopVideo = async () => {
    try {
      await this.player.stop();
    } catch (e) {
      console.error('停止失败', e);
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <IJKPlayer
          ref={ref => (this.player = ref)}
          style={styles.player}
          scaleType="fitParent"
          volume={1.0}
          mute={false}
          onLoad={event => console.log('视频加载完成', event)}
          onError={event => console.log('播放错误', event)}
          onProgress={event => console.log('播放进度', event)}
          onEnd={() => console.log('播放结束')}
          onBuffering={event => console.log('缓冲状态', event)}
          onStateChanged={event => console.log('播放状态变化', event)}
        />
        <View style={styles.controls}>
          <Button title="播放" onPress={this.playVideo} />
          <Button title="暂停" onPress={this.pauseVideo} />
          <Button title="停止" onPress={this.stopVideo} />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  player: {
    flex: 1,
    backgroundColor: '#000',
  },
  controls: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 10,
  },
});
```

## API

### IJKPlayer 组件

#### 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| scaleType | string | 'fitParent' | 视频缩放类型，可选值：'fitParent', 'fillParent', 'wrapContent', 'fitXY', 'centerCrop' |
| volume | number | 1.0 | 音量大小，范围0.0-1.0 |
| mute | boolean | false | 是否静音 |

#### 事件

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| onLoad | 视频加载完成 | { duration, width, height } |
| onError | 播放错误 | { code, message } |
| onProgress | 播放进度 | { currentPosition, duration, bufferedPosition } |
| onEnd | 播放结束 | {} |
| onBuffering | 缓冲状态 | { buffering } |
| onStateChanged | 播放状态变化 | { state } |

#### 方法

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| setDataSource | (url, options) | Promise | 设置播放源 |
| prepareAsync | () | Promise | 异步准备播放 |
| start | () | Promise | 开始播放 |
| pause | () | Promise | 暂停播放 |
| stop | () | Promise | 停止播放 |
| reset | () | Promise | 重置播放器 |
| seekTo | (msec) | Promise | 跳转到指定位置 |
| setVolume | (volume) | Promise | 设置音量 |
| setScreenOn | (on) | Promise | 设置屏幕常亮 |
| setAudioFocus | (request) | Promise | 设置音频焦点 |

### IJKPlayerModule

可以通过`import { IJKPlayerModule } from '@yz1311/react-native-ijkplayer'`导入模块，直接调用原生方法。

## 播放器状态

IJKPlayerModule提供了以下播放器状态常量：

- STATE_IDLE: 空闲状态
- STATE_INITIALIZED: 初始化状态
- STATE_PREPARING: 准备中状态
- STATE_PREPARED: 准备完成状态
- STATE_STARTED: 播放中状态
- STATE_PAUSED: 暂停状态
- STATE_COMPLETED: 播放完成状态
- STATE_STOPPED: 停止状态
- STATE_ERROR: 错误状态
- STATE_END: 结束状态

## 许可证

MIT
