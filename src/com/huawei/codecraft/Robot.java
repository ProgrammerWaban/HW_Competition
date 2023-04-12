package com.huawei.codecraft;

import java.util.ArrayList;

public class Robot {
    private int workStationID;
    private int goodID;
    private double timeCoef;
    private double collideCoef;
    private double rotate;
    private double lineSpend_x;
    private double lineSpend_y;
    private double intendAngle;
    private double x;
    private double y;

    public Robot(int workStationID, int goodID, double timeCoef, double collideCoef, double rotate, double lineSpend_x, double lineSpend_y, double intendAngle, double x, double y) {
        this.workStationID = workStationID;
        this.goodID = goodID;
        this.timeCoef = timeCoef;
        this.collideCoef = collideCoef;
        this.rotate = rotate;
        this.lineSpend_x = lineSpend_x;
        this.lineSpend_y = lineSpend_y;
        this.intendAngle = intendAngle;
        this.x = x;
        this.y = y;
    }

    public int getWorkStationID() {
        return workStationID;
    }

    public void setWorkStationID(int workStationID) {
        this.workStationID = workStationID;
    }

    public int getGoodID() {
        return goodID;
    }

    public void setGoodID(int goodID) {
        this.goodID = goodID;
    }

    public double getTimeCoef() {
        return timeCoef;
    }

    public void setTimeCoef(double timeCoef) {
        this.timeCoef = timeCoef;
    }

    public double getCollideCoef() {
        return collideCoef;
    }

    public void setCollideCoef(double collideCoef) {
        this.collideCoef = collideCoef;
    }

    public double getRotate() {
        return rotate;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    public double getLineSpend_x() {
        return lineSpend_x;
    }

    public void setLineSpend_x(double lineSpend_x) {
        this.lineSpend_x = lineSpend_x;
    }

    public double getLineSpend_y() {
        return lineSpend_y;
    }

    public void setLineSpend_y(double lineSpend_y) {
        this.lineSpend_y = lineSpend_y;
    }

    public double getIntendAngle() {
        return intendAngle;
    }

    public void setIntendAngle(double intendAngle) {
        this.intendAngle = intendAngle;
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

    //根据实际坐标返回二维数组的下标
    public int[] getMatXY(){
        int[] a = new int[2];
        a[0] = (int) Math.floor(y / 0.5);
        a[1] = (int) Math.floor(x / 0.5);
        return a;
    }

    //往目的地所需设置的前进速度和旋转速度
    private double wantForward = 1;
    private double wantRotate = 0.2;

    public double getWantForward() {
        return wantForward;
    }

    public void setWantForward(double wantForward) {
        this.wantForward = wantForward;
    }

    public double getWantRotate() {
        return wantRotate;
    }

    public void setWantRotate(double wantRotate) {
        this.wantRotate = wantRotate;
    }

    //根据目的地求前进和旋转
    public void calForwardAndRotate(Workbench wb){
        if(wb == null){
            wantForward = 0.5;
            wantRotate = 0;
            return;
        }

        //求工作台在机器人哪个方向
        double wbAngle = Tool.calAngle(x, y, wb.getX(), wb.getY());
        //机器人朝向 置正
        double rbAngle = intendAngle < 0 ? intendAngle + 2 * Math.PI : intendAngle;
        //求弧度差
        double angleDiff = rbAngle - wbAngle;
        //设置wantRotate
        if(angleDiff == 0)  wantRotate = 0;
        if(angleDiff > 0){
            if(angleDiff > Math.PI) wantRotate = 2 * Math.PI - angleDiff;
            else    wantRotate = angleDiff * -1;
        }
        if(angleDiff < 0){
            angleDiff *= -1;    //转变成wbAngle - rbAngle
            if(angleDiff > Math.PI) wantRotate = angleDiff - 2 * Math.PI;
            else    wantRotate = angleDiff;
        }

        //设置wantForward
        wantForward = 6;
        if(Tool.calDistanceByXY(x, y, wb.getX(), wb.getY()) < 1.75 && wantRotate > Math.PI / 6){
            wantForward = 2;
        }

        wantRotate *= 3;

        if(Math.abs(angleDiff) > Math.PI / 2){
            wantForward = 1;
            wantRotate *= 3;
        }
    }

    //决定是否买卖
    private boolean sell = false;
    private boolean buy = false;
    private boolean destroy = false;

    public boolean isSell() {
        return sell;
    }

    public void setSell(boolean sell) {
        this.sell = sell;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    //检查是否到达目的地
    public void checkReachDestination(dispatchingCenter dc, int robotID, ArrayList<Workbench> workbenches){
        //目的工作台ID和next目的工作台
        int destinationID = dc.findDestinationIDByRobotID(robotID);
        int nextDestinationID = dc.findNextDestinationIDByRobotID(robotID);
        //机器人到达目的地
        if(workStationID != -1 && destinationID == workStationID){
            //当前工作台
            Workbench wb = workbenches.get(workStationID);
            //买
            if(wb.getProduct_status() == 1 && goodID == 0 && nextDestinationID != -1){
                buy = true;
                //购买物品后修改机器人手上的商品ID
                goodID = wb.getID();
                //修改目的地和next目的地
                dc.changeRobotDestinationIDByRobotID(robotID, nextDestinationID);
                dc.changeRobotNextDestinationIDByRobotID(robotID, -1);
                //买完了就释放 预定出售的工作台
                if(dc.isBookWB(destinationID))  dc.delete_setWBXWithWBID(destinationID);
            } else if(!Tool.changeRawToList(wb.getRaw()).contains(goodID) && goodID != 0 && nextDestinationID == -1){   //卖
                sell = true;
                //出售物品后修改工作台的原材料格状态
                wb.changeRawWithGoodID(goodID);
                //修改目的地和next目的地
                dc.changeRobotDestinationIDByRobotID(robotID, -1);
                dc.changeRobotNextDestinationIDByRobotID(robotID, -1);
                //卖完了就释放 预定收购的工作台
                dc.deleteWBXByGoodIDAndWBID(goodID, destinationID);
                //释放收购工作台的出售预定
                dc.delete_setWBXWithWBID(destinationID);

//                //计算卖的总金额
//                int[] value = new int[]{0, 3000, 3200, 3400, 7100, 7800, 8300, 29000};
//                Main.money.set(Main.money.get() + value[goodID]);
            }
        }
    }
}