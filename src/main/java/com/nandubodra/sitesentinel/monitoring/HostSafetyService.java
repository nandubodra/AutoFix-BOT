package com.nandubodra.sitesentinel.monitoring;

import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Service
public class HostSafetyService {

    public void assertSafeHost(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                throw new IllegalArgumentException("No resolved address");
            }
            for (InetAddress address : addresses) {
                assertSafeAddress(address);
            }
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Host resolution failed");
        }
    }

    void assertSafeAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() ||
                address.isSiteLocalAddress() || address.isMulticastAddress()) {
            throw new IllegalArgumentException("Unsafe destination");
        }

        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            if (first == 0 || first >= 224 ||
                    (first == 100 && (second >= 64 && second <= 127)) ||
                    (first == 169 && second == 254) ||
                    (first == 198 && (second == 18 || second == 19))) {
                throw new IllegalArgumentException("Unsafe destination");
            }
            if (Arrays.equals(bytes, new byte[]{(byte) 169, (byte) 254, (byte) 169, (byte) 254})) {
                throw new IllegalArgumentException("Unsafe destination");
            }
        }

        if (address instanceof Inet6Address inet6) {
            if (inet6.isIPv4CompatibleAddress() || inet6.isAnyLocalAddress() || inet6.isLoopbackAddress()) {
                throw new IllegalArgumentException("Unsafe destination");
            }
            byte[] b = inet6.getAddress();
            if ((b[0] & 0xff) == 0xfc || (b[0] & 0xff) == 0xfd || ((b[0] & 0xff) == 0xfe && (b[1] & 0xc0) == 0x80)) {
                throw new IllegalArgumentException("Unsafe destination");
            }
        }
    }
}
