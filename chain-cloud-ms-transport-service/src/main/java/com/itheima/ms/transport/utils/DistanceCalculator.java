package com.itheima.ms.transport.utils;

/**
 * @author zzj
 * @version 1.0
 */
public class DistanceCalculator {

    private static final double EARTH_RADIUS = 6371.0;

    public static double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lng1Rad = Math.toRadians(lng1);
        double lng2Rad = Math.toRadians(lng2);
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLng = lng2Rad - lng1Rad;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public static void main(String[] args) {
        double beijingLng = 116.4074;
        double beijingLat = 39.9042;
        double shanghaiLng = 121.4737;
        double shanghaiLat = 31.2304;
        double distance = calculateDistance(beijingLng, beijingLat, shanghaiLng, shanghaiLat);
        System.out.println("北京到上海的直线距离：" + String.format("%.2f", distance) + " 公里");
    }
}
