/*
 *
 *  Copyright (C) 2015 NXP Semiconductors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.gsma.services.nfc;

import android.app.ActivityThread;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import dalvik.system.DexClassLoader;
import android.nfc.cardemulation.CardEmulation;
import android.util.Log;

import com.gsma.services.utils.InsufficientResourcesException;
import com.gsma.services.nfc.OffHostService;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import com.nxp.nfc.gsma.internal.NxpOffHostService;
/**
 * This class handles "Off-Host" services
 * @since NFCHST6.0 <I>(REQ_127)</I>
 */
public class OffHostService {
    int mUserId;
    String mDescription = null;
    String mSEName = null;
    Drawable mBanner;
    String mPackageName = null;
    String mServiceName = null;
    boolean mModifiable = true;
    List<AidGroup> mAidGroupList = new ArrayList<AidGroup>();
    NxpNfcController mNxpNfcController = null;
    int mBannerResId;
    Context mContext = null;
    static final String TAG = "OffHostService";

    protected OffHostService(int userId, String description, String SELocation,String packageName,
                             String serviceName, boolean modifiable) {
        mBannerResId = 0x00;
        mUserId = userId;
        mDescription = description;
        mSEName  = SELocation;
        mPackageName = packageName;
        mServiceName = serviceName;
        mModifiable = modifiable;  // It will distinguish between static service and dynamic services
    }

