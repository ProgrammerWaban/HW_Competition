package com.huawei.codecraft;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Workbench {
    private int ID;
    private double x = 50;
    private double y = 50;
    //二维数组地图的下标
    private int xMap;
    private int yMap;
    private int remain_frame;
    private int raw = -1;
    private int raw_old = -1;
    private int product_status = -1;
    private int product_status_old = -1;
    //收购的产品号
    private Set<Integer> needs = new HashSet();
    //饥饿帧
    private int hungryFrame = 0;
    //工作台是否存在
    private boolean isAlive = true;
    //无商品，记录到地图任意一点的距离
    private double[][] distMatWithNoGood;
    //有商品，记录到地图任意一点的距离
    private double[][] distMatWithGood;

    public Workbench() {
    }

    public Workbench(int ID, double x, double y, int remain_frame, int raw, int product_status) {
        this.ID = ID;
        this.x = x;
        this.y = y;
        this.remain_frame = remain_frame;
        this.raw = raw;
        this.product_status = product_status;
        switch (ID) {
            case 1:
            case 2:
            case 3:
                break;
            case 4:
                needs.add(1);
                needs.add(2);
                break;
            case 5:
                needs.add(1);
                needs.add(3);
                break;
            case 6:
                needs.add(2);
                needs.add(3);
                break;
            case 7:
                needs.add(4);
                needs.add(5);
                needs.add(6);
                break;
            case 8:
                needs.add(7);
                break;
            case 9:
                needs.add(1);
                needs.add(2);
                needs.add(3);
                needs.add(4);
                needs.add(5);
                needs.add(6);
                needs.add(7);
        }

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getRemain_frame() {
        return remain_frame;
    }

    public void setRemain_frame(int remain_frame) {
        this.remain_frame = remain_frame;
    }

    public int getRaw() {
        return raw;
    }

    public void setRaw(int raw) {
        this.raw = raw;
    }

    public int getProduct_status() {
        return product_status;
    }

    public void setProduct_status(int product_status) {
        this.product_status = product_status;
    }

    public Set<Integer> getNeeds() {
        return needs;
    }

    public void setNeeds() {
        switch (ID) {
            case 1:
            case 2:
            case 3:
                break;
            case 4:
                needs.add(1);
                needs.add(2);
                break;
            case 5:
                needs.add(1);
                needs.add(3);
                break;
            case 6:
                needs.add(2);
                needs.add(3);
                break;
            case 7:
                needs.add(4);
                needs.add(5);
                needs.add(6);
                break;
            case 8:
                needs.add(7);
                break;
            case 9:
                needs.add(1);
                needs.add(2);
                needs.add(3);
                needs.add(4);
                needs.add(5);
                needs.add(6);
                needs.add(7);
        }
    }

    public int getHungryFrame() {
        return hungryFrame;
    }

    public void setHungryFrame(int hungryFrame) {
        this.hungryFrame = hungryFrame;
    }

    public int getRaw_old() {
        return raw_old;
    }

    public void setRaw_old(int raw_old) {
        this.raw_old = raw_old;
    }

    public int getProduct_status_old() {
        return product_status_old;
    }

    public void setProduct_status_old(int product_status_old) {
        this.product_status_old = product_status_old;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getxMap() {
        return xMap;
    }

    public void setxMap(int xMap) {
        this.xMap = xMap;
    }

    public int getyMap() {
        return yMap;
    }

    public void setyMap(int yMap) {
        this.yMap = yMap;
    }

    public double[][] getDistMatWithNoGood() {
        return distMatWithNoGood;
    }

    public void setDistMatWithNoGood(double[][] distMatWithNoGood) {
        this.distMatWithNoGood = distMatWithNoGood;
    }

    public double[][] getDistMatWithGood() {
        return distMatWithGood;
    }

    public void setDistMatWithGood(double[][] distMatWithGood) {
        this.distMatWithGood = distMatWithGood;
    }

    public void changeRawWithGoodID(int goodID){
        List<Integer> rawList = Tool.changeRawToList(raw);
        rawList.add(goodID);
        //如果原材料格满了，并且能够生产，那么就清0
        if(rawList.size() == needs.size() && remain_frame == -1){
            raw = 0;
        }else{
            int newRaw = 0;
            for(Integer n : rawList){
                newRaw += (int)Math.pow(2, n);
            }
            raw = newRaw;
        }
    }
}
