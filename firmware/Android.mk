# Copyright (C), 2008-2012, OPPO Mobile Comm Corp., Ltd
# VENDOR_EDIT
# Ansroid.mk for the pn544 firmware compile.
#
# 15 Oct 2013, Yuyi <oppo@Independent Group>
# Rewritten to use firmware of pn544.
#
#ifeq ($(filter-out MSM_14021 MSM_14024 MSM_14091 MSM_14001,$(OPPO_TARGET_DEVICE)),)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(shell mkdir -p $(TARGET_OUT)/vendor/firmware)
#LOCAL_MODULE       := libpn547_fw.so
LOCAL_MODULE       := libpn548ad_fw.so
LOCAL_MODULE_TAGS  := optional eng
LOCAL_MODULE_CLASS := VENDOR
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/firmware
#LOCAL_SRC_FILES    := ./firmware/libpn547_fw_08_01_CC_prod_plm.so
LOCAL_SRC_FILES    := ./firmware/libpn548ad_fw.so

include $(BUILD_PREBUILT)
#endif
#Create symbolic link
#$(shell mkdir -p $(TARGET_OUT)/vendor/firmware; \
#        ln -sf /system/lib/libpn544_fw.so \
#               $(TARGET_OUT)/vendor/firmware/libpn544_fw.so)
