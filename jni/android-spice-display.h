/*
   Copyright (C) 2014 Carnegie Mellon University
   Copyright (C) 2013 Iordan Iordanov
   Copyright (C) 2010 Red Hat, Inc.

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, see <http://www.gnu.org/licenses/>.
*/
#ifndef __ANDROID_SPICE_DISPLAY_H__
#define __ANDROID_SPICE_DISPLAY_H__

#include <spice-client.h>
#include <spice-util.h>
#include "android-spice.h"

G_BEGIN_DECLS

#define SPICE_TYPE_DISPLAY            (spice_display_get_type())
#define SPICE_DISPLAY(obj)            (G_TYPE_CHECK_INSTANCE_CAST((obj), SPICE_TYPE_DISPLAY, SpiceDisplay))
#define SPICE_DISPLAY_CLASS(klass)    (G_TYPE_CHECK_CLASS_CAST((klass), SPICE_TYPE_DISPLAY, SpiceDisplayClass))
#define SPICE_IS_DISPLAY(obj)         (G_TYPE_CHECK_INSTANCE_TYPE((obj), SPICE_TYPE_DISPLAY))
#define SPICE_IS_DISPLAY_CLASS(klass) (G_TYPE_CHECK_CLASS_TYPE((klass), SPICE_TYPE_DISPLAY))
#define SPICE_DISPLAY_GET_CLASS(obj)  (G_TYPE_INSTANCE_GET_CLASS((obj), SPICE_TYPE_DISPLAY, SpiceDisplayClass))

typedef struct _SpiceDisplay SpiceDisplay;
typedef struct _SpiceDisplayClass SpiceDisplayClass;
typedef struct _SpiceDisplayPrivate SpiceDisplayPrivate;

struct _SpiceDisplay {
    SpiceChannel parent;
    SpiceDisplayPrivate *priv;
    /* Do not add fields to this struct */
};

struct _SpiceDisplayClass {
    SpiceChannelClass parent_class;

    /* signals */

    /*< private >*/
};

GType spice_display_get_type(void);
SpiceDisplay* spice_display_new(struct spice_context *ctx, int id);
void send_key(SpiceDisplay *display, int scancode, int down);
gint get_display_id(SpiceDisplay *display);

G_END_DECLS

#endif /* __ANDROID_SPICE_DISPLAY_H__ */