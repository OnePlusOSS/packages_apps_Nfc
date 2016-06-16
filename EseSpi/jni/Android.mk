LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_MODULE:= libese_spi_jni

LOCAL_CFLAGS += -DNXP_LDR_SVC_VER_2

# All of the source files that we will compile.
LOCAL_SRC_FILES += NativeEseManager.cpp \
    NativeEseAla.cpp \
    SpiJniUtil.cpp \
    SpiChannel.cpp \
    Mutex.cpp \
    CondVar.cpp


# Also need the JNI headers.
LOCAL_C_INCLUDES += \
    libcore/include \
    frameworks/native/include \
    external/libese-spi/inc \
    external/libese-spi/common \
    external/libese-spi/utils \
    external/p61-jcop-kit/include \
    
LOCAL_SHARED_LIBRARIES := \
    libicuuc \
    libnativehelper \
    libcutils \
    libutils \
    libese-spi \
    libp61-jcop-kit


#### Select the JCOP OS Version ####
JCOP_VER_3_0 := 1
JCOP_VER_3_1_1 := 2
JCOP_VER_3_1_2 := 3
JCOP_VER_3_2 := 4

LOCAL_CFLAGS += -DJCOP_VER_3_0=$(JCOP_VER_3_0)
LOCAL_CFLAGS += -DJCOP_VER_3_1_1=$(JCOP_VER_3_1_1)
LOCAL_CFLAGS += -DJCOP_VER_3_1_2=$(JCOP_VER_3_1_2)
LOCAL_CFLAGS += -DJCOP_VER_3_2=$(JCOP_VER_3_2)

LOCAL_CFLAGS += -DNFC_NXP_ESE_VER=$(JCOP_VER_3_1_2)

#LOCAL_CFLAGS += -DISO7816_4_APDU_PARSER_ENABLE
include $(BUILD_SHARED_LIBRARY)
