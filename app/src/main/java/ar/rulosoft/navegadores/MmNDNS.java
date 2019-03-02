package ar.rulosoft.navegadores;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by Raúl on 15/05/2018.
 * saved only jic
 */
public class MmNDNS implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> list = new ArrayList<>();
        if (hostname.toLowerCase().contains("kkkk.com")) {
            list.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 0}));
        } else if (hostname.toLowerCase().contains("rrrrr.to")) {
            list.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 127, (byte) 0, (byte) 0, (byte) 1}));
        } else {
            list = SYSTEM.lookup(hostname);
        }
        return list;
    }
}
