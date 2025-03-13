import * as React from 'react';
import {Omit, StyleProp, TextStyle, ViewProps, ViewStyle} from 'react-native';
import { Component } from 'react';


export interface IJKPlayerProps extends ViewProps {
  /**
   * 视频缩放类型
   */
  scaleType?: 'fitParent' | 'fillParent' | 'wrapContent' | 'fitXY' | 'centerCrop';
  
  /**
   * 音量大小，范围0.0-1.0
   */
  volume?: number;
  
  /**
   * 是否静音
   */
  mute?: boolean;
  
  /**
   * 视频加载完成回调
   */
  onLoad?: (event: { duration: number; width: number; height: number }) => void;
  
  /**
   * 播放错误回调
   */
  onError?: (event: { code: number; message: string }) => void;
  
  /**
   * 播放进度回调
   */
  onProgress?: (event: { currentPosition: number; duration: number; bufferedPosition: number }) => void;
  
  /**
   * 播放结束回调
   */
  onEnd?: (event: {}) => void;
  
  /**
   * 缓冲状态回调
   */
  onBuffering?: (event: { buffering: boolean }) => void;
  
  /**
   * 播放状态变化回调
   */
  onStateChanged?: (event: { state: number }) => void;
}

export interface IJKPlayerModule {
  /**
   * 播放器状态常量
   */
  STATE_IDLE: number;
  STATE_INITIALIZED: number;
  STATE_PREPARING: number;
  STATE_PREPARED: number;
  STATE_STARTED: number;
  STATE_PAUSED: number;
  STATE_COMPLETED: number;
  STATE_STOPPED: number;
  STATE_ERROR: number;
  STATE_END: number;
  
  /**
   * 创建播放器实例
   */
  createPlayer(): Promise<{ id: number }>;
  
  /**
   * 释放播放器实例
   */
  releasePlayer(playerId: number): Promise<void>;
  
  /**
   * 设置播放源
   */
  setDataSource(playerId: number, url: string, options?: object): Promise<void>;
  
  /**
   * 异步准备播放
   */
  prepareAsync(playerId: number): Promise<void>;
  
  /**
   * 开始播放
   */
  start(playerId: number): Promise<void>;
  
  /**
   * 暂停播放
   */
  pause(playerId: number): Promise<void>;
  
  /**
   * 停止播放
   */
  stop(playerId: number): Promise<void>;
  
  /**
   * 重置播放器
   */
  reset(playerId: number): Promise<void>;
  
  /**
   * 跳转到指定位置
   */
  seekTo(playerId: number, msec: number): Promise<void>;
  
  /**
   * 设置音量
   */
  setVolume(playerId: number, volume: number): Promise<void>;
  
  /**
   * 设置屏幕常亮
   */
  setScreenOn(on: boolean): Promise<void>;
  
  /**
   * 设置音频焦点
   */
  setAudioFocus(request: boolean): Promise<void>;
}

export class IJKPlayer extends Component<IJKPlayerProps> {
  /**
   * 设置播放源
   */
  setDataSource(url: string, options?: object): Promise<void>;
  
  /**
   * 异步准备播放
   */
  prepareAsync(): Promise<void>;
  
  /**
   * 开始播放
   */
  start(): Promise<void>;
  
  /**
   * 暂停播放
   */
  pause(): Promise<void>;
  
  /**
   * 停止播放
   */
  stop(): Promise<void>;
  
  /**
   * 重置播放器
   */
  reset(): Promise<void>;
  
  /**
   * 跳转到指定位置
   */
  seekTo(msec: number): Promise<void>;
  
  /**
   * 设置音量
   */
  setVolume(volume: number): Promise<void>;
  
  /**
   * 设置屏幕常亮
   */
  setScreenOn(on: boolean): Promise<void>;
  
  /**
   * 设置音频焦点
   */
  setAudioFocus(request: boolean): Promise<void>;
}

export const IJKPlayerModule: IJKPlayerModule;
