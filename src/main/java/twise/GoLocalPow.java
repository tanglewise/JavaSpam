package twise;

import jota.IotaLocalPoW;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import twise.ObjectWrapper;
import java.io.File;

public class GoLocalPow implements IotaLocalPoW{

    private static final ObjectWrapper powTrytes = new ObjectWrapper(null);
    private static final ObjectWrapper powSuccess = new ObjectWrapper(false);
    private static final ObjectWrapper scannerOpen = new ObjectWrapper(false);

    public static final String powFileName = "pow";
    private static GoLocalPow goLocalPow;
    private int num_threads = 1;

    public void init() {
        goLocalPow = this;
        goLocalPow.start(num_threads);
    }

    @Override
    public String performPoW(String trytes, int minWeightMagnitude) {
        long pow_start = System.currentTimeMillis();
        String trytes_with_nonce = goPow(trytes);
        double pow_time = (double) (System.currentTimeMillis() - pow_start) / 1000;
        System.out.println("Go PoW took " + String.valueOf(pow_time) + " seconds.");
        return trytes_with_nonce;
    }

    public static String goPow(String preparedTrytes) {
        powTrytes.o = preparedTrytes;
        powSuccess.o = false;

        synchronized (powTrytes) {
            powTrytes.notify();
            try { powTrytes.wait(); } catch (InterruptedException e) { }
        }
//	    System.out.println("pow trytes: " + (String) powTrytes.o);
        return (boolean) powSuccess.o ? (String) powTrytes.o : null;
    }

    public static void start(int threads) {
	    File goPowFile = new File(powFileName);
        boolean goPowAvailable = goPowFile.exists();
        if (!goPowAvailable) {
            System.out.println("PoW go file not detected!");
            return;
        }

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("sudo ./"+powFileName);
        } catch (IOException e) {
            System.out.println("IO Exception when running go PoW: " + e.getMessage());
        }

        InputStream in = proc.getInputStream();
        final OutputStream out = proc.getOutputStream();
        final Scanner scanner = new Scanner(in);
        scannerOpen.o = true;
        scanner.useDelimiter("\n");
	    threads = Math.min(Math.max(1, threads), Runtime.getRuntime().availableProcessors());
        int MWM = 14;
        try {
                out.write((threads + "\n" + MWM + "\n").getBytes());
                out.flush();
            } catch (IOException e) {
                System.out.println("Error while writing to OutputStream: " + e.getMessage());
            }

        powTrytes.o = "";
        Thread powThread = new Thread(Main.SUPER_THREAD, "GOldDiggerLocalPoW") {
            public void run() {
                while(true) {
                    synchronized (powTrytes) { try { powTrytes.wait(); } catch (InterruptedException e) { break; } }
                    try {
                        out.write((powTrytes.o+"\n").getBytes());
                        out.flush();
                        powTrytes.o = scanner.hasNext() ? scanner.next().replace("\n", "") : null;
                        powSuccess.o = true;
                    } catch (IOException | IllegalStateException e) {
                        System.out.println("Go PoW failed: " + e.getMessage());
                    }
                    synchronized (powTrytes) { powTrytes.notify(); }
                }
            }
        };
        powThread.start();
    }
}
