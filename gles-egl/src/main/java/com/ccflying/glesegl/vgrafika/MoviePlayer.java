/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ccflying.glesegl.vgrafika;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 使用Surface播放视频
 * TODO: needs more advanced shuttle controls (pause/resume, skip)
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MoviePlayer {
    private static final String TAG = "MoviePlayer";
    private static final boolean VERBOSE = false;
    private static final int TIMEOUT_USEC = 10000;

    // Declare this here to reduce allocations.
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    // May be set/read by different threads.
    private volatile boolean mIsStopRequested;

    private File mSourceFile;
    private Surface mOutputSurface;
    private FrameCallback mFrameCallback;
    private boolean mLoop;
    private int mVideoWidth;
    private int mVideoHeight;

    /**
     * Interface to be implemented by class that manages playback UI.
     * <p>
     * Callback methods will be invoked on the UI thread.
     */
    public interface PlayerFeedback {
        void playbackStopped();
    }


    /**
     * Callback invoked when rendering video frames.  The MoviePlayer client must
     * provide one of these.
     */
    public interface FrameCallback {
        /**
         * Called immediately before the frame is rendered.
         *
         * @param presentationTimeUsec The desired presentation time, in microseconds.
         */
        void preRender(long presentationTimeUsec);

        /**
         * Called immediately after the frame render call returns.  The frame may not have
         * actually been rendered yet.
         */
        void postRender();

        /**
         * Called after the last frame of a looped movie has been rendered.  This allows the
         * callback to adjust its expectations of the next presentation time stamp.
         */
        void loopReset();
    }


    /**
     * Constructs a MoviePlayer.
     *
     * @param sourceFile    视频文件
     * @param outputSurface 视频显示画面Surface
     * @param frameCallback Callback object, used to pace output.
     */
    public MoviePlayer(File sourceFile, Surface outputSurface, FrameCallback frameCallback)
            throws IOException {
        mSourceFile = sourceFile;
        mOutputSurface = outputSurface;
        mFrameCallback = frameCallback;

        // 获取视频
        MediaExtractor extractor = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(sourceFile.toString());
            int trackIndex = selectVideoTrack(extractor);

            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + mVideoWidth + "x" + mVideoHeight);
            }
        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * 设置循环模式
     */
    public void setLoopMode(boolean loopMode) {
        mLoop = loopMode;
    }

    /**
     * 停止视频播放器
     */
    public void requestStop() {
        mIsStopRequested = true;
    }

    /**
     * 解码视频流并播放，循环解码直到播放完成，或者被用户停止。
     */
    public void play() throws IOException {
        if (!mSourceFile.canRead()) {
            throw new FileNotFoundException("Unable to read " + mSourceFile);
        }

        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSourceFile.toString());
            int trackIndex = selectVideoTrack(extractor);
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);

            // 根据mime创建解码器，并且配置MediaFormat，It's very important to use the format from the extractor because
            // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, mOutputSurface, null, 0);
            decoder.start();
            // 开启解码工作
            doExtract(extractor, trackIndex, decoder, mFrameCallback);
        } finally {
            // release everything we grabbed
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }

    /**
     * Selects the video track, if any.
     */
    private static int selectVideoTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }

    /**
     * 解码视频并播放
     */
    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           FrameCallback frameCallback) {
        // 我们需要在提供输入和读取输出之间取得平衡，有效地在输出侧没有延迟。
        // 为了避免输出端的延迟，我们需要保持解码器的输入缓冲区处于饱和状态。
        // 在 输入N帧到解码器 到 接收到N帧被解码完成，可能会有很大的延迟，所以需要提前提交帧到解码器。
        //
        // 许多视频解码器在开始解码并输出之前，似乎需要多几帧视频才开始真正的解码工作。
        // 我们需要提前提供一组输入帧，并且尽量保持队列一直处于填充满的状态。
        // 所以我们不能仅仅提供一帧到解码器，然后等待这个帧解码完成。
        //
        // We can't just fixate on the input side though. If we spend too much time trying
        // to stuff the input, we might miss a presentation deadline.  At 60Hz we have 16.7ms
        // between frames, so sleeping for 10ms would eat up a significant fraction of the
        // time allowed.  (Most video is at 30Hz or less, so for most content we'll have
        // significantly longer.)  Waiting for output is okay, but sleeping on availability
        // of input buffers is unwise if we need to be providing output on a regular schedule.
        //
        // In some situations, startup latency may be a concern.  To minimize startup time,
        // we'd want to stuff the input full as quickly as possible.  This turns out to be
        // somewhat complicated, as the codec may still be starting up and will refuse to
        // accept input.  Removing the timeout from dequeueInputBuffer() results in spinning
        // on the CPU.
        //
        // If you have tight startup latency requirements, it would probably be best to
        // "prime the pump" with a sequence of frames that aren't actually shown (e.g.
        // grab the first 10 NAL units and shove them through, then rewind to the start of
        // the first key frame).
        //
        // The actual latency seems to depend on strongly on the nature of the video (e.g.
        // resolution).
        //
        // One conceptually nice approach is to loop on the input side to ensure that the codec
        // always has all the input it can handle.  After submitting a buffer, we immediately
        // check to see if it will accept another.  We can use a short timeout so we don't
        // miss a presentation deadline.  On the output side we only check once, with a longer
        // timeout, then return to the outer loop to see if the codec is hungry for more input.
        //
        // In practice, every call to check for available buffers involves a lot of message-
        // passing between threads and processes.  Setting a very brief timeout doesn't
        // exactly work because the overhead required to determine that no buffer is available
        // is substantial.  On one device, the "clever" approach caused significantly greater
        // and more highly variable startup latency.
        //
        // The code below takes a very simple-minded approach that works, but carries a risk
        // of occasionally running out of output.  A more sophisticated approach might
        // detect an output timeout and use that as a signal to try to enqueue several input
        // buffers on the next iteration.
        //

        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        int inputChunk = 0;
        long firstInputTimeNsec = -1;
        // output输出完成标志
        boolean outputDone = false;
        // input输入完成标志
        boolean inputDone = false;
        while (!outputDone) {
            if (mIsStopRequested) {
                Log.d(TAG, "Stop requested");
                return;
            }

            // Feed more data to the decoder.
            if (!inputDone) {
                // inputIndex
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    if (firstInputTimeNsec == -1) {
                        firstInputTimeNsec = System.nanoTime();
                    }
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // 将 sample data 读取填充到ByteBuffer.
                    int chunkSize = extractor.readSampleData(inputBuf, 0);
                    // End of stream -- send empty frame with EOS flag set.
                    if (chunkSize < 0) {
                        // EOS，设置 BUFFER_FLAG_END_OF_STREAM 标志。
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        // extractor.getSampleTrackIndex()
                        // 获取时间
                        long presentationTimeUs = extractor.getSampleTime();
                        // 提交解码Index
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0 /*flags*/);
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                // 解码输出完成状态
                int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    throw new RuntimeException(
                            "unexpected result from decoder.dequeueOutputBuffer: " +
                                    decoderStatus);
                } else { // decoderStatus >= 0
                    if (firstInputTimeNsec != 0) {
                        // 输出第一帧从 提交 到 解码完成 所耗的时间
                        long nowNsec = System.nanoTime();
                        Log.d(TAG, "startup lag " + ((nowNsec - firstInputTimeNsec) / 1000000.0) + " ms");
                        firstInputTimeNsec = 0;
                    }
                    boolean doLoop = false;
                    Log.d(TAG, "decoderStatus: " + decoderStatus + " (size=" + mBufferInfo.size + ")");
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        if (mLoop) {
                            doLoop = true;
                        } else {
                            outputDone = true;
                        }
                    }

                    boolean doRender = (mBufferInfo.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  We can't control when it
                    // appears on-screen, but we can manage the pace at which we release
                    // the buffers.
                    if (doRender && frameCallback != null) {
                        frameCallback.preRender(mBufferInfo.presentationTimeUs);
                    }
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender && frameCallback != null) {
                        frameCallback.postRender();
                    }

                    if (doLoop) {
                        Log.d(TAG, "Reached EOS, looping");
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        inputDone = false;
                        decoder.flush();    // reset decoder state
                        frameCallback.loopReset();
                    }
                }
            }
        }
    }

    /**
     * Thread helper for video playback.
     * <p>
     * The PlayerFeedback callbacks will execute on the thread that creates the object,
     * assuming that thread has a looper.  Otherwise, they will execute on the main looper.
     */
    public static class PlayTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;

        private MoviePlayer mPlayer;
        private PlayerFeedback mFeedback;
        private boolean mDoLoop;
        private Thread mThread;
        private LocalHandler mLocalHandler;

        private final Object mStopLock = new Object();
        private boolean mStopped = false;

        /**
         * Prepares new PlayTask.
         *
         * @param player   The player object, configured with control and output.
         * @param feedback UI feedback object.
         */
        public PlayTask(MoviePlayer player, PlayerFeedback feedback) {
            mPlayer = player;
            mFeedback = feedback;

            mLocalHandler = new LocalHandler();
        }

        /**
         * Sets the loop mode.  If true, playback will loop forever.
         */
        public void setLoopMode(boolean loopMode) {
            mDoLoop = loopMode;
        }

        /**
         * Creates a new thread, and starts execution of the player.
         */
        public void execute() {
            mPlayer.setLoopMode(mDoLoop);
            mThread = new Thread(this, "Movie Player");
            mThread.start();
        }

        /**
         * Requests that the player stop.
         * <p>
         * Called from arbitrary thread.
         */
        public void requestStop() {
            mPlayer.requestStop();
        }

        /**
         * Wait for the player to stop.
         * <p>
         * Called from any thread other than the PlayTask thread.
         */
        public void waitForStop() {
            synchronized (mStopLock) {
                while (!mStopped) {
                    try {
                        mStopLock.wait();
                    } catch (InterruptedException ie) {
                        // discard
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                mPlayer.play();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                // tell anybody waiting on us that we're done
                synchronized (mStopLock) {
                    mStopped = true;
                    mStopLock.notifyAll();
                }

                // Send message through Handler so it runs on the right thread.
                mLocalHandler.sendMessage(
                        mLocalHandler.obtainMessage(MSG_PLAY_STOPPED, mFeedback));
            }
        }

        private static class LocalHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case MSG_PLAY_STOPPED:
                        PlayerFeedback fb = (PlayerFeedback) msg.obj;
                        fb.playbackStopped();
                        break;
                    default:
                        throw new RuntimeException("Unknown msg " + what);
                }
            }
        }
    }
}
