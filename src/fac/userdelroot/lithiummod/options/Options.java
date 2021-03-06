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

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

/**
 * Options preferenceactivity
 * 
 * @author userdelroot Oct 26, 2010
 */
public class Options extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "Options ";
    private static Context mContext;
    private LCDDensity mLcdDensity;
    
    // SharedPreferences keys and keys for xml files
    private static final String PREFS_NAME = "fac.userdelroot.lithiummod.options_preferences";
    private static final String LCD_DENSITY = "lcd_density";
    private static final String BASH_ENVIRO = "bash_enviro";
    private static final String BLOCK_ADS = "block_ads";
    private static final String DOCK_DESK = "dock_desk";
    private static final String DOCK_CAR = "dock_car";
    private static final String DISABLE_JIT = "disable_jit";
    
    private static final String DOCK_STATE = "android.intent.extra.DOCK_STATE";
    private static final String DOCK_EVENT = "android.intent.extra.DOCK_EVENT";
    private static final String ANDROID_MAIN = "android.intent.action.MAIN";
    
    private static ProgressDialog mProgressDialog;
    private SeekBarPref mDensitySeekBarPref;
    private static final int LCDDENSITY = 1;
    private static final int BLOCKADS = 2;
    private static final int JITCOMPILER = 3;
    private boolean miFaceSuccess;
    
    
    private PreferenceScreen mDockDesk, mDockCar;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.options);
        mLcdDensity = new LCDDensity();
        mContext = getApplicationContext();
        miFaceSuccess = false;
        
        mDensitySeekBarPref = (SeekBarPref) findPreference(LCD_DENSITY);

        mDockDesk = (PreferenceScreen) findPreference(DOCK_DESK);
        mDockCar = (PreferenceScreen) findPreference(DOCK_CAR);
        
        // load sharedpreferences
        loadSharedPreferences();
    }

    /**
     * load our shared preferences
     */
    private void loadSharedPreferences() {
        
        try {
            SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
            int val = prefs.getInt(LCD_DENSITY, -1);
            int propval = 0;
            
            boolean lcdDisabled = false;
            /*
             * Always check build.prop first.
             * If build.prop and LM do not match use the build.prop lcd_density
             */
            if (mLcdDensity != null) {
                propval = mLcdDensity.getBuildPropLcdDensity();
                if (propval != val || propval < 0) {

                    if (propval < mLcdDensity.getMinDensity()) {
                        if (Log.LOGV)
                            Log.w(TAG + "LCD Density " + propval + " is below " + mLcdDensity.getMinDensity() + " min value");
                    }
                    
                    SharedPreferences.Editor p = this.getSharedPreferences(PREFS_NAME, 0).edit();
                    
                    /*
                     * If this is 0 then, either they have no ro.sf.lcd_density line or they have it commented out.
                     * Bitch out it to user and disable this feature until they fix they build.prop
                     */
                    if (propval < 0) {
                        Toast.makeText(mContext, R.string.lcd_density_prop_missing, Toast.LENGTH_LONG).show();
                        lcdDisabled = true;
                    }
                    else {
                    
                        p.putInt(LCD_DENSITY, propval);
                        p.commit();
                        val = propval;
                    }
                    if (Log.LOGV)
                        Log.i(TAG + "loadSharedPreferences() build.prop and lm options lcd_density do not match\n using build.prop");
                }
            }
            

            String str = null;
            if (lcdDisabled) {
                str = getStrResId(R.string.lcd_density_disabled);
                mDensitySeekBarPref.setSummary(str);
                mDensitySeekBarPref.setEnabled(false);
            }
            else {
                str = getStrResId(R.string.format_summary_current);
                mDensitySeekBarPref.setSummary(String.format(str, val));
            }
        }
        catch (NullPointerException e) {
            // do nothing
            if (Log.LOGV)
                Log.v(TAG + "loadSharedPreferences() nullpointer " + e.getLocalizedMessage().toString());
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {

        if (key.equals(LCD_DENSITY)) {
            if (Log.LOGV)
                Log.v(TAG + "seekbarpref changed " + sharedPrefs.getInt(key, -1));
            int val = sharedPrefs.getInt(LCD_DENSITY, -1);
           
            String str = getStrResId(R.string.format_summary_current);
            mDensitySeekBarPref.setSummary(String.format(str,val));
            
            if (mLcdDensity == null) {
                if (Log.LOGV)
                    Log.e(TAG + "onSharedPreferenceChanged() mLcdDensity is null ");
                return;
            }
            
            prepareProgressBar(LCDDENSITY,R.string.loading_please_wait);
            mLcdDensity.setPhoneDensity(val);
            return;
        }
        
        if (key.equals(BASH_ENVIRO)) {
           boolean val = sharedPrefs.getBoolean(BASH_ENVIRO, false);
           prepareProgressBar(-1, R.string.loading_please_wait);
           CommandsHelper.bashEnv(val, mContext);
           return;
        }
        
        if (key.equals(BLOCK_ADS)) {
            boolean val = sharedPrefs.getBoolean(BLOCK_ADS, false);
            prepareProgressBar(BLOCKADS, R.string.loading_please_wait);
           new BlockAds(val, mContext, myStdio).start();
           // ba.start();
            return;
        }
        
        if (key.equals(DISABLE_JIT)) {
            boolean val = sharedPrefs.getBoolean(DISABLE_JIT, false);
            prepareProgressBar(JITCOMPILER, R.string.loading_please_wait);
            new JitCompiler(val, mContext, myStdio).start();
            return;
        }
    }

    private void prepareProgressBar(final int which, int resId) {
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        String str = mContext.getResources().getString(resId);
        mProgressDialog.setMessage(str);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                
                switch (which) {
                    case LCDDENSITY:
                        showRebootDialog(LCDDENSITY);
                        break;
                    
                    case BLOCKADS:
                        showRebootDialog(BLOCKADS);
                        break;
                        
                    case JITCOMPILER:
                        showRebootDialog(JITCOMPILER);
                        break;
                    default:
                        break;
                }
            }
            
        });
    }
    
    public static void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }
    
    /**
     * Show a reboot dialog to have changes take affect
     * @param which
     */
    private void showRebootDialog(int which) {

        int title = 0;
        int msg = 0;
        switch (which) {
            case LCDDENSITY:
                boolean isSuccess = false;

                if (mLcdDensity != null)
                    isSuccess = mLcdDensity.getIsSuccess();

                if (!isSuccess) {
                    Toast.makeText(this, R.string.lcd_density_change_failure, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                
                title = R.string.lcd_density_dialog_reboot_title;
                msg = R.string.reboot_required_msg;
                break;
                
            case BLOCKADS:
                if (!miFaceSuccess) {
                    Toast.makeText(mContext, R.string.block_ads_failure, Toast.LENGTH_LONG).show();
                    return;
                }
                title = R.string.block_ads_dialog_reboot_title;
                msg = R.string.reboot_required_msg;
                
                // reset this as it gets used other places
                miFaceSuccess = false;
                break;
                
            case JITCOMPILER:
                if (!miFaceSuccess) {
                    Toast.makeText(mContext, R.string.jit_failure, Toast.LENGTH_LONG).show();
                    return;
                }
                
                title = R.string.jit_compiler_dialog_reboot_title;
                msg = R.string.reboot_required_msg;
                
                // reset this as it gets used other places
                miFaceSuccess = false;
                break;
                
            default:
               return;

        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(msg)
                .setPositiveButton(R.string.dialog_btn_reboot,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CommandsHelper.reboot();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.dialog_btn_cancel, null).show();
    }
    
    private String getStrResId(int strResId) {
        return mContext.getResources().getString(strResId);
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        mDockDesk.setOnPreferenceClickListener(this);
        mDockCar.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        
        mDockDesk.setOnPreferenceClickListener(null);
        mDockCar.setOnPreferenceClickListener(null);
    }
    
    
    private void dockDesk(boolean docked) {

        try {
            
            if (!docked) {
                Intent dockIntent = new Intent(DOCK_EVENT);
                dockIntent.putExtra(DOCK_STATE, 0);
                getBaseContext().sendStickyBroadcast(dockIntent);
                Log.i(TAG + "dockDesk() DockDesk off");
                return;
            }

            Intent dockIntent = new Intent(DOCK_EVENT);
            dockIntent.putExtra(DOCK_STATE, 1);
            getBaseContext().sendStickyBroadcast(dockIntent);
            Intent intent = new Intent(ANDROID_MAIN);
            intent.addCategory("android.intent.category.DESK_DOCK");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getBaseContext().startActivity(intent);
            Log.i(TAG + "dockDesk() DockDesk on");
            return;
        } catch (Exception e) {
            Log.e(TAG + "dockDesk()", e);
        }
    }

    private void dockCar(boolean docked) {

        try {
            if (!docked) {

                Intent dockIntent = new Intent(DOCK_EVENT);
                dockIntent.putExtra(DOCK_STATE, 0);
                getBaseContext().sendStickyBroadcast(dockIntent);
                // getBaseContext().startActivity(dockIntent);
                Log.i(TAG + "dockCar() Docking off");
                return;
            }

            Intent dockIntent = new Intent(DOCK_EVENT);
            dockIntent.putExtra(DOCK_STATE, 2);
            getBaseContext().sendStickyBroadcast(dockIntent);
            // getBaseContext().startActivity(dockIntent);
            Intent intent = new Intent(ANDROID_MAIN);
            intent.addCategory("android.intent.category.CAR_DOCK");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getBaseContext().startActivity(intent);
            Log.i(TAG + "dockCar() Docking on");
            return;
        } catch (Exception e) {
            Log.e(TAG + "dockCar()", e);
        }
    }
    
    
    ifaceStdio myStdio = new ifaceStdio() {

        @Override
        public void setStdErr(String err) {
        }

        @Override
        public void setStdOut(String out) {
        }

        @Override
        public void setExitStatus(int code) {
        }

        @Override
        public void setIsSuccess(boolean success) {
            miFaceSuccess = success;
        }
        
    };


    @Override
    public boolean onPreferenceClick(Preference pref) {
        
        if (pref == mDockDesk) {
            
            dockDesk(true);
            
            return true;
        }
        if (pref == mDockCar) {
            dockCar(true);
            return true;
        }
            
        
        return false;
    }
}

