/*
 * Copyright (C) 2014 Carnegie Mellon University
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package org.olivearchive.vmnetx.android;

import android.os.Bundle;
import android.util.Log;

class ViewerConnectionProcessor implements ConnectionProcessor, ProtocolEndpoint.MessageProcessor {
    private static final String TAG = "ViewerConnectionProcessor";

    private static final int CONNECT_CONTINUE = 0;
    private static final int CONNECT_DONE = 1;
    private static final int CONNECT_FAILED = 2;

    private native void Connect(String host, String port);
    private native void SendMessage(int fd, byte[] data);

    private final String host;
    private final String port;
    private final String token;
    private final ClientProtocolEndpoint endpoint;
    private int state = CONNECT_CONTINUE;
    private int fd = -1;

    ViewerConnectionProcessor(String host, String port, String token) {
        this.host = host;
        this.port = port;
        this.token = token;
        endpoint = new ClientProtocolEndpoint(this, this);
    }

    @Override
    public void setEndpoint(ProtocolEndpoint endpoint) {}

    @Override
    public void send(byte[] data) {
        if (fd != -1 && state == CONNECT_CONTINUE)
            SendMessage(fd, data);
        else
            throw new IllegalStateException("Connection not available for sending");
    }

    public int connect() throws ProtocolException {
        if (state != CONNECT_CONTINUE)
            throw new IllegalStateException("Cannot reuse ViewerConnectionProcessor objects");
        Log.d(TAG, "Connect " + host + ":" + port);
        Connect(host, port);
        if (state != CONNECT_DONE)
            throw new ProtocolException("Could not establish connection");
        return fd;
    }

    private void OnConnect(int fd) {
        // callback from JNI
        this.fd = fd;
        endpoint.connected();
    }

    private int OnReceiveMessage(byte[] data) {
        // callback from JNI
        try {
            endpoint.dispatch(data);
        } catch (ProtocolException e) {
            Log.e(TAG, "Dispatch error", e);
            transition(CONNECT_FAILED);
        }
        return state;
    }

    private void transition(int state) {
        if (this.state == CONNECT_CONTINUE)
            this.state = state;
    }

    @Override
    public void processMessage(int what, Bundle args) {
        // callback from ProtocolEndpoint
        switch (what) {
        case Constants.PROTOCOL_CONNECTED:
            endpoint.sendAuthenticate(token);
            break;

        case Constants.CLIENT_PROTOCOL_AUTH_OK:
            int vmState = args.getInt(Constants.ARG_VM_STATE);
            if (vmState != Constants.VM_STATE_RUNNING) {
                Log.e(TAG, "Server in unexpected state " + Integer.toString(vmState));
                transition(CONNECT_FAILED);
            } else {
                endpoint.sendAttachViewer();
            }
            break;

        case Constants.CLIENT_PROTOCOL_AUTH_FAILED:
            Log.e(TAG, "Viewer auth failed: " + args.getString(Constants.ARG_ERROR));
            transition(CONNECT_FAILED);
            break;

        case Constants.CLIENT_PROTOCOL_ATTACHING_VIEWER:
            transition(CONNECT_DONE);
            break;

        case Constants.PROTOCOL_ERROR:
            Log.e(TAG, "Protocol error: " + args.getString(Constants.ARG_ERROR));
            transition(CONNECT_FAILED);
            break;

        default:
            Log.e(TAG, "Ignored message " + Integer.toString(what));
            break;
        }
    }
}