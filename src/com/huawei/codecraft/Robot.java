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
    private double[] radar;

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

    public double[] getRadar() {
        return radar;
    }

    public void setRadar(double[] radar) {
        this.radar = radar;
    }

    //根据实际坐标返回二维数组的下标
    public int[] getMatXY(){
        int[] a = new int[2];
        a[0] = (int) Math.floor(y / 0.5);
        a[1] = (int) Math.floor(x / 0.5);
        return a;
    }

    public int[] changeMapXY(int[][] map, int[] a) {
        if (a[0] + 1 < map.length && a[1] + 1 < map[0].length && a[0] - 1 >= 0 && a[1] - 1 >= 0) {
            //如果机器人卡在一个对角线中间，则需要根据机器人中心点位置重新更改落点格子坐标
            if (map[a[0] + 1][a[1] + 1] == -1 && map[a[0] - 1][a[1] - 1] == -1) {
                if (y > a[0] * 0.5 + 0.25 || x < a[1] * 0.5 + 0.25) {
                    a[0]++;
                    a[1]--;
                } else if (y < a[0] * 0.5 + 0.25 || x > a[1] * 0.5 + 0.25) {
                    a[0]--;
                    a[1]++;
                }
            } else if (map[a[0] - 1][a[1] + 1] == -1 && map[a[0] + 1][a[1] - 1] == -1) {
                if (y > a[0] * 0.5 + 0.25 && x > a[1] * 0.5 + 0.25) {
                    a[0]++;
                    a[1]++;
                } else if (y < a[0] * 0.5 + 0.25 && x < a[1] * 0.5 + 0.25) {
                    a[0]--;
                    a[1]--;
                }
            }
        }
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
                //出售物品后修改机器人手上的商品ID
                goodID = 0;

//                //计算卖的总金额
//                int[] value = new int[]{0, 3000, 3200, 3400, 7100, 7800, 8300, 29000};
//                Main.money.set(Main.money.get() + value[goodID]);
            }
        }
    }

    //根据激光雷达来更新地图（只取机器人朝向的扇形区域（40度））
    public void radarCheck(int[][] map_clone, int[][] map, ArrayList<Robot> robots) {

        for (int i = 0; i < 170; i = i + 4) {
            double angle_willAdd = Math.toRadians(i);
            singleLineOfRadarIsRobot(map_clone, map, robots, intendAngle + angle_willAdd, radar[i]);
            singleLineOfRadarIsRobot(map_clone, map, robots, intendAngle - angle_willAdd, radar[360 - 1 - i]);
        }

    }

    //用于判断在一条线上是否有障碍物，进而来更新地图
    private void singleLineOfRadarIsRobot(int[][] map_clone, int[][] map, ArrayList<Robot> robots, double theta, double distance) {
        double x_add = x + (distance + 0.1) * Math.cos(theta);
        double y_add = y + (distance + 0.1) * Math.sin(theta);
        int mapIndex0 = (int) (y_add / 0.5);
        int mapIndex1 = (int) (x_add / 0.5);
        if (mapIndex0 < 1 || mapIndex0 > 98 || mapIndex1 < 1 || mapIndex1 > 98) return;
        if (isBarrierAround(mapIndex0, mapIndex1, map)) {
            //return false; // 表示雷达遍历到的是原生障碍物墙,不做处理
        } else {
            if (!isTeammate(robots, mapIndex0, mapIndex1)) {
                map_clone[mapIndex0][mapIndex1] = -1;
                //System.err.println("前方有机器人，更改地图");
                //return true;   //表示遍历到的是机器人（对面方的）
            }
            //return false; //对于友方机器人不做处理，直接忽略
        }
    }

    //用于判断雷达路径上的不是本方机器人
    private boolean isTeammate(ArrayList<Robot> robots, int mapIndex0, int mapIndex1) {
        int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for (int i = 0; i < direction.length; i++) {
            for (Robot robot : robots) {
                if (robot.getX() == x && robot.getY() == y) {
                    continue;
                }

                int tmp_x = robot.getMatXY()[0] + direction[i][0];
                int tmp_y = robot.getMatXY()[1] + direction[i][1];
                if (tmp_x < 0 || tmp_y < 0 || tmp_x > 99 || tmp_y > 99) continue;
                if (mapIndex0 == tmp_x && mapIndex1 == tmp_y) {
                    return true;  //表示想更新障碍物的地方周围存在友军
                }
            }
        }
        return false; //没有友军存在

        //这套逻辑看来行不通（当机器人贴的很近的时候）
//        for (Robot robot : robots) {
//            //排除掉自己
//            if (robot.getX() == x && robot.getY() == y) {
//                continue;
//            }
//
//            double angle = Tool.calAngle(x, y, robot.getX(), robot.getY());
//            theta = theta < 0 ? theta + 2 * Math.PI : theta;
//            //机器人得在雷达所遍历的那个5度角上
//            if (Math.abs(angle - theta) < 1) {
//                double robotDistance = Tool.calDistanceByXY(x, y, robot.getX(), robot.getY());
//                if (robotDistance < distance + 1 && robotDistance > distance - 1) {
//                    //则表示雷达弹照到的是本方机器人
//                    return true;
//                }
//            }
//        }
//        //所有机器人都不符合条件则表示弹照到的是对面机器人
//        return false;
    }

    //判断一个点周围的点是否存在障碍物（缩小这个雷达计算误差）
    private boolean isBarrierAround(int nowY, int nowX, int[][] map) {
        int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for (int i = 0; i < direction.length; i++) {
            int tmp_x = nowY + direction[i][0];
            int tmp_y = nowX + direction[i][1];
            if (tmp_y == getMatXY()[1] && tmp_x == getMatXY()[0]) continue;  //如果遍历到了自己当然直接pass
            if (map[tmp_x][tmp_y] == -1) return true;
        }
        return false;
    }
}