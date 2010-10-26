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
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package fac.userdelroot.lithiummod.options;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * LCDDensity to change the lcd density
 * 
 * @author userdelroot Oct 26, 2010
 */
public class LCDDensity {

    private static final String TAG = "LCDDensity ";

    private static final HashMap<String, String> mPhoneDensityMap = new HashMap<String, String>();

    private static final String MODEL = Build.MODEL.toLowerCase();

    private boolean bSuccess;

    // constructor
    LCDDensity() {
        // fill in the phone default densities.
        mPhoneDensityMap.put("droid", "240");
        mPhoneDensityMap.put("incredible", "240");
        bSuccess = false;
    }

    /**
     * getBuildPropLcdDensity get the build.prop ro.sf.lcd_density from getprop
     * @return
     */
    public int getBuildPropLcdDensity() {

        if (Log.LOGV)
            Log.v(TAG + "");

        Process p = null;
        int cur = 0;

        try {

            // get root enviroment
            p = Runtime.getRuntime().exec("su");
            DataOutputStream stdout = new DataOutputStream(p.getOutputStream());
            DataInputStream stdin = new DataInputStream(p.getInputStream());

            
            stdout.writeBytes("getprop ro.sf.lcd_density\n");
            stdout.flush();

            String temp  = stdin.readLine().trim();
            
            cur = Integer.valueOf(temp);
            
            stdout.writeBytes("exit\n");
            stdout.close();
            stdin.close();

        } catch (IOException e) {
            if (Log.LOGV)
                Log.e(TAG + "IOException " + e.getLocalizedMessage().toString());

        }
        
        if (Log.LOGV)
            Log.v(TAG + "getBuildPropDensity() " + cur);
        
        return (cur < 160) ? 160 : cur;
    }

    /**
     * gets the default lcd density for this phone
     * 
     * @return
     */
    public String getDefaultDensity() {
        return mPhoneDensityMap.get(MODEL);
    }

    /**
     * Sets the requested density
     * 
     * @param density
     * @param context
     */
    public void setPhoneDensity(final int density) {

        if (Log.LOGV)
            Log.v(TAG + "setPhoneDensity " + density);

        Thread t = new Thread() {
            @Override
            public void run() {

                Process p = null;

                try {

                    // get root enviroment
                    p = Runtime.getRuntime().exec("su");
                    DataOutputStream stdout = new DataOutputStream(p.getOutputStream());

                    // this is a little easier to read then one long ass string
                    String str1 = "busybox cat /system/build.prop |";
                    String str2 = " sed -e \"s/ro\\.sf\\.lcd_density=.*/ro\\.sf\\.lcd_density="
                            + density + "/g\"";
                    String str3 = " > /data/local/tmp/build.prop.new\n";
                    String command = str1 + str2 + str3;
                    // do some crazy sed stuff
                    stdout.writeBytes(command);
                    stdout.flush();

                    // remount filesystem read/write
                    stdout.writeBytes("busybox mount -o remount,rw /system\n");
                    stdout.flush();

                    File file = new File("/system/build.prop.orig");

                    // backup original build.prop if it doesn't exist
                    if (!file.exists()) {
                        stdout.writeBytes("busybox cp /system/build.prop /system/build.prop.orig\n");
                        stdout.flush();
                    }

                    // mv temp file to build.prop
                    stdout.writeBytes("busybox mv /data/local/tmp/build.prop.new /system/build.prop\n");
                    stdout.flush();

                    // remount filesystem read-only
                    stdout.writeBytes("busybox mount -o remount,ro /system\n");
                    stdout.flush();

                    // edit su
                    stdout.writeBytes("exit\n");
                    stdout.flush();

                    // close stdout stream
                    stdout.close();

                    // wait for return status
                    p.waitFor();

                    if (p.exitValue() != 255) {

                        // success
                        bSuccess = true;

                        if (Log.LOGV)
                            Log.i(TAG + "LCD Density setting successfully ");

                    } else {
                        // epic failure
                        bSuccess = false;

                        if (Log.LOGV)
                            Log.i(TAG + "LCD Density setting failure ");
                    }

                    Thread.sleep(1000);
                    myHandler.handleMessage(myHandler.obtainMessage());

                } catch (IOException e) {
                    if (Log.LOGV)
                        Log.e(TAG + "IOException " + e.getLocalizedMessage().toString());

                } catch (InterruptedException e) {
                    if (Log.LOGV)
                        Log.e(TAG + "InterruptedException " + e.getLocalizedMessage().toString());

                }

            }
        };
        t.start();

    }

    Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Options.dismissProgressDialog();
        }
    };

    public boolean getIsSuccess() {

        return bSuccess;
    }
}
