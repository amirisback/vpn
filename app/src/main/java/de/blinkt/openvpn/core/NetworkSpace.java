package de.blinkt.openvpn.core;

import android.os.Build;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.Collection;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Vector;

public class NetworkSpace {

    TreeSet<ipAddress> mIpAddresses = new TreeSet<ipAddress>();

    public Collection<ipAddress> getNetworks(boolean included) {
        Vector<ipAddress> ips = new Vector<ipAddress>();
        for (ipAddress ip : mIpAddresses) {
            if (ip.included == included)
                ips.add(ip);
        }
        return ips;
    }

    public void clear() {
        mIpAddresses.clear();
    }

    void addIP(CIDRIP cidrIp, boolean include) {

        mIpAddresses.add(new ipAddress(cidrIp, include));
    }

    public void addIPSplit(CIDRIP cidrIp, boolean include) {
        ipAddress newIP = new ipAddress(cidrIp, include);
        ipAddress[] splitIps = newIP.split();
        for (ipAddress split : splitIps)
            mIpAddresses.add(split);
    }

    void addIPv6(Inet6Address address, int mask, boolean included) {
        mIpAddresses.add(new ipAddress(address, mask, included));
    }

    TreeSet<ipAddress> generateIPList() {

        PriorityQueue<ipAddress> networks = new PriorityQueue<ipAddress>(mIpAddresses);

        TreeSet<ipAddress> ipsDone = new TreeSet<ipAddress>();

        ipAddress currentNet = networks.poll();
        if (currentNet == null)
            return ipsDone;

        while (currentNet != null) {

            ipAddress nextNet = networks.poll();

//            if (BuildConfig.DEBUG) Assert.assertNotNull(currentNet);
            if (nextNet == null || currentNet.getLastAddress().compareTo(nextNet.getFirstAddress()) == -1) {

                ipsDone.add(currentNet);

                currentNet = nextNet;
            } else {

                if (currentNet.getFirstAddress().equals(nextNet.getFirstAddress()) && currentNet.networkMask >= nextNet.networkMask) {
                    if (currentNet.included == nextNet.included) {


                        currentNet = nextNet;
                    } else {

                        ipAddress[] newNets = nextNet.split();


                        if (!networks.contains(newNets[1]))
                            networks.add(newNets[1]);

                        if (newNets[0].getLastAddress().equals(currentNet.getLastAddress())) {
//                            if (BuildConfig.DEBUG)
//                                Assert.assertEquals(newNets[0].networkMask, currentNet.networkMask);
//
                        } else {
                            if (!networks.contains(newNets[0]))
                                networks.add(newNets[0]);
                        }

                    }
                } else {
//                    if (BuildConfig.DEBUG) {
//                        Assert.assertTrue(currentNet.networkMask < nextNet.networkMask);
//                        Assert.assertTrue(nextNet.getFirstAddress().compareTo(currentNet.getFirstAddress()) == 1);
//                        Assert.assertTrue(currentNet.getLastAddress().compareTo(nextNet.getLastAddress()) != -1);
//                    }


                    if (currentNet.included == nextNet.included) {


                    } else {

                        ipAddress[] newNets = currentNet.split();


                        if (newNets[1].networkMask == nextNet.networkMask) {
//                            if (BuildConfig.DEBUG) {
//                                Assert.assertTrue(newNets[1].getFirstAddress().equals(nextNet.getFirstAddress()));
//                                Assert.assertTrue(newNets[1].getLastAddress().equals(currentNet.getLastAddress()));
//
//                            }
                            networks.add(nextNet);
                        } else {

                            networks.add(newNets[1]);
                            networks.add(nextNet);
                        }
                        currentNet = newNets[0];

                    }
                }
            }

        }

        return ipsDone;
    }

