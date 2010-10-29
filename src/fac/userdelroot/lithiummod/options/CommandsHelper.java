/**
 *  LithiumMod Options 
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
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * Commands Helper class
 * 
 * @author userdelroot
 * Oct 26, 2010
 */
public class CommandsHelper {

    private static final String TAG = "CommandsHelpers ";
    private static final String BASH_RC = "/mnt/sdcard/.bashrc";
    private static final String BASH_ALIASES = "/mnt/sdcard/.bash_aliases";
    private static final String BASH_BIN = "/mnt/sdcard/bin";
    private static final String HOSTS_FILE = "/system/etc/hosts";
    private static final String HOSTS_FILE_ORIG = "/system/etc/hosts.orig.lm";
    private static final String BLOCK_ADS = "/system/etc/adblock.hosts.lm";
    private static final String BLOCK_ADS_TEMP = "/mnt/sdcard/adblock.lithium";
    
    private static final String ASSET_BASH_RC = "bashrc";
    private static final String ASSET_BASH_ALIASES = "bash_aliases";
    private static final String ASSET_BLOCK_ADS = "hosts.adblocking";
    
    /**
     * Reboot the device, pretty simple
     * Requires: root
     */
    public static void reboot() {

        Process p = null;
        
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream stdout = new DataOutputStream(p.getOutputStream());
            stdout.writeBytes("reboot\n");
            stdout.flush();
            stdout.close();
            
        } catch (IOException e) {
            if (Log.LOGV)
                Log.e(TAG + "reboot " + e.getLocalizedMessage().toString());
        }
    }
    
    /**
     * Setup bash enviroment on the sdcard
     * does not require root
     * @param enabled
     * @param c
     */
    public static void bashEnv(boolean enabled, Context c) {
        
        // make sure the device external storage is mounted.
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(c, R.string.external_storage_umounted, Toast.LENGTH_SHORT).show();
            return;
        }

    
        if (enabled) {
            writeBashRc(c);
        }
        else {
            File file = new File (BASH_RC);
            if (file.exists())
                file.delete();
            
            file = null;
            file = new File(BASH_ALIASES);
            if (file.exists())
                file.delete();
            
            file = null;
        }

        
        Options.dismissProgressDialog();
    }

    
    /**
     * Write bash_aliases
     * does not require root
     * @param c
     */
    private static void writeBashAliases(Context c) {

        InputStream is = null;
        
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        File ofile = new File(BASH_ALIASES);
        

        
        try {
        
            if (!ofile.exists() && !ofile.createNewFile()) {
                Toast.makeText(c, R.string.file_not_writeable, Toast.LENGTH_SHORT).show();
                return;
            }
            
            is = c.getAssets().open(ASSET_BASH_ALIASES);
            fos = new FileOutputStream(ofile);
            osw = new OutputStreamWriter(fos);
            
            int nextChar;
            
            while (( nextChar = is.read() ) != -1) {
                osw.write(nextChar);
                osw.flush();
            }
            
            // Close the streams
            is.close();
            fos.close();
            osw.close();
            ofile = null;
        }
        catch (Exception e) {
            Toast.makeText(c, R.string.unknown_failure, Toast.LENGTH_SHORT).show();
            if (Log.LOGV)
                Log.e(TAG + "Exception " + e.getLocalizedMessage().toString());
            return;
        }    
    }

    /**
     * Write bashrc
     * does not require root
     * @param c
     */
    private static void writeBashRc(Context c) {

        File ofile = new File(BASH_RC);
        InputStream is = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        
        try {
            
            if (!ofile.exists() && !ofile.createNewFile()) {
                Toast.makeText(c, R.string.file_not_writeable, Toast.LENGTH_SHORT).show();
                return;
            }
            
            is = c.getAssets().open(ASSET_BASH_RC);
            fos = new FileOutputStream(ofile);
            osw = new OutputStreamWriter(fos);
            
            int nextChar;
            
            while (( nextChar = is.read() ) != -1) {
                osw.write(nextChar);
                osw.flush();
            }
            
            // Close the streams
            is.close();
            fos.close();
            osw.close();
            ofile = null;
        }
        catch (Exception e) {
            Toast.makeText(c, R.string.unknown_failure, Toast.LENGTH_SHORT).show();
            if (Log.LOGV)
                Log.e(TAG + "Exception " + e.getLocalizedMessage().toString());
            return;
        }        
        
        ofile = new File(BASH_BIN);
       
        if (!ofile.exists()) {
            if (Log.LOGV)
                Log.i(TAG + "creating " + BASH_BIN + " directory\n");
            
            ofile.mkdir();
        }
        writeBashAliases(c);
    }

    /**
     * block ads hosts file Requires: root
     * @param block
     * @param c
     */
    public static void blockAds(final boolean block, final Context c) {

        Thread t = new Thread() {

            File tmp = null;
            File hosts = null;            
            File ads = null;

            InputStream is = null;

            FileOutputStream fos = null;

            OutputStreamWriter osw = null;

            Process p = null;

            DataOutputStream stdout = null;


            @Override
            public void run() {

                // only do this if file does not exist already
                ads = new File(BLOCK_ADS);
                try {

                    // get root access
                    p = Runtime.getRuntime().exec("su");
                    stdout = new DataOutputStream(p.getOutputStream());

                    // mount read/write
                    stdout.writeBytes("busybox mount -o remount,rw /system\n");
                    stdout.flush();
                    
                    if (!ads.exists()) {
                        tmp = new File(BLOCK_ADS_TEMP);
                        
                        // write the asset file out to /system/adblock.disabled
                        is = c.getAssets().open(ASSET_BLOCK_ADS);
                        fos = new FileOutputStream(tmp);
                        osw = new OutputStreamWriter(fos);

                        int nextChar;

                        while ((nextChar = is.read()) != -1) {
                            osw.write(nextChar);
                            osw.flush();
                        }

                        // Close the streams
                        is.close();
                        fos.close();
                        osw.close();
                        
                        // place current hosts file to top of ads host file
                        stdout.writeBytes("busybox cat " + HOSTS_FILE + " | cat - " + BLOCK_ADS_TEMP + " > " + BLOCK_ADS + "\n");
                        stdout.flush();

                       
                    
                        // backup original hosts file
                        hosts = new File(HOSTS_FILE_ORIG);
                        if (!hosts.exists()) {
                            stdout.writeBytes("busybox cp " + HOSTS_FILE + " " + HOSTS_FILE_ORIG + "\n");
                            stdout.flush();
                        }
                    
                    }

                    // at this point we should have all files in place.
        
                    // block or unblock adds
                    if (block) {
                        stdout.writeBytes("busybox cp " + BLOCK_ADS + " " + HOSTS_FILE + "\n");
                        stdout.flush();
                        Log.i(TAG + "block ads " + block);
                        
                    }
                    else {
                        stdout.writeBytes("busybox cp " + HOSTS_FILE_ORIG + " " + HOSTS_FILE + "\n");
                        stdout.flush();
                        Log.i(TAG + "block ads " + block);
                    }

                    Thread.sleep(500);
                    // mount read-only
                    stdout.writeBytes("busybox mount -o remount,ro /system\n");
                    stdout.flush();
                    stdout.writeBytes("exit\n");
                    stdout.flush();
                    stdout.close();
                    ads = null;
                
                } catch (IOException e) {
                    if (Log.LOGV)
                        Log.e(TAG + "IOException: " + e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                finally {
                    if (p != null)
                        p.destroy();
                }
                
                myHandler.handleMessage(myHandler.obtainMessage());
            }

        };
        t.start();
    }
    
    
    static Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Options.dismissProgressDialog();
        }
    };
}
