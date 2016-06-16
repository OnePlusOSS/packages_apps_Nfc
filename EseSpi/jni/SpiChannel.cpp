/*
 * Copyright (C) 2014 NXP Semiconductors
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

#include "SpiChannel.h"
#include <cutils/log.h>
#include "SyncEvent.h"

extern "C"
{
    #include "phNxpEseHal_Api.h"
}



namespace android
{
   extern SyncEvent     sTransceiveEvent;  // event for Set_Config....
   extern uint8_t*      sTransceiveData;
   extern uint32_t      sTransceiveDataLen;
   extern BOOLEAN       isIntialized();
}
bool spiChannelForceClose = false;
INT16 mHandle = DEFAULT;

/*******************************************************************************
**
** Function:        open
**
** Description:     Opens the DWP channel to eSE
**
** Returns:         True if ok.
**
*******************************************************************************/
INT16 open()
{
    bool stat = true;
    INT16 status = EE_ERROR_OPEN_FAIL;
    ALOGE("SpiChannel: Sec Element open Enter");
    stat = android::isIntialized();
    spiChannelForceClose = false;
    /*If channel is open and client handle is free*/
    if((stat == true)&&(mHandle != DEFAULT))
    {
        ALOGE("SpiChannel: Channel open success");
        status = mHandle;
    }
    else
    {
        ALOGE("SpiChannel: Open Failed");
    }
    return status;
}

/*******************************************************************************
**
** Function:        close
**
** Description:     closes the DWP connection with eSE
**
** Returns:         True if ok.
**
*******************************************************************************/
bool close(INT16 clientType)
{
    bool stat = false;
    if(clientType == mHandle)
    {
        mHandle = DEFAULT;
        stat = true;
    }
//Do Nothing
     return stat;
}

/*******************************************************************************
**
** Function:        transceive
**
** Description:     transceive the DWP connection with eSE
**
** Returns:         True if ok.
**
*******************************************************************************/
bool transceive (UINT8* xmitBuffer, INT32 xmitBufferSize, UINT8* recvBuffer,
                 INT32 recvBufferMaxSize, INT32& recvBufferActualSize, INT32 timeoutMillisec)
{
    static const char fn [] = "SpiChannel::transceive";
    ALOGD("%s: enter", fn);
    ESESTATUS status = ESESTATUS_SUCCESS;
    bool stat = false;
    UINT32 recvBuffLen=recvBufferMaxSize;

    if(spiChannelForceClose == true)
        return stat;

    SyncEventGuard guard (android::sTransceiveEvent);
    status = phNxpEseP61_Transceive(xmitBufferSize, xmitBuffer);
    if (status == ESESTATUS_SUCCESS)
        android::sTransceiveEvent.wait ();
     else
         ALOGE ("%s: phNxpEseP61_Transceive Failed", __FUNCTION__);

    recvBufferActualSize = android::sTransceiveDataLen;

    if(recvBufferActualSize > 0)
    {
        ALOGD("%s: recvBuffLen=0x0%x", fn, android::sTransceiveDataLen);
        memcpy (recvBuffer, android::sTransceiveData, recvBufferActualSize);
        stat = true;
    }
    ALOGD("%s: exit; status=0x0%x", fn, stat);
    return stat;
}

/*******************************************************************************
**
** Function:        doeSE_Reset
**
** Description:     Reset the DWP connection with eSE
**
** Returns:         True if ok.
**
*******************************************************************************/
void doeSE_Reset(void)
{
    static const char fn [] = "SpiChannel::doeSE_Reset";
    ESESTATUS status = ESESTATUS_SUCCESS;
    ALOGD("%s: enter:", fn);
    if(status != phNxpEseP61_reset())
    {
        ALOGD ("phNxpEseP61_reset: Failed");
    }
    ALOGD("%s: exit:", fn);

}
