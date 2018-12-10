#!/bin/bash

BUILD_DIR="${1}/../src/main/res"
ROOT_DIR="${2}/app/src/main/res"
ICON_NAME="pilloxa_custom_notification.png"

# Creating symlinks for the notification icons
#
ln -s "${ROOT_DIR}/drawable-mdpi/${ICON_NAME}" \
"${BUILD_DIR}/drawable-mdpi/${ICON_NAME}"

ln -s "${ROOT_DIR}/drawable-hdpi/${ICON_NAME}" \
"${BUILD_DIR}/drawable-hdpi/${ICON_NAME}"

ln -s "${ROOT_DIR}/drawable-xhdpi/${ICON_NAME}" \
"${BUILD_DIR}/drawable-xhdpi/${ICON_NAME}"

ln -s "${ROOT_DIR}/drawable-xxhdpi/${ICON_NAME}" \
"${BUILD_DIR}/drawable-xxhdpi/${ICON_NAME}"

# If the user has not any custom notification icons we remove the dangling symlinks
# or else the build will fail
#
find -L "${BUILD_DIR}" -type l -delete
