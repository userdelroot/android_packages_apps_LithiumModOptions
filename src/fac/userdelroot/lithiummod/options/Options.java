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
import android.preference.PreferenceActivity;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

/**
 * Options preferenceactivity
 * 
 * @author userdelroot Oct 26, 2010
 */
public class Options extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String TAG = "Options ";
    private static Context mContext;
    private LCDDensity mLcdDensity;
    private static final String PREFS_NAME = "fac.userdelroot.lithiummod.options_preferences";
    private static final String LCD_DENSITY = "lcd_density";
    private static ProgressDialog mProgressDialog;
    private SeekBarPref mDensitySeekBarPref;
    private String mDensitySummary;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.options);

        mLcdDensity = new LCDDensity();
        mContext = getApplicationContext();
        
        mDensitySeekBarPref = (SeekBarPref) findPreference(LCD_DENSITY);
        
        mDensitySummary = mContext.getString(R.string.format_summary_density);
        
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
            
            if (val < 160 && mLcdDensity != null) {
                val = mLcdDensity.getBuildPropLcdDensity();
                SharedPreferences.Editor p = this.getSharedPreferences(PREFS_NAME, 0).edit();
                p.putInt(LCD_DENSITY, val);
                p.commit();
            }

            mDensitySeekBarPref.setSummary(String.format(mDensitySummary, val));
            
        }
        catch (NullPointerException e) {
            // do nothing
            if (Log.LOGV)
                Log.v(TAG + "loadSharedPreferences() nullpointer " + e.getLocalizedMessage().toString());
        }
    }

    /**
     * showRebootDialog() if success on setting the lcd density popup dialog to
     * reboot.
     * 
     * @para m success
     */
    public static void showRebootDialag(boolean success) {
        if (!success) {
            Toast.makeText(mContext, R.string.lcd_density_change_failure, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.lcd_density_dialog_title_reboot)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.lcd_density_reboot_required)
                .setPositiveButton(R.string.dialog_btn_reboot,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CommandsHelper.reboot();
                            }
                        }).setNegativeButton(R.string.dialog_btn_cancel, null).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(LCD_DENSITY)) {
            if (Log.LOGV)
                Log.v(TAG + "seekbarpref changed " + sharedPreferences.getInt(key, -1));
            int val = sharedPreferences.getInt(LCD_DENSITY, -1);
            if (val < 160) 
                val = 160;
            
            mDensitySeekBarPref.setSummary(String.format(mDensitySummary,val));
            prepareLcdDensityChange(sharedPreferences.getInt(key, -1));
        }
    }

    private void prepareLcdDensityChange(int density) {

        if (mLcdDensity == null)
            return;
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        String str = mContext.getResources().getString(R.string.setting_lcd_density_progress);
        mProgressDialog.setMessage(str);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        

        mLcdDensity.setPhoneDensity(density);
        mProgressDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                
                showRebootDialog();
            }
            
        });
    }
    
    public static void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }
    
    private void showRebootDialog() {
        boolean isSuccess = false;
        
        if (mLcdDensity != null)
            isSuccess = mLcdDensity.getIsSuccess();
        
        if (!isSuccess) {
            Toast.makeText(this, R.string.lcd_density_change_failure, Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this).setTitle(R.string.lcd_density_dialog_title_reboot)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(R.string.lcd_density_reboot_required)
        .setPositiveButton(R.string.dialog_btn_reboot, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CommandsHelper.reboot();
                dialog.dismiss();
            }   
        }).setNegativeButton(R.string.dialog_btn_cancel, null).show();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

}