    protected OffHostService(NxpOffHostService service) {
        mUserId = service.mUserId;
        mDescription = service.mDescription;
        mSEName  = service.mSEName;
        mPackageName = service.mPackageName;
        mServiceName = service.mServiceName;
        mModifiable = service.mModifiable;  // It will distinguish between static service and dynamic services
        mAidGroupList = convertToOffHostAidGroupList(service.mNxpAidGroupList);
        mBanner = service.mBanner;
        mBannerResId = service.getBannerId();
        mContext = service.getContext();
        mNxpNfcController = service.mNxpNfcController;

        PackageManager pManager = mContext.getPackageManager();
        //final String packName = mContext.getPackageName();
        if (mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(mBannerResId));
                mBanner = pManager.getResourcesForApplication(mPackageName).getDrawable(mBannerResId,null);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        }
    }

    /**
     * Return the Secure Element name which holds the "Off-Host" service.
     * @return Secure Element name holding the "Off-Host" service
     * @since NFCHST6.0
     */
    public String getLocation() {
        return mSEName;
    }

    /**
     * Return the description of the "Off-Host" service.
     * @return The Description of the "Off-Host" service
     * @since NFCHST6.0
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Return the Service Name of the "Off-Host" service.
     * @return The Service name of the "Off-Host" service
     * @since NFCHST6.0
     */
    protected String getServiceName() {
        return mServiceName;
    }

    /**
     * Set a banner for the "Off-Host" service.
     * @param banner A {@link Drawable} object representing the banner
     * @since NFCHST6.0
     */
    public void setBanner(Drawable banner) {
        //mBanner = banner;
        PackageManager pManager = mContext.getPackageManager();
        final String packName = mPackageName; //mNfcController.mContext.getPackageName();
        Log.d(TAG, "setBanner() Resources packName: " + packName);
        try {
            for (int i = 0; i < Class.forName(packName + ".R").getClasses().length; i++) {
                if(Class.forName(packName + ".R").getClasses()[i].getName().split("\\$")[1].equals("drawable")) {
                    if(Class.forName(packName + ".R").getClasses()[i] != null) {
                        Field[] f = Class.forName(packName + ".R").getClasses()[i].getDeclaredFields();
                        for (int counter = 0, max = f.length; counter < max; counter++) {
                            int resId = f[counter].getInt(null);
                            Drawable d = pManager.getResourcesForApplication(packName).getDrawable(resId,null);
                            if ( areDrawablesEqual(banner,d) ) {
                                mBannerResId = resId;
                                mBanner = d;
                                Log.d(TAG, "setBanner() Resources GOT THE DRAWABLE On loop "
                                           + String.valueOf(counter) + "got resId : " + String.valueOf(resId));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setBanner() Resources exception ..." + e.getMessage());
            e.printStackTrace();
        }
        if(mBannerResId == 0x00) {
            Log.d(TAG, "bannerId  set to 0");
            mBannerResId = -1;
            mBanner = banner;
        }
    }

    /**
     * Set a banner for the "Off-Host" service.
     * @param resourceId The desired resource identifier.
     * <LI> The value 0 is an invalid identifier </LI>
     * @exception Resources.NotFoundException <BR>Indicate that the given resourceId does not exist.
     * @since NFCHST8.0
     */
      //Set a banner for the "Off-Host" service.
    public void setBanner(int bannerResId){
        Log.d(TAG, "setBannerResId() with " + String.valueOf(bannerResId));
        mBannerResId = bannerResId;

        PackageManager pManager = mContext.getPackageManager();
        final String packName = mContext.getPackageName();
        if (mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(mBannerResId));
                mBanner = pManager.getResourcesForApplication(packName).getDrawable(mBannerResId,null);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        }
    }

    /**
     * Get a modifiable for dynamic "Off-Host" service.
     * @return modifiable for "Off-Host service
     * @since NFCHST6.0
     */
    protected boolean getModifiable() {
        return mModifiable;
    }

    /**
     * Return the banner linked to the "Off-Host" service.
     * @return {@link Drawable} object representing the banner or <code>null</code> if no banner has been set
     * @since NFCHST6.0
     */
    public Drawable getBanner() {
        return mBanner;
    }

    /**
     * Create a new empty group of AIDs for the "Off-Host" service.
     * @param description Description of the group of AIDs
     * @param category Category the "Off-Host" service belongs to:<BR><UL>
     * <LI><code>android.nfc.cardemulation.CardEmulation.CATEGORY_PAYMENT</code></IL>
     * <LI><code>android.nfc.cardemulation.CardEmulation.CATEGORY_OTHER</code></IL></UL>
     * @return An instance of an {@link AidGroup} class
     * @since NFCHST6.0
     */
    public AidGroup defineAidGroup(String description, String category) {
        if(description == null){
            throw new IllegalArgumentException("Invalid description provided");
        }
        if((category == null) || (category.isEmpty()) ||
                ((category.compareTo(CardEmulation.CATEGORY_OTHER)!=0) &&
                        (category.compareTo(CardEmulation.CATEGORY_PAYMENT)!=0))){
            throw new IllegalArgumentException("Invalid category provided");
        }
        AidGroup aidGroup = new AidGroup(description,category);
        mAidGroupList.add(aidGroup);
        return aidGroup;
    }

    /**
     * Delete an existing AID group from the "Off-Host" service.
     * @param group Instance of an {@link AidGroup} class to be deleted
     * @since NFCHST6.0
     */
    public void deleteAidGroup(AidGroup group) {
        mAidGroupList.remove(group);
    }

    /**
     * Return a list of the AID groups linked to the "Off-Host" service.
     * @return A list of {@link AidGroup} instances or <code>null</code> if no such instance exists.
     * @since NFCHST6.0
     */
    public AidGroup[] getAidGroups() {
        if(mAidGroupList.size() != 0){
            AidGroup aidGroup[] = new AidGroup[mAidGroupList.size()];
            return mAidGroupList.toArray(aidGroup);
        } else {
            return null;
        }
    }

    private ArrayList<android.nfc.cardemulation.NxpAidGroup> convertToCeAidGroupList(List<com.gsma.services.nfc.AidGroup> mAidGroups) {
        ArrayList<android.nfc.cardemulation.NxpAidGroup> mApduAidGroupList = new ArrayList<android.nfc.cardemulation.NxpAidGroup>();
        android.nfc.cardemulation.NxpAidGroup mCeAidGroup = null;
        List<String> aidList = new ArrayList<String>();
        for(com.gsma.services.nfc.AidGroup mGroup : mAidGroups) {
            for(String aid :mGroup.getAidList()) {
                aidList.add(aid);
            }
            mCeAidGroup = new android.nfc.cardemulation.NxpAidGroup(aidList, mGroup.getCategory(), mGroup.getDescription());
            mApduAidGroupList.add(mCeAidGroup);
        }
    return mApduAidGroupList;
    }

    private NxpOffHostService convertToNxpOffhostService(OffHostService service) {
        ArrayList<android.nfc.cardemulation.NxpAidGroup> mAidGroupList = convertToCeAidGroupList(service.mAidGroupList);
        NxpOffHostService mNxpOffHostService = new NxpOffHostService(service.mUserId,service.mDescription, service.mSEName, service.mPackageName, service.mServiceName,
                                                                     service.mModifiable);
        mNxpOffHostService.setBanner(service.mBanner);
        mNxpOffHostService.setBannerId(service.mBannerResId);
        mNxpOffHostService.mNxpAidGroupList.addAll(mAidGroupList);
        return mNxpOffHostService;
  }

    /**
     * Update the Android Framework with all pending updates.
     * @exception InsufficientResourcesException <BR>Indicate that insufficient resources are available in the routing table.
     * @since NFCHST6.0
     */
    public void commit() throws InsufficientResourcesException{
        boolean status = false;
       // if(mModifiable ==true) {
            Log.d(TAG, "GSMA: banner ResId=" + String.valueOf(mBannerResId));
            Log.d(TAG, "GSMA: banner =" + String.valueOf(mBanner));
            status = mNxpNfcController.commitOffHostService(mUserId, mPackageName, convertToNxpOffhostService(this));
            Log.d("GSMA", " commit status value" + status);
            if(status == false)
                throw new InsufficientResourcesException("Routing Table is Full, Cannot Commit");
      // } else {
           // throw new InsufficientResourcesException("OffHostService: Cannot Commit static off host Service");
       // }
    }
    private void setNxpNfcController(NxpNfcController nxpNfcController) {
        mNxpNfcController = nxpNfcController;
    }

    private ArrayList<com.gsma.services.nfc.AidGroup> convertToOffHostAidGroupList(List<android.nfc.cardemulation.NxpAidGroup> mAidGroups) {
        ArrayList<com.gsma.services.nfc.AidGroup> mOffHostAidGroups= new ArrayList<com.gsma.services.nfc.AidGroup>();
        com.gsma.services.nfc.AidGroup mAidGroup;
        String aidGroupDescription = "";
        for(android.nfc.cardemulation.NxpAidGroup mCeAidGroup: mAidGroups) {
            if(mCeAidGroup.getDescription() == null) {
                aidGroupDescription = "";
            }
            else {
                aidGroupDescription = mCeAidGroup.getDescription();
            }
            mAidGroup = defineAidGroup(aidGroupDescription, mCeAidGroup.getCategory());
            for(String aid : mCeAidGroup.getAids()) {
                mAidGroup.addNewAid(aid);
            }
            mOffHostAidGroups.add(mAidGroup);
        }
        return mOffHostAidGroups;
    }

    /*Compare drawable resources(ex: .png/.jpg/.bmp/..)
     * Description: if constantState of two drawables are equal, then Drawables are equal
     * However converse of it is not necessarily true.
     * In case ConstantState's are not equal, their bitmaps are compared.*/
    private boolean areDrawablesEqual(Drawable drawableA, Drawable drawableB) {
        Drawable.ConstantState stateA = drawableA.getConstantState();
        Drawable.ConstantState stateB = drawableB.getConstantState();
        if(stateA != null && stateB != null && stateA.equals(stateB))
            return true;
        else if(areDrawableBitmapsEqual(drawableA,drawableB))
            return true;
        else
            return false;
    }

    private boolean areDrawableBitmapsEqual(Drawable drawableA, Drawable drawableB) {
        boolean result =false;
        if (drawableA instanceof BitmapDrawable) {
            if (drawableB instanceof BitmapDrawable) {
                Bitmap bitmapA =  ((BitmapDrawable) drawableA).getBitmap();
                Bitmap bitmapB =  ((BitmapDrawable) drawableB).getBitmap();
                if (bitmapA.getWidth() == bitmapB.getWidth() &&
                        bitmapA.getHeight() == bitmapB.getHeight()) {
                    if(bitmapA.sameAs(bitmapB))
                        result =true;
                }
            }
        }
        return result;
    }

}
