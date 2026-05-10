package business.shared;

public class TrackingService {
    public static String generate(String prefix) {
        return prefix + "-" + (int)(Math.random() * 100000);
    }
}