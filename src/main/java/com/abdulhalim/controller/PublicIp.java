package com.abdulhalim.controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.*;

@RestController
public class PublicIp {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String[] IPV4_SERVICES = {
            "http://checkip.amazonaws.com/",
            "https://ipv4.icanhazip.com/",
            "http://bot.whatismyipaddress.com/"
            // and so on ...
    };

    public static String get() throws ExecutionException, InterruptedException {
        List<Callable<String>> callables = new ArrayList<>();
        for (String ipService : IPV4_SERVICES) {
            callables.add(() -> get(ipService));
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            return executorService.invokeAny(callables);
        } finally {
            executorService.shutdown();
        }
    }

    private static String get(String url) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String ip = in.readLine();
            if (IPV4_PATTERN.matcher(ip).matches()) {
                return ip;
            } else {
                throw new IOException("invalid IPv4 address: " + ip);
            }
        }
    }


    @GetMapping("/")
    public String showIp() throws UnknownHostException, ExecutionException, InterruptedException {
        InetAddress ip = InetAddress.getLocalHost();

        String localIp = ip.getHostAddress();
        System.out.println("Local IP: "+localIp);

        String publicIp = PublicIp.get();
        System.out.println("Public IP: " + publicIp);
        String ipString = "Local IP: " + localIp + " Public IP: "+publicIp;
        return ipString;
    }

}
