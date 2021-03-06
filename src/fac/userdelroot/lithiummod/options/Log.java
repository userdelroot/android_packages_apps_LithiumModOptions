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

/** 
 * Helper class for logging and debugging
 * @see android.util.Log
 */
final class Log {
	public final static String LOGTAG = "LithiumModOptions";

	static final boolean LOGV = false;

	static final void v(String logMe) {
	    if (LOGV)
	        android.util.Log.v(LOGTAG, logMe);
	}

	static final void e(String logMe) {
	    if (LOGV)
	        android.util.Log.e(LOGTAG, logMe);
	}

	static final void e(String logMe, Exception ex) {
	    if (LOGV)
	        android.util.Log.e(LOGTAG, logMe, ex);
	}

	static final void w(String logMe) {
	    if (LOGV)
	        android.util.Log.w(LOGTAG, logMe);
	}
	
	static final void i(String logMe) {
	    if (LOGV)
	        android.util.Log.i(LOGTAG, logMe);
	}
}
