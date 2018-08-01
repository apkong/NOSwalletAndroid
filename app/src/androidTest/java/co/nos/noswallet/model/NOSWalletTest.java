package co.nos.noswallet.model;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
@RunWith(AndroidJUnit4.class)
public class NOSWalletTest extends InstrumentationTestCase {

    @Test
    public void rawToNeuros() {
        Assert.assertEquals(NOSWallet.neurosToRaw("2"), "2000000000000000000000000000000");
    }

    @Test
    public void neurosToRaw() {
        Assert.assertEquals(NOSWallet.rawToNeuros("2000000000000000000000000000000"), "2");
    }

    @Test
    public void zeros() {
        Assert.assertEquals(NOSWallet.zeros(3), "000");
    }
}