'use strict';

import React,{} from 'react';
import { NativeModules, requireNativeComponent, UIManager, findNodeHandle } from 'react-native';
import React, { Component } from 'react';

const { ReactNativeIJKPlayerModule } = NativeModules;
const ReactNativeIJKPlayerView = requireNativeComponent('ReactNativeIJKPlayerView');

class IJKPlayer extends Component {
	constructor(props) {
		super(props);
		this.state = {
			playerId: -1
		};
		this._onLoad = this._onLoad.bind(this);
		this._onError = this._onError.bind(this);
		this._onProgress = this._onProgress.bind(this);
		this._onEnd = this._onEnd.bind(this);
		this._onBuffering = this._onBuffering.bind(this);
		this._onStateChanged = this._onStateChanged.bind(this);
	}

	async componentDidMount() {
		try {
			const result = await ReactNativeIJKPlayerModule.createPlayer();
			this.setState({ playerId: result.id });
		} catch (e) {
			console.error('Failed to create player', e);
		}
	}

	componentWillUnmount() {
		if (this.state.playerId !== -1) {
			ReactNativeIJKPlayerModule.releasePlayer(this.state.playerId);
		}
	}

	_onLoad(event) {
		if (this.props.onLoad) {
			this.props.onLoad(event.nativeEvent);
		}
	}

	_onError(event) {
		if (this.props.onError) {
			this.props.onError(event.nativeEvent);
		}
	}

	_onProgress(event) {
		if (this.props.onProgress) {
			this.props.onProgress(event.nativeEvent);
		}
	}

	_onEnd(event) {
		if (this.props.onEnd) {
			this.props.onEnd(event.nativeEvent);
		}
	}

	_onBuffering(event) {
		if (this.props.onBuffering) {
			this.props.onBuffering(event.nativeEvent);
		}
	}

	_onStateChanged(event) {
		if (this.props.onStateChanged) {
			this.props.onStateChanged(event.nativeEvent);
		}
	}

	setDataSource(url, options = {}) {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.setDataSource(this.state.playerId, url, options);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	prepareAsync() {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.prepareAsync(this.state.playerId);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	start() {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.start(this.state.playerId);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	pause() {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.pause(this.state.playerId);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	stop() {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.stop(this.state.playerId);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	reset() {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.reset(this.state.playerId);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	seekTo(msec) {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.seekTo(this.state.playerId, msec);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	setVolume(volume) {
		if (this.state.playerId !== -1) {
			return ReactNativeIJKPlayerModule.setVolume(this.state.playerId, volume);
		}
		return Promise.reject(new Error('Player not initialized'));
	}

	setScreenOn(on) {
		return ReactNativeIJKPlayerModule.setScreenOn(on);
	}

	setAudioFocus(request) {
		return ReactNativeIJKPlayerModule.setAudioFocus(request);
	}

	_playCommand() {
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.playerView),
			UIManager.getViewManagerConfig('ReactNativeIJKPlayerView').Commands.play,
			[]
		);
	}

	_pauseCommand() {
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.playerView),
			UIManager.getViewManagerConfig('ReactNativeIJKPlayerView').Commands.pause,
			[]
		);
	}

	_stopCommand() {
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.playerView),
			UIManager.getViewManagerConfig('ReactNativeIJKPlayerView').Commands.stop,
			[]
		);
	}

	_seekToCommand(msec) {
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.playerView),
			UIManager.getViewManagerConfig('ReactNativeIJKPlayerView').Commands.seekTo,
			[msec]
		);
	}

	_snapshotCommand() {
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.playerView),
			UIManager.getViewManagerConfig('ReactNativeIJKPlayerView').Commands.snapshot,
			[]
		);
	}

	render() {
		const { style, scaleType, volume, mute, ...restProps } = this.props;
		return (
			<ReactNativeIJKPlayerView
				ref={ref => (this.playerView = ref)}
				style={style}
				playerId={this.state.playerId}
				scaleType={scaleType}
				volume={volume}
				mute={mute}
				onLoad={this._onLoad}
				onError={this._onError}
				onProgress={this._onProgress}
				onEnd={this._onEnd}
				onBuffering={this._onBuffering}
				onStateChanged={this._onStateChanged}
				{...restProps}
			/>
		);
	}
}

// 导出组件和模块
export { IJKPlayer, ReactNativeIJKPlayerModule as IJKPlayerModule };