    Collection<ipAddress> getPositiveIPList() {
        TreeSet<ipAddress> ipsSorted = generateIPList();

        Vector<ipAddress> ips = new Vector<ipAddress>();
        for (ipAddress ia : ipsSorted) {
            if (ia.included)
                ips.add(ia);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {


            for (ipAddress origIp : mIpAddresses) {
                if (!origIp.included)
                    continue;

                if (ipsSorted.contains(origIp))
                    continue;

                boolean skipIp = false;

                for (ipAddress calculatedIp : ipsSorted) {
                    if (!calculatedIp.included && origIp.containsNet(calculatedIp)) {
                        skipIp = true;
                        break;
                    }
                }
                if (skipIp)
                    continue;
                ips.add(origIp);
            }
        }
        return ips;
    }

    static class ipAddress implements Comparable<ipAddress> {
        public int networkMask;
        private BigInteger netAddress;
        private boolean included;
        private boolean isV4;
        private BigInteger firstAddress;
        private BigInteger lastAddress;

        public ipAddress(CIDRIP ip, boolean include) {
            included = include;
            netAddress = BigInteger.valueOf(ip.getInt());
            networkMask = ip.len;
            isV4 = true;
        }


        public ipAddress(Inet6Address address, int mask, boolean include) {
            networkMask = mask;
            included = include;

            int s = 128;

            netAddress = BigInteger.ZERO;
            for (byte b : address.getAddress()) {
                s -= 8;
                netAddress = netAddress.add(BigInteger.valueOf((b & 0xFF)).shiftLeft(s));
            }
        }

        ipAddress(BigInteger baseAddress, int mask, boolean included, boolean isV4) {
            this.netAddress = baseAddress;
            this.networkMask = mask;
            this.included = included;
            this.isV4 = isV4;
        }

        @Override
        public int compareTo(@NonNull ipAddress another) {
            int comp = getFirstAddress().compareTo(another.getFirstAddress());
            if (comp != 0)
                return comp;


            if (networkMask > another.networkMask)
                return -1;
            else if (another.networkMask == networkMask)
                return 0;
            else
                return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ipAddress))
                return super.equals(o);


            ipAddress on = (ipAddress) o;
            return (networkMask == on.networkMask) && on.getFirstAddress().equals(getFirstAddress());
        }

        public BigInteger getLastAddress() {
            if (lastAddress == null)
                lastAddress = getMaskedAddress(true);
            return lastAddress;
        }

        public BigInteger getFirstAddress() {
            if (firstAddress == null)
                firstAddress = getMaskedAddress(false);
            return firstAddress;
        }

        private BigInteger getMaskedAddress(boolean one) {
            BigInteger numAddress = netAddress;

            int numBits;
            if (isV4) {
                numBits = 32 - networkMask;
            } else {
                numBits = 128 - networkMask;
            }

            for (int i = 0; i < numBits; i++) {
                if (one)
                    numAddress = numAddress.setBit(i);
                else
                    numAddress = numAddress.clearBit(i);
            }
            return numAddress;
        }

        @Override
        public String toString() {

            if (isV4)
                return String.format(Locale.US, "%s/%d", getIPv4Address(), networkMask);
            else
                return String.format(Locale.US, "%s/%d", getIPv6Address(), networkMask);
        }

        public ipAddress[] split() {
            ipAddress firstHalf = new ipAddress(getFirstAddress(), networkMask + 1, included, isV4);
            ipAddress secondHalf = new ipAddress(firstHalf.getLastAddress().add(BigInteger.ONE), networkMask + 1, included, isV4);
//            if (BuildConfig.DEBUG)
//                Assert.assertTrue(secondHalf.getLastAddress().equals(getLastAddress()));
            return new ipAddress[]{firstHalf, secondHalf};
        }

        String getIPv4Address() {
//            if (BuildConfig.DEBUG) {
//                Assert.assertTrue(isV4);
//                Assert.assertTrue(netAddress.longValue() <= 0xffffffffl);
//                Assert.assertTrue(netAddress.longValue() >= 0);
//            }
            long ip = netAddress.longValue();
            return String.format(Locale.US, "%d.%d.%d.%d", (ip >> 24) % 256, (ip >> 16) % 256, (ip >> 8) % 256, ip % 256);
        }

        String getIPv6Address() {
//            if (BuildConfig.DEBUG) Assert.assertTrue(!isV4);
            BigInteger r = netAddress;

            String ipv6str = null;
            boolean lastPart = true;

            while (r.compareTo(BigInteger.ZERO) == 1) {

                long part = r.mod(BigInteger.valueOf(0x10000)).longValue();
                if (ipv6str != null || part != 0) {
                    if (ipv6str == null && !lastPart)
                        ipv6str = ":";

                    if (lastPart)
                        ipv6str = String.format(Locale.US, "%x", part, ipv6str);
                    else
                        ipv6str = String.format(Locale.US, "%x:%s", part, ipv6str);
                }

                r = r.shiftRight(16);
                lastPart = false;
            }
            if (ipv6str == null)
                return "::";


            return ipv6str;
        }

        public boolean containsNet(ipAddress network) {

            BigInteger ourFirst = getFirstAddress();
            BigInteger ourLast = getLastAddress();
            BigInteger netFirst = network.getFirstAddress();
            BigInteger netLast = network.getLastAddress();

            boolean a = ourFirst.compareTo(netFirst) != 1;
            boolean b = ourLast.compareTo(netLast) != -1;
            return a && b;

        }
    }

}