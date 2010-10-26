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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/**
 * SeekbarPref used to set the density
 * @author userdelroot
 * Oct 25, 2010
 */
public class SeekBarPref extends DialogPreference implements View.OnKeyListener {

    private static final String TAG = "SeekBarPref ";

    public static ProgressDialog mProgressDialog;
    
    private Drawable mMyIcon;

    private TextView mTextView;

    private int mProgress;

    private static int mSeekbar_Max;

    private final int mSeekbar_Min = 160;
    
    private int mSeekbar_Diff;

    private static int mDefaultVal;

    private static int mDensity;

    private SeekBar mSeekBar;
    private static final String PREFS_NAME = "fac.userdelroot.lithiummod.options_preferences";
    private static final String LCD_DENSITY = "lcd_density";
    
    public SeekBarPref(Context context, AttributeSet attrs) {
        super(context, attrs);

        
        setDialogLayoutResource(R.layout.seekbar_pref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);      

    }
    
    private void loadSharedPrefs() {
        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
            
            mDefaultVal = prefs.getInt(LCD_DENSITY, -1);
        }
        catch (Exception e) {
            // do nothing
        }
    }

    @Override
    protected void onBindDialogView(View view) {

        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        LCDDensity density = new LCDDensity();
        
        mTextView = (TextView) view.findViewById(R.id.updatetext);

        mSeekBar = getSeekBar(view);
        
        loadSharedPrefs();
        mSeekbar_Max = Integer.valueOf(density.getDefaultDensity());
        
        
        // max density
        if (mSeekbar_Max <= 0) 
            mSeekbar_Max = 240;
        
        // minimum density
        if (mDefaultVal < 160) // we were never set
            mDefaultVal = mSeekbar_Min;

        // diff the density for scaling
        mSeekbar_Diff = mSeekbar_Max - mSeekbar_Min;
        
        // Steal the XML dialogIcon attribute's value
        mMyIcon = getDialogIcon();
        setDialogIcon(null);

        // save off for later
        mProgress = mDefaultVal;

        if (mMyIcon != null) {
            iconView.setImageDrawable(mMyIcon);
        } else {
            iconView.setVisibility(View.GONE);
        }

        if (mSeekBar == null)
            return;

        mSeekBar.setMax(mSeekbar_Diff);

        // scale it
       double scale = (((double)mDefaultVal - (double)mSeekbar_Min) / ((double)mSeekbar_Max - (double)mSeekbar_Min)) * (double) mSeekbar_Diff;

       mSeekBar.setProgress((int)scale);

        mTextView.setText(String.valueOf(mDefaultVal));
        mProgress = mDefaultVal;

        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        mSeekBar.setOnSeekBarChangeListener(SeekBarProgress);

    }

    OnSeekBarChangeListener SeekBarProgress = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // bail if not from userclick
            if (!fromUser)
                return;

            // if this is null baill
            if (mTextView == null)
                return;

            // convert progress to a max / min for our lcd density
            int val = ((mSeekbar_Max - mSeekbar_Min) * progress / mSeekbar_Diff) + mSeekbar_Min;

            mProgress = val;

            mTextView.setText(String.valueOf(val));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // not used
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // not used
        }

    };

    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(R.id.seekbar);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        
        if (!positiveResult) {
            mProgress = mDefaultVal;
            mDensity = mProgress;
            return;
        }        

        mDensity = mProgress;
        
       
        if (callChangeListener((Integer) mDensity)) {
            persistInt(mDensity);
            notifyChanged();
            
        }
           
    }

    /**
     * Cool little feature to use the volume keys to be more specific 
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        
        boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isdown) {
                    incrementProgressBar(-1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isdown) {
                    incrementProgressBar(+1);
                }
                return true;
            default:
                return false;
        }
    }
    
    
    /**
     * Used to increment the density using the volume button.  A lot more precise then using screen
     * @param progress
     */
    private void incrementProgressBar(int progress) {

        
        mSeekBar.incrementProgressBy(progress);
        
        int val = ((mSeekbar_Max - mSeekbar_Min) * mSeekBar.getProgress() / mSeekbar_Diff) + mSeekbar_Min;

        mProgress = val;
        
        mTextView.setText(String.valueOf(val));    
    }        
}
