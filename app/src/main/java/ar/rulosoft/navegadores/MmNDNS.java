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
        if (hostname.toLowerCase().contains("raw.githubusercontent.com")) {//Raw.githubusercontent.com
            list.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 151, (byte) 101, (byte) 4, (byte) 133}));
        } else {
            list = SYSTEM.lookup(hostname);
        }
        return list;
    }
}
