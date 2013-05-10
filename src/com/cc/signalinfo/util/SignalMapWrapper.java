/*
 *
 * Copyright (c) 2013 Wes Lanning, http://codingcreation.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * http://www.opensource.org/licenses/mit-license.php
 * /
 */

package com.cc.signalinfo.util;

import android.telephony.TelephonyManager;
import android.util.Log;
import com.cc.signalinfo.config.AppSetup;
import com.cc.signalinfo.enums.NetworkType;
import com.cc.signalinfo.enums.Signal;
import com.cc.signalinfo.signals.CdmaInfo;
import com.cc.signalinfo.signals.GsmInfo;
import com.cc.signalinfo.signals.ISignal;
import com.cc.signalinfo.signals.LteInfo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Wes Lanning
 * @version 2013-04-29
 */
public class SignalMapWrapper
{
    // filter out any readings matching this regex as they're invalid
    private static final Pattern FILTER_SIGNAL = Pattern.compile("0|-1|-?99|-?[1-9][0-9]{3,}");
    private Map<NetworkType, ISignal> networkMap;

    /**
     * @param filteredSignalData - signal data formatted for ICS+ compatibility
     * @param tm - instance of TelephonyManager
     */
    public SignalMapWrapper(String[] filteredSignalData, TelephonyManager tm)
    {
        networkMap = createSignalDataMap(tm, filteredSignalData);
    }

    /**
     * Initialize the network map that will hold all the various signal readings
     *
     * @param tm - dependency for the network map
     * @return the created map, empty other than the signal container maps (for gsm, lte, etc)
     */
    private static Map<NetworkType, ISignal> initNetworkMap(TelephonyManager tm)
    {
        Map<NetworkType, ISignal> networkMap = new EnumMap<>(NetworkType.class);
        networkMap.put(NetworkType.GSM, new GsmInfo(tm));
        networkMap.put(NetworkType.CDMA, new CdmaInfo(tm));
        networkMap.put(NetworkType.LTE, new LteInfo(tm));
        return networkMap;
    }

    /**
     * Do we already have signal data entered from the system?
     *
     * @return true if we already have at least one signal data reading collected.
     */
    public boolean hasData()
    {
        return !networkMap.isEmpty() && networkMap.entrySet().iterator().hasNext();
    }

    /**
     * Returns an unmodifiable copy of the network signal info map
     *
     * @return network signal info organized by gsm, cdma, lte, etc
     */
    public Map<NetworkType, ISignal> getNetworkMap()
    {
        return Collections.unmodifiableMap(networkMap);
    }

    /**
     * Removes any crap that might show weird numbers because the phone does not support
     * some reading or avoids causing an exception by removing it.
     *
     * @param data - data to filter
     * @param tm - dependency for the network map
     * @return filtered data with "n/a" instead of the bad value
     */
    private Map<NetworkType, ISignal> createSignalDataMap(TelephonyManager tm, String[] data)
    {
        Map<NetworkType, ISignal> networkMap = initNetworkMap(tm);
        Signal[] values = Signal.values();

        for (int i = 0; i < values.length; ++i) {
            String signalValue = i < data.length
                ? data[i] :
                AppSetup.DEFAULT_TXT;
            networkMap.get(values[i].type()).addSignalValue(values[i], signalValue);
        }
        Log.d("Signal Map CDMA: ", networkMap.get(NetworkType.CDMA).getSignals().toString());
        Log.d("Signal Map GSM: ", networkMap.get(NetworkType.GSM).getSignals().toString());
        Log.d("Signal Map LTE: ", networkMap.get(NetworkType.LTE).getSignals().toString());
        return networkMap;
    }
}