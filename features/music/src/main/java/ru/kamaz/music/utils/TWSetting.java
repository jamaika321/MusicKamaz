package ru.kamaz.music.utils;

import android.tw.john.*;

public class TWSetting extends TWUtil {
    private static int mCount; private static TWSetting mTW;
    static { TWSetting.mTW = new TWSetting(17); TWSetting.mCount = 0; }
    public TWSetting(final int n) { super(n); }

    public static TWSetting open() {
        final int mCount = TWSetting.mCount; TWSetting.mCount = mCount + 1;
        if (mCount == 0) { final TWSetting mtw = TWSetting.mTW; final short[] array2;
            final short[] array = array2 = new short[22]; array2[0] = 257;
            array2[1] = 258; array2[2] = 259; array2[3] = 260; array2[4] = 262;
            array2[5] = 264; array2[6] = 265; array2[7] = 266; array2[8] = 267;
            array2[9] = 272; array2[10] = 274; array2[11] = 513; array2[12] = 516;
            array2[13] = 1025; array2[14] = 1026; array2[15] = 1028; array2[16] = 1029;
            array2[17] = 1030; array2[18] = 1539; array2[19] = 1541; array2[20] = 276;
            array2[21] = -25057; if (mtw.open(array) != 0) {  --TWSetting.mCount; return null; }
            TWSetting.mTW.start(); } return TWSetting.mTW;
    }

    public void close() { final int mCount = TWSetting.mCount; if (mCount > 0 && (TWSetting.mCount = mCount - 1) == 0) { this.stop(); super.close(); } }
}
