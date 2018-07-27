package ar.rulosoft.navegadores;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by Raúl on 15/05/2018.
 */
public class MmNDNS implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> list = new ArrayList<>();
        if (hostname.toLowerCase().contains("kissmanga.com")) {
            list.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 87, (byte) 121, (byte) 98, (byte) 205}));
       // } else if (hostname.toLowerCase().contains("readcomiconline.to")) {
       //    list.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 51, (byte) 15, (byte) 204, (byte) 213}));
        } else {
            list = SYSTEM.lookup(hostname);
        }
        return list;
    }
}
