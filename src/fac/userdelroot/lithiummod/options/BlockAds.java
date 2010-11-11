/**
 * LithiumMod Options
 *
 * Copyright (C) 2010  userdelroot r00t316@gmail.com (Frank C.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package fac.userdelroot.lithiummod.options;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public class BlockAds extends Thread {

    private static final String TAG = "BlockAds ";

    private static final String HOSTS_FILE = "/system/etc/hosts";
    private static final String HOSTS_FILE_ORIG = "/system/etc/hosts.orig.lm";
    private static final String BLOCK_ADS = "/system/etc/blockads.hosts.lm";
    private static final String BLOCK_ADS_TMP = "/mnt/sdcard/blockads.lithium";
    private static final String ASSET_BLOCK_ADS = "hosts.adblocking";

    private boolean blockAds;
    private String stdError;
    private String stdOut;
    private int exitStatus;
    private boolean isSuccess;

    private Context mContext;
    private volatile Thread destroy;
    private ifaceStdio mStdio;
    
    /**
     * Constructor
     * @param block
     * @param c
     * @param stdio
     */
    BlockAds(boolean block, Context c, ifaceStdio stdio) {
        blockAds = block;
        isSuccess = true;
        mContext = c;
        destroy = this;
        this.setName("BlockAds");
        mStdio = stdio;
    }

   /**
    * stops our thread.  Used if there is an error or has finished.
    */
    private void cleanUp() {
        mStdio.setIsSuccess(isSuccess);
        myHandler.handleMessage(myHandler.obtainMessage());
        systemRO();
        
        destroy = null;
        isSuccess = false; // need to set this to false

    }
   
   /**
    * Thread.
    * This is the recommended way of stopping an active thread as stop() and destroy() are depreciated
    */
    public void run() {
        Thread mythread = this;
        while (destroy == mythread) {


            if (!isSuccess)
                return;
            
            File ads = new File(BLOCK_ADS); // this only gets initialized first
                                            // because of a check below.

            
            // mount the /system read/write
            systemRW();
            if (!isSuccess) {
                Log.w(TAG + "failed to mount /system read-write");
                cleanUp();
                return;
            }
            // if the ads file exists no need to proceed on building a new
            // one.
            if (!ads.exists()) {

                createBlockAdsTmp();
                if (!isSuccess) {
                    return;
                }

                // copy from the sdcard to the /system/etc/
                copyAdsTmpToLoc();
                if (!isSuccess) {
                    return;
                }

                hostsFileBackup();
                if (!isSuccess) {
                    return;

                }
            }

            // block the ads
            blockAds();
            
            cleanUp();

        }
        
        
    }

    /**
     * handler needed to get back to the ui thread
     */
    Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Options.dismissProgressDialog();
        }
    };

    /**
     * Creates a temp ads file from an asset on the sdcard
     * requires write_external_storage in manifest.
     */
    private void createBlockAdsTmp() {
        File adstmp = new File(BLOCK_ADS_TMP);

        // write the asset file out to /system/adblock.disabled
        InputStream is;
        try {
            is = mContext.getAssets().open(ASSET_BLOCK_ADS);
            FileOutputStream fos = new FileOutputStream(adstmp);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            int nextChar;

            while ((nextChar = is.read()) != -1) {
                osw.write(nextChar);
                osw.flush();
            }

            // Close the streams
            is.close();
            fos.close();
            osw.close();
            
        } catch (IOException e) {
            Log.e(TAG + "createBlockAdsTmp IOException ", e);
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * amend hosts into tmp ads write out blockads
     */
    private void copyAdsTmpToLoc() {
        String cmd = "busybox cat " + HOSTS_FILE + " | cat - " + BLOCK_ADS_TMP + " > " + BLOCK_ADS
                + " || echo failure\n";
        try {
            OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
            output.start();
            output.join();
        } catch (InterruptedException e) {
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * backup the original hosts file
     */
    private void hostsFileBackup() {

        File hosts = new File(HOSTS_FILE_ORIG);
        if (hosts.exists()) {
            return;
        }

        String cmd = "busybox cp " + HOSTS_FILE + " " + HOSTS_FILE_ORIG + " || echo failure\n";
        try {
            OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
            output.start();
            output.join();
        } catch (InterruptedException e) {
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * mount /system read/write
     */
    private void systemRW() {

        String cmd = "busybox mount -o remount,rw /system  || echo failure\n";
        try {
            OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
            output.start();
            output.join();
        } catch (InterruptedException e) {
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * mount system read-only
     */
    private void systemRO() {

        String cmd = "busybox mount -o remount,ro /system || echo failure\n";
        try {
            OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
            output.start();
            output.join();
        } catch (InterruptedException e) {
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * enable / disable block ads
     */
    private void blockAds() {
        String cmd = "busybox cp " + BLOCK_ADS + " " + HOSTS_FILE + " || echo failure\n";
        if (!blockAds) {
            cmd = "busybox cp " + HOSTS_FILE_ORIG + " " + HOSTS_FILE + " || echo failure\n";
        } 
        
        try {
            OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
            output.start();
            output.join();
        } catch (InterruptedException e) {
            Log.e(TAG + e.getLocalizedMessage());
            isSuccess = false;
            cleanUp();
            return;
        }
    }

    /**
     * interface used by other threads when they have finished.
     */
    ifaceStdio stdioSetters = new ifaceStdio() {

        @Override
        public void setStdErr(String err) {
            stdError = err;
            Log.i(TAG + "stderr:"+ stdError);
        }

        @Override
        public void setStdOut(String out) {
            stdOut = out;
            Log.i(TAG + "stdout:"+ stdOut);
        }

        @Override
        public void setExitStatus(int code) {
            exitStatus = code;
            Log.i(TAG + "exit status:"+ exitStatus);
        }

        @Override
        public void setIsSuccess(boolean success) {
            isSuccess = success;
            Log.i(TAG + "isSuccess:" + isSuccess);
        }

    };
}
