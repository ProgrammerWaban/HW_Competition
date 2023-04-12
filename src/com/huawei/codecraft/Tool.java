package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.List;

public class Tool {
    private Tool() {
    }
    //求弧度差 以第一个点为基准
    public static double calAngle(double x1, double y1, double x2, double y2){
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;
        double angle = Math.atan2(yDiff, xDiff);
        return angle < 0 ? angle + 2 * Math.PI : angle;
    }

    //通过整数判断拥有的物品号
    public static List<Integer> changeRawToList(Integer a) {
        List<Integer> list = new ArrayList<>();
        int i = 0;
        while (a > 0) {
            if (a % 2 != 0) list.add(i);
            i++;
            a /= 2;
        }
        return list;
    }

    //根据坐标计算距离
    public static double calDistanceByXY(double x1, double y1, double x2, double y2){
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return distance;
    }

    //2个list求差集 A-B
    public static List<Integer> calDifferenceWithABList(List<Integer> A, List<Integer> B){
        if(B == null)   return A;
        List<Integer> list = new ArrayList<>();
        for(Integer a : A){
            if(!B.contains(a))  list.add(a);
        }
        return list;
    }

    //交换list里的i,j的值
    public static void changeListValueByAB(List<Integer> list, int A, int B){
        int temp = list.get(A);
        list.set(A, list.get(B));
        list.set(B, temp);
    }
}
