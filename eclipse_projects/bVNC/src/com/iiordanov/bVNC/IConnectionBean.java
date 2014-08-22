/**
 * Copyright (C) 2012 Iordan Iordanov
 * Copyright (C) 2009 Michael A. MacDonald
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

package com.iiordanov.bVNC;

import com.antlersoft.android.db.*;

/**
 * @author Iordan Iordanov
 * @author Michael A. MacDonald
 *
 */
@TableInterface(ImplementingClassName="AbstractConnectionBean",TableName="CONNECTION_BEAN")
interface IConnectionBean {
    @FieldAccessor
    long get_Id();
    @FieldAccessor
    String getNickname();
    @FieldAccessor
    String getAddress();
    @FieldAccessor
    int getPort();
    @FieldAccessor
    String getPassword();
    @FieldAccessor
    int getExtraKeysToggleType();
    /**
     * Records bitmap data implementation selection.  0 for auto, 1 for force full bitmap, 2 for force tiled
     * <p>
     * For historical reasons, this is named as if it were just a boolean selection for auto and force full.
     * @return 0 for auto, 1 for force full bitmap, 2 for forced tiled
     */
    @FieldAccessor
    long getForceFull();
    @FieldAccessor
    String getRepeaterId();
    @FieldAccessor
    String getInputMode();
    @FieldAccessor(Name="SCALEMODE")
    String getScaleModeAsString();
    @FieldAccessor
    boolean getUseDpadAsArrows();
    @FieldAccessor
    boolean getRotateDpad();
    @FieldAccessor
    boolean getUsePortrait();
    @FieldAccessor
    boolean getUseLocalCursor();
    @FieldAccessor
    boolean getKeepPassword();
    @FieldAccessor
    boolean getFollowMouse();
    @FieldAccessor
    boolean getUseRepeater();
    @FieldAccessor(Name="LAST_META_KEY_ID")
    long getLastMetaKeyId();
    @FieldAccessor(DefaultValue="false")
    boolean getFollowPan();
    @FieldAccessor
    String getUserName();
    @FieldAccessor
    String getRdpDomain();
    @FieldAccessor(DefaultValue="true")
    boolean getShowZoomButtons();
    @FieldAccessor(Name="DOUBLE_TAP_ACTION")
    String getDoubleTapActionAsString();
    @FieldAccessor
    int getRdpColor();
    @FieldAccessor
    boolean getRemoteFx();
    @FieldAccessor
    boolean getDesktopBackground();
    @FieldAccessor
    boolean getFontSmoothing();
    @FieldAccessor
    boolean getDesktopComposition();
    @FieldAccessor
    boolean getWindowContents();
    @FieldAccessor
    boolean getMenuAnimation();
    @FieldAccessor
    boolean getVisualStyles();
    @FieldAccessor
    boolean getRedirectSdCard();
    @FieldAccessor
    boolean getConsoleMode();
    @FieldAccessor
    boolean getViewOnly();
}
