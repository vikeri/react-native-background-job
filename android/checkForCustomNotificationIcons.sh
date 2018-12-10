#!/bin/bash

BUILD_DIR=$1
ICON_NAME="pilloxa_custom_notification.png"

# Creating symlinks for the notification icons
#
ln -s ../../../../../../../android/app/src/main/res/drawable-mdpi/ICON_NAME \
"${BUILD_DIR}/../src/main/res/drawable-mdpi/ICON_NAME"

ln -s ../../../../../../../android/app/src/main/res/drawable-hdpi/ICON_NAME \
"${BUILD_DIR}/../src/main/res/drawable-hdpi/ICON_NAME"

ln -s ../../../../../../../android/app/src/main/res/drawable-xhdpi/ICON_NAME \
"${BUILD_DIR}/../src/main/res/drawable-xhdpi/ICON_NAME"

ln -s ../../../../../../../android/app/src/main/res/drawable-xxhdpi/ICON_NAME \
"${BUILD_DIR}/../src/main/res/drawable-xxhdpi/ICON_NAME"

# If the user has not any custom notification icons we remove the dangling symlinks
# or else the build will fail
#
find -L "${BUILD_DIR}/../src/main/res" -type l -delete
