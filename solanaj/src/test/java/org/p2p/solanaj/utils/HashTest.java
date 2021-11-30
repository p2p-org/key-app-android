package org.p2p.solanaj.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import org.bitcoinj.core.Base58;

public class HashTest {

    @Test
    public void sha256Test() {
        byte[] b = Hash.sha256(Base58.decode("D4Rioa1Zh1jMummwpqx8m2SkhSATN"));
        assertEquals("73hrfBgvit4TbtXpZuFe2exKDqWs5h2DFZ6keiiQizyc", Base58.encode(b));
    }

    @Test
    public void keccak256Test() {
        byte[] b = Hash.keccak256("BTC/toSolana".getBytes());
        assertEquals("2XWUS8dNzaAFeDk6e6Q4dsojE3n9jncAZ9nNBpCJWEgZ", Base58.encode(b));
    }

    @Test
    public void generateSHashTest() {
        assertEquals("2XWUS8dNzaAFeDk6e6Q4dsojE3n9jncAZ9nNBpCJWEgZ", Base58.encode(Hash.generateSHash()));
    }

    @Test
    public void generatePHashTest() {
        assertEquals("EKDHSGbrGztomDfuiV4iqiZ6LschDJPsFiXjZ83f92Md", Base58.encode(Hash.generatePHash()));
    }

    @Test
    public void generateGHashTest() {
        assertEquals("2dpw381hu88DTX3VVw78LhqvrPDfyvXyuyArqGouzNYa",
                Base58.encode(Hash.generateGHash("34cef1aee9a983b47366dddb37f5327263737f3551cf4ce30125668c41331a80",
                        "16ac6fb8b800ff9e24220479d69d38b59a077966f500c7bbd3435dad78d8fc02",
                        Base58.decode("3AQTaduKvYWFTu1ExZSQK1hQp5jSZ2yEt4KzsASghu2T"))));
    }

    @Test
    public void generateNHashTest() {
        assertEquals("vAOtS_KYooAdS8u0RDJ1ANa-HV1w4vCZSgcrE62GA6U",
                Utils.toURLBase64(Hash.generateNHash(Base58.decode("3AQTaduKvYWFTu1ExZSQK1hQp5jSZ2yEt4KzsASghu3E"),
                        Base58.decode("3r2qaGgK1Pvj6ExUqC91QexvFyAXzWA9P3WDPwAMW8me"), "0")));
    }

}
