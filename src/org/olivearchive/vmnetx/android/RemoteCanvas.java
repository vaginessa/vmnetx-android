/**
 * Copyright (C) 2013-2014 Carnegie Mellon University
 * Copyright (C) 2012 Iordan Iordanov
 * Copyright (C) 2009-2010 Michael A. MacDonald
 * Copyright (C) 2004 Horizon Wimba.  All Rights Reserved.
 * Copyright (C) 2001-2003 HorizonLive.com, Inc.  All Rights Reserved.
 * Copyright (C) 2001,2002 Constantin Kaplinsky.  All Rights Reserved.
 * Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
 * Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
 * 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

//
// RemoteCanvas is a subclass of android.widget.ImageView which draws a SPICE
// desktop on it.
//

package org.olivearchive.vmnetx.android;

import java.io.IOException;
import java.text.NumberFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Rect;

import org.olivearchive.vmnetx.android.input.RemoteKeyboard;
import org.olivearchive.vmnetx.android.input.RemotePointer;
import org.olivearchive.vmnetx.android.protocol.ClientProtocolEndpoint;
import org.olivearchive.vmnetx.android.protocol.ControlConnectionProcessor;

public class RemoteCanvas extends ImageView {
    private final static String TAG = "RemoteCanvas";
    private final static Bitmap.Config cfg = Bitmap.Config.ARGB_8888;
    
    // Connection parameters
    private ConnectionBean connection;

    // VMNetX control connection
    private ControlConnectionProcessor controlConn;
    private ClientProtocolEndpoint endpoint;
    private String vmName = null;
    private int vmState = Constants.VM_STATE_UNKNOWN;
    
    // SPICE protocol connection
    private SpiceCommunicator spice = null;
    
    private boolean maintainConnection = true;
    
    // The remote pointer and keyboard
    private RemotePointer pointer;
    private RemoteKeyboard keyboard;
    
    // Internal bitmap data
    private Bitmap bitmap;
    private final BitmapDrawable drawable = new BitmapDrawable(this);

    // Mouse cursor
    private int[] cursor;
    private boolean cursorVisible;
    private int cursorWidth, cursorHeight, hotX, hotY;
    
    // Progress dialog shown at connection time.
    private ProgressDialog pd;
    
    private Runnable updateActivity;
    
    // Bitmap scaling
    private final Matrix matrix = new Matrix();
    private float scaling = 1;
    private float minimumScale;

    /*
     * Position of the top left portion of the <i>visible</i> part of the screen, in
     * full-frame coordinates
     */
    private int absoluteXPosition = 0;
    private int absoluteYPosition = 0;
    private int prevAbsoluteXPosition = -1;
    private int prevAbsoluteYPosition = -1;
    
    /*
     * How much to shift coordinates over when converting from full to view coordinates.
     */
    private float shiftX = 0, shiftY = 0;

    private final float displayDensity;
    
    private boolean spiceUpdateReceived = false;
    
    /**
     * Constructor used by the inflation apparatus
     * 
     * @param context
     */
    public RemoteCanvas(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageDrawable(drawable);
        setScaleType(ImageView.ScaleType.MATRIX);
        
        final Display display = ((Activity)context).getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        displayDensity = metrics.density;
    }
    
    
    /**
     * Create a view showing a remote desktop connection
     * @param context Containing context (activity)
     * @param bean Connection settings
     * @param updateActivity Callback to run on UI thread after connection is set up
     */
    void initializeCanvas(ConnectionBean bean, final Runnable updateActivity) {
        this.updateActivity = updateActivity;
        connection = bean;

        // Startup the connection thread with a progress dialog
        pd = new ProgressDialog(getContext());
        pd.setTitle(getContext().getString(R.string.info_progress_dialog_title));
        pd.setMessage(getContext().getString(R.string.info_progress_dialog_message));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgressNumberFormat(null);
        pd.setProgressPercentFormat(null);
        pd.setMax(10000);
        pd.setIndeterminate(true);
        // Make this dialog cancellable only upon hitting the Back button and not touching outside.
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeConnection();
                handler.post(new Runnable() {
                    public void run() {
                        Utils.showFatalErrorMessage(getContext(), getContext().getString(R.string.info_progress_dialog_aborted));
                    }
                });
            }
        });
        pd.show();
        
        startControlConnection();
    }


    private void startControlConnection() {
        try {
            controlConn = new ControlConnectionProcessor(connection.getAddress(), connection.getPort());
            endpoint = new ClientProtocolEndpoint(controlConn, handler);
            new Thread(controlConn).start();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't create ControlConnectionProcessor", e);
            showFatalMessageAndQuit(getContext().getString(R.string.error_connection_failed));
        }
    }


    /**
     * Starts a SPICE connection using libspice.
     */
    private void startSpiceConnection() {
        new Thread() {
            public void run() {
                try {
                    spice = new SpiceCommunicator (getContext(), RemoteCanvas.this, handler, connection);
                    pointer = new RemotePointer (spice, RemoteCanvas.this);
                    keyboard = new RemoteKeyboard (spice, handler);
                    spice.connect();
                } catch (Throwable e) {
                    if (maintainConnection) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                        // Ensure we dismiss the progress dialog before we finish
                        if (pd.isShowing())
                            pd.dismiss();
                        
                        if (e instanceof OutOfMemoryError) {
                            showFatalMessageAndQuit (getContext().getString(R.string.error_out_of_memory));
                        } else {
                            String error = getContext().getString(R.string.error_connection_failed);
                            if (e.getMessage() != null) {
                                error = error + "<br>" + e.getLocalizedMessage();
                            }
                            showFatalMessageAndQuit(error);
                        }
                    }
                }
            }
        }.start();
    }
    
    
    /**
     * Retreives the requested remote width.
     */
    private int getRemoteWidth (int viewWidth, int viewHeight) {
        int remoteWidth = Math.max(viewWidth, viewHeight);
        // We make the resolution even if it is odd.
        if (remoteWidth % 2 == 1) remoteWidth--;
        return remoteWidth;
    }
    
    
    /**
     * Retreives the requested remote height.
     */
    private int getRemoteHeight (int viewWidth, int viewHeight) {
        int remoteHeight = Math.min(viewWidth, viewHeight);
        // We make the resolution even if it is odd.
        if (remoteHeight % 2 == 1) remoteHeight--;
        return remoteHeight;
    }
    
    
    /**
     * Closes the connection and shows a fatal message which ends the activity.
     * @param error
     */
    void showFatalMessageAndQuit (final String error) {
        closeConnection();
        handler.post(new Runnable() {
            public void run() {
                Utils.showFatalErrorMessage(getContext(), error);
            }
        });
    }
    
    
    /**
     * Displays a short toast message on the screen.
     * @param message
     */
    public void displayShortToastMessage (final CharSequence message) {
        screenMessage = message;
        handler.removeCallbacks(showMessage);
        handler.post(showMessage);
    }
    
    
    /**
     * Displays a short toast message on the screen.
     * @param messageID
     */
    public void displayShortToastMessage (final int messageID) {
        screenMessage = getResources().getText(messageID);
        handler.removeCallbacks(showMessage);
        handler.post(showMessage);
    }
    
    public void restartVM() {
        endpoint.sendStopVM();
        // VM_STATE_STOPPED handler will restart it
    }
    
    /**
     * Method that disconnects from the remote server.
     */
    public void closeConnection() {
        maintainConnection = false;
        
        if (keyboard != null) {
            // Tell the server to release any meta keys.
            keyboard.clearOnScreenModifiers();
            keyboard.processLocalKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, 0));
        }
        // Close the SPICE connection.
        if (spice != null)
            spice.disconnect();
        // Close the control connection.
        if (controlConn != null) {
            try {
                wantVMState(Constants.VM_STATE_DESTROYED);
            } catch (IllegalStateException e) {}
            controlConn.close();
        }

        onDestroy();
    }
    
    
    /**
     * Cleans up resources after a disconnection.
     */
    private void onDestroy() {
        Log.v(TAG, "Cleaning up resources");
        
        handler.removeCallbacksAndMessages(null);

        updateActivity   = null;
        connection       = null;
        screenMessage    = null;
        spice            = null;
        endpoint         = null;
        controlConn      = null;
    }
    
    /**
     * Update scaling from framebuffer size
     */
    private void updateScale() {
        // Compute the X and Y offset for converting coordinates from
        // full-frame coordinates to view coordinates
        shiftX = (spice.framebufferWidth()  - getWidth())  / 2;
        shiftY = (spice.framebufferHeight() - getHeight()) / 2;

        boolean zoomedOut = (scaling <= minimumScale);
        minimumScale = computeMinimumScale();
        if (zoomedOut) {
            // We were fully zoomed out; stay that way
            setScale(minimumScale);
        } else {
            setScale(Math.max(scaling, minimumScale));
        }
    }

    /**
     * Change the scaling and focus dynamically, as from a detected scale gesture
     * @param scaleFactor Factor by which to adjust scaling
     * @param fx Focus X of center of scale change
     * @param fy Focus Y of center of scale change
     */
    public void adjustScale(float scaleFactor, float fx, float fy) {
        float oldScale;
        float newScale = scaleFactor * scaling;
        if (scaleFactor < 1) {
            if (newScale < minimumScale)
                newScale = minimumScale;
        } else {
            if (newScale > 4)
                newScale = 4;
        }

        // ax is the absolute x of the focus
        int xPan = getAbsoluteX();
        float ax = (fx / scaling) + xPan;
        float newXPan = (scaling * xPan - scaling * ax + newScale * ax) /
                newScale;
        int yPan = getAbsoluteY();
        float ay = (fy / scaling) + yPan;
        float newYPan = (scaling * yPan - scaling * ay + newScale * ay) /
                newScale;

        // Here we do snapping to 1:1. If we are approaching scale = 1, we
        // snap to it.
        oldScale = scaling;
        if ((newScale > 0.90f && newScale < 1.00f) ||
            (newScale > 1.00f && newScale < 1.10f)) {
            newScale = 1.f;
            // Only if oldScale is outside the snap region, do we inform the
            // user.
            if (oldScale < 0.90f || oldScale > 1.10f)
                displayShortToastMessage(R.string.snap_one_to_one);
        }

        setScale(newScale);

        // Only if we have actually scaled do we pan.
        if (oldScale != newScale) {
            pan((int) (newXPan - xPan), (int) (newYPan - yPan));
        }
    }
    
    private void setScale(float scale) {
        scaling = scale;
        matrix.reset();
        matrix.preTranslate((int) -shiftX, (int) -shiftY);
        matrix.postScale(scaling, scaling);
        setImageMatrix(matrix);
        scrollToAbsolute(true);
    }

    /**
     * Change to Canvas's scroll position to match the absoluteXPosition
     */
    private void scrollToAbsolute(boolean force) {
        // Clamp to bounds of desktop image
        absoluteXPosition = Math.max(absoluteXPosition, 0);
        absoluteYPosition = Math.max(absoluteYPosition, 0);
        absoluteXPosition = Math.min(absoluteXPosition,
                getImageWidth() - getVisibleWidth());
        absoluteYPosition = Math.min(absoluteYPosition,
                getImageHeight() - getVisibleHeight());
        // If image is smaller than the canvas, center the image
        if (absoluteXPosition < 0)
            absoluteXPosition /= 2;
        if (absoluteYPosition < 0)
            absoluteYPosition /= 2;

        if (force ||
                absoluteXPosition != prevAbsoluteXPosition ||
                absoluteYPosition != prevAbsoluteYPosition) {
            scrollTo((int)((absoluteXPosition - shiftX) * scaling),
                     (int)((absoluteYPosition - shiftY) * scaling));
            prevAbsoluteXPosition = absoluteXPosition;
            prevAbsoluteYPosition = absoluteYPosition;
        }
    }
    
    
    /**
     * Make sure mouse is visible on displayable part of screen
     */
    public void panToMouse() {
        if (spice == null)
            return;
        
        int x = pointer.getX();
        int y = pointer.getY();
        int w = getVisibleWidth();
        int h = getVisibleHeight();
        int iw = getImageWidth();
        int ih = getImageHeight();
        int wthresh = 30;
        int hthresh = 30;
        
        // Don't pan in a certain direction if dimension scaled is already less
        // than the dimension of the visible part of the screen.
        if (spice.framebufferWidth() > getVisibleWidth()) {
            if (x - absoluteXPosition >= w - wthresh) {
                absoluteXPosition = x - (w - wthresh);
                if (absoluteXPosition + w > iw)
                    absoluteXPosition = iw - w;
            } else if (x < absoluteXPosition + wthresh) {
                absoluteXPosition = x - wthresh;
                if (absoluteXPosition < 0)
                    absoluteXPosition = 0;
            }
        }
        if (spice.framebufferHeight() > getVisibleHeight()) {
            if (y - absoluteYPosition >= h - hthresh) {
                absoluteYPosition = y - (h - hthresh);
                if (absoluteYPosition + h > ih)
                    absoluteYPosition = ih - h;
            } else if (y < absoluteYPosition + hthresh) {
                absoluteYPosition = y - hthresh;
                if (absoluteYPosition < 0)
                    absoluteYPosition = 0;
            }
        }
        
        scrollToAbsolute(false);
    }
    
    /**
     * Pan by a number of pixels (relative pan)
     * @param dX
     * @param dY
     */
    public void pan(int dX, int dY) {
        absoluteXPosition += (double) dX / scaling;
        absoluteYPosition += (double) dY / scaling;
        scrollToAbsolute(false);
    }


    /**
     * This runnable displays a message on the screen.
     */
    CharSequence screenMessage;
    private Runnable showMessage = new Runnable() {
            public void run() { Toast.makeText( getContext(), screenMessage, Toast.LENGTH_SHORT).show(); }
    };
    
    
    /**
     * Causes a redraw of the bitmap to happen at the indicated coordinates.
     */
    private void reDraw(int x, int y, int w, int h) {
        float shiftedX = x-shiftX;
        float shiftedY = y-shiftY;
        // Make the box slightly larger to avoid artifacts due to truncation errors.
        postInvalidate(
            (int) ((shiftedX - 1) * scaling),
            (int) ((shiftedY - 1) * scaling),
            (int) ((shiftedX + w + 1) * scaling),
            (int) ((shiftedY + h + 1) * scaling));
    }
    
    
    /**
     * Invalidates (to redraw) the location of the remote pointer.
     */
    public void invalidateMousePosition() {
        drawable.moveCursor(pointer.getX(), pointer.getY());
        Rect r = drawable.getCursorRect();
        if (r != null)
            reDraw(r.left, r.top, r.width(), r.height());
    }
    
    /**
     * Initializes the data structure which holds the remote pointer data.
     */
    private void setDefaultSoftCursor() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int [] tempPixels = new int[w*h];
        bm.getPixels(tempPixels, 0, w, 0, 0, w, h);
        // Set softCursor to whatever the resource is.
        drawable.setSoftCursor(tempPixels, w, h, 0, 0);
        bm.recycle();
    }
    
    private final Runnable configureCursor = new Runnable() {
        public void run() {
            if (spice == null)
                return;

            Rect prevR = drawable.getCursorRect();
            if (prevR != null)
                prevR = new Rect(prevR);
            synchronized (this) {
                if (!cursorVisible)
                    drawable.clearSoftCursor();
                else if (cursor != null)
                    drawable.setSoftCursor(cursor, cursorWidth, cursorHeight,
                            hotX, hotY);
                else
                    setDefaultSoftCursor();
            }
            // Redraw the cursor.
            Rect r = drawable.getCursorRect();
            if (r != null)
                reDraw(r.left, r.top, r.width(), r.height());
            if (prevR != null)
                reDraw(prevR.left, prevR.top, prevR.width(), prevR.height());
        }
    };

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_NULL;
        /* TODO: If people complain about kbd not working, this is a possible workaround to
         * test and add an option for.
        // Workaround for IME's that don't support InputType.TYPE_NULL.
        outAttrs.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_FULLSCREEN;
        */
        return new BaseInputConnection(this, false);
    }

    public void setFilteringEnabled(boolean enabled) {
        drawable.setFilteringEnabled(enabled);
    }
    
    Bitmap getBitmap() {
        return bitmap;
    }

    public RemotePointer getPointer() {
        return pointer;
    }
    
    public RemoteKeyboard getKeyboard() {
        return keyboard;
    }
    
    public float getScale() {
        return scaling;
    }
    
    public int getVisibleWidth() {
        return (int)((double) getWidth() / scaling + 0.5);
    }
    
    public int getVisibleHeight() {
        return (int)((double) getHeight() / scaling + 0.5);
    }
    
    public int getImageWidth() {
        if (bitmap != null)
            return bitmap.getWidth();
        else
            return 1;
    }
    
    public int getImageHeight() {
        if (bitmap != null)
            return bitmap.getHeight();
        else
            return 1;
    }
    
    /**
     * @return The scale at which the bitmap would be smaller than the screen
     */
    private float computeMinimumScale() {
        return Math.min((float) getWidth() / getImageWidth(),
                (float) getHeight() / getImageHeight());
    }
    
    public float getDisplayDensity() {
        return displayDensity;
    }
    
    public int getAbsoluteX () {
        return absoluteXPosition;
    }
    
    public int getAbsoluteY () {
        return absoluteYPosition;
    }
    
    public boolean getAbsoluteMouse() {
        return spice.getAbsoluteMouse();
    }

    public String getVMName() {
        return vmName;
    }

    /**
     * Used to wait until getWidth and getHeight return sane values.
     */
    private void waitUntilInflated() {
        synchronized (this) {
            while (getWidth() == 0 || getHeight() == 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }
    
    /**
     * Used to detect when the view is inflated to a sane size other than 0x0.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0) {
            synchronized (this) {
                this.notify();
            }
            if (spice != null) {
                // Ensure the view position is sane
                updateScale();
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    //  Through the functions implemented below libspice communicates remote
    //  desktop size and updates.
    //////////////////////////////////////////////////////////////////////////////////
    
    void OnSettingsChanged(final int width, final int height, int bpp) {
        android.util.Log.d(TAG, "onSettingsChanged called, wxh: " + width + "x" + height);
        
        // We need to initialize the communicator and remote keyboard and mouse now.
        waitUntilInflated();
        int remoteWidth  = getRemoteWidth(getWidth(), getHeight());
        int remoteHeight = getRemoteHeight(getWidth(), getHeight());
        if (width != remoteWidth || height != remoteHeight) {
            android.util.Log.d(TAG, "Requesting new res: " + remoteWidth + "x" + remoteHeight);
            spice.requestResolution(remoteWidth, remoteHeight);
        }

        // Recreate bitmap.
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    try {
                        bitmap.reconfigure(width, height, cfg);
                    } catch (IllegalArgumentException e) {
                        bitmap = null;
                    }
                }
                if (bitmap == null) {
                    try {
                        bitmap = Bitmap.createBitmap(width, height, cfg);
                        bitmap.setHasAlpha(false);
                    } catch (Throwable e) {
                        showFatalMessageAndQuit(getContext().getString(R.string.error_out_of_memory));
                    }
                }
                updateScale();
                spice.redraw();
            }
        });
        
        // Re-initialize cursor.
        handler.post(configureCursor);

        // Update activity state.
        handler.post(updateActivity);
        
        // Notify that we have a connection.
        spiceUpdateReceived = true;
        handler.sendEmptyMessage(Constants.SPICE_CONNECT_SUCCESS);
    }

    void OnGraphicsUpdate(int x, int y, int width, int height) {
        //android.util.Log.d(TAG, "OnGraphicsUpdate called: " + x +", " + y + " + " + width + "x" + height );

        if (bitmap == null)
            return;

        synchronized (bitmap) {
            spice.updateBitmap(bitmap, x, y, width, height);
        }
        
        reDraw(x, y, width, height);
    }

    void OnMouseMode() {
        handler.post(updateActivity);
    }

    void OnCursorConfig(boolean shown, int[] bitmap, int w, int h,
            int hx, int hy) {
        synchronized (this) {
            cursorVisible = shown;
            cursor = bitmap;
            cursorWidth = w;
            cursorHeight = h;
            hotX = hx;
            hotY = hy;
        }
        handler.post(configureCursor);
    }

    private void wantVMState(int wanted) {
        // Only handle transitions for which we can usefully issue a
        // command.  Other transitions will be handled after a
        // subsequent event.
        switch (wanted) {
        case Constants.VM_STATE_RUNNING:
            if (vmState == Constants.VM_STATE_STOPPED) {
                endpoint.sendStartVM();
                vmState = Constants.VM_STATE_STARTING;
            }
            break;

        case Constants.VM_STATE_STOPPED:
            if (vmState == Constants.VM_STATE_STARTING || vmState == Constants.VM_STATE_RUNNING) {
                endpoint.sendStopVM();
                vmState = Constants.VM_STATE_STOPPING;
            }
            break;

        case Constants.VM_STATE_DESTROYED:
            if (vmState != Constants.VM_STATE_DESTROYED) {
                endpoint.sendDestroyVM();
                vmState = Constants.VM_STATE_DESTROYED;
            }
            break;
        }
    }


    private class PingerRunnable implements Runnable {
        private static final int INTERVAL = 5000;
        private static final int COUNT = 5;

        private int outstanding = 0;

        public void start() {
            stop();
            schedule();
        }

        public void stop() {
            handler.removeCallbacks(pinger);
        }

        public void pong() {
            outstanding = 0;
        }

        private void schedule() {
            handler.postDelayed(pinger, INTERVAL);
        }

        @Override
        public void run() {
            if (outstanding < COUNT) {
                endpoint.sendPing();
                outstanding += 1;
                schedule();
            } else {
                Log.w(TAG, "Control connection timed out");
                controlConn.close();
            }
        }
    };
    private final PingerRunnable pinger = new PingerRunnable();


    /** 
     * Handler for connection events.
     */
    private final Handler handler = new Handler() {
        private void showProtocolErrorAndQuit(String error) {
            showFatalMessageAndQuit(getContext().getString(R.string.error_protocol) + " " + error);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle args = msg.getData();
            String error;

            switch (msg.what) {
            case Constants.SPICE_CONNECT_SUCCESS:
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                break;

            case Constants.SPICE_CONNECT_FAILURE:
                // Data connection failed; retry
                if (maintainConnection) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startSpiceConnection();
                        }
                    }, 1000);
                }
                break;

            case Constants.PROTOCOL_CONNECTED:
                Log.d(TAG, "connected");
                endpoint.sendAuthenticate(connection.getToken());
                break;

            case Constants.PROTOCOL_ERROR:
                error = args.getString(Constants.ARG_ERROR);
                Log.d(TAG, "error " + error);
                showProtocolErrorAndQuit(error);
                break;

            case Constants.PROTOCOL_DISCONNECTED:
                Log.d(TAG, "disconnected");
                vmState = Constants.VM_STATE_UNKNOWN;
                pinger.stop();
                if (maintainConnection) {
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                    if (!spiceUpdateReceived) {
                        showFatalMessageAndQuit(getContext().getString(R.string.error_connection_failed));
                    } else {
                        showFatalMessageAndQuit(getContext().getString(R.string.error_connection_interrupted));
                    }
                }
                break;

            case Constants.CLIENT_PROTOCOL_AUTH_OK:
                vmName = args.getString(Constants.ARG_VM_NAME);
                vmState = args.getInt(Constants.ARG_VM_STATE);
                int maxMouseRate = args.getInt(Constants.ARG_MAX_MOUSE_RATE);
                Log.d(TAG, "auth ok " + vmName + " " + Integer.toString(vmState) + " " + Integer.toString(maxMouseRate));

                // Start pinging
                pinger.start();

                // Update window title
                post(updateActivity);

                // If the VM is in a stable state, synthesize a state transition.
                switch (vmState) {
                case Constants.VM_STATE_RUNNING:
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.ARG_CHECK_DISPLAY, false);
                    Message message = handler.obtainMessage(Constants.CLIENT_PROTOCOL_VM_STARTED);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    break;
                case Constants.VM_STATE_STOPPED:
                    handler.sendEmptyMessage(Constants.CLIENT_PROTOCOL_VM_STOPPED);
                    break;
                }
                break;

            case Constants.CLIENT_PROTOCOL_AUTH_FAILED:
                error = args.getString(Constants.ARG_ERROR);
                Log.d(TAG, "auth failed " + error);
                showProtocolErrorAndQuit(error);
                break;

            case Constants.CLIENT_PROTOCOL_STARTUP_PROGRESS:
                double progress = args.getDouble(Constants.ARG_PROGRESS);
                if (pd.isIndeterminate() && progress > 0) {
                    pd.setIndeterminate(false);
                    pd.setProgressPercentFormat(NumberFormat.getPercentInstance());
                }
                pd.setProgress((int) (progress * pd.getMax()));
                break;

            case Constants.CLIENT_PROTOCOL_STARTUP_REJECTED_MEMORY:
                Log.d(TAG, "rejected memory");
                break;

            case Constants.CLIENT_PROTOCOL_STARTUP_FAILED:
                error = args.getString(Constants.ARG_ERROR);
                Log.d(TAG, "startup failed " + error);
                showProtocolErrorAndQuit(error);
                break;

            case Constants.CLIENT_PROTOCOL_VM_STARTED:
                boolean checkDisplay = args.getBoolean(Constants.ARG_CHECK_DISPLAY);
                Log.d(TAG, "VM started, check: " + Boolean.toString(checkDisplay));
                vmState = Constants.VM_STATE_RUNNING;
                if (spice == null)
                    startSpiceConnection();
                break;

            case Constants.CLIENT_PROTOCOL_VM_STOPPED:
                Log.d(TAG, "VM stopped");
                vmState = Constants.VM_STATE_STOPPED;
                wantVMState(Constants.VM_STATE_RUNNING);
                break;

            case Constants.CLIENT_PROTOCOL_VM_DESTROYED:
                Log.d(TAG, "VM destroyed");
                vmState = Constants.VM_STATE_DESTROYED;
                if (maintainConnection) {
                    showFatalMessageAndQuit(getContext().getString(R.string.error_vm_terminated));
                }
                break;

            case Constants.CLIENT_PROTOCOL_PONG:
                //Log.d(TAG, "pong!");
                pinger.pong();
                break;

            default:
                Log.w(TAG, "Handler received unknown message " + Integer.toString(msg.what));
                break;
            }
        }
    };
}
