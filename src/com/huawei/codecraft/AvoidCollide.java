package com.huawei.codecraft;

import java.util.ArrayList;

public class AvoidCollide {
    //碰撞避免算法
    //机器人的一个防止碰撞的逻辑

    //判断rob2是否在rob1的朝向所示某一扇形区域内
//    public static boolean judge_distance(Robot rob1, Robot rob2, double r, double intendAngle, double angle) {
//        //给定点与下方的机器人的夹角
//        Math.abs(intendAngle - angle)<1;
//
//    }

    //计算两个机器人之间的距离（两两之间）
    public static double cal_2_Robots_Distance(Robot rob1, Robot rob2) {
        return Math.sqrt(Math.pow(rob1.getX() - rob2.getX(), 2) + Math.pow(rob1.getY() - rob2.getY(), 2));
    }

    //计算两个机器人之间的角度是否小于一定值
    public static boolean lessAngle(Robot rob1, Robot rob2) {
        double angle1 = rob1.getIntendAngle() < 0 ? rob1.getIntendAngle() + 2 * Math.PI : rob1.getIntendAngle();
        double angle2 = rob2.getIntendAngle() < 0 ? rob2.getIntendAngle() + 2 * Math.PI : rob2.getIntendAngle();
        double diff = angle1 - angle2;
        return diff < Math.PI + 1 && diff > Math.PI - 1 || diff > -Math.PI - 1 && diff < -Math.PI + 1;
//        if (diff > Math.PI && diff < Math.PI + 1) {
//            //这个时候得判断双方的角速度是往哪边
//        }
    }

    //微小的调整机器人的角速度
    public static void adjust_2_Robots_Rotate(Robot rob1, Robot rob2) {
        //System.err.println("碰撞调整！！！！！！");

        //以机器人所在的相对象限来判断往哪儿转
        //当1相对于2在其右上角
        if (rob1.getX() > rob2.getX() && rob1.getY() > rob2.getY()) {
            double angle = Tool.calAngle(rob2.getX(), rob2.getY(), rob1.getX(), rob1.getY());
            angle = angle > 0 ? angle : angle + Math.PI;
            double intendAngle = rob2.getIntendAngle() > 0 ? rob2.getIntendAngle() : rob2.getIntendAngle() + 2 * Math.PI;
            //如果不在扇形范围内就不变了
            if(Math.abs(intendAngle - angle)>1){
                return;
            }
            if (intendAngle > angle) {
                rob2.setWantRotate(3); // 逆时针转
                rob1.setWantRotate(3);
            } else {
                rob1.setWantRotate(-3);
                rob2.setWantRotate(-3);
            }
        } else if (rob1.getX() > rob2.getX() && rob1.getY() < rob2.getY()) {
            double angle = Tool.calAngle(rob1.getX(), rob1.getY(), rob2.getX(), rob2.getY());
            angle = angle > 0 ? angle : angle + Math.PI;
            double intendAngle = rob1.getIntendAngle() > 0 ? rob1.getIntendAngle() : rob1.getIntendAngle() + 2 * Math.PI;
            //如果不在扇形范围内就不变了
            if(Math.abs(intendAngle - angle)>1){
                return;
            }
            if (intendAngle > angle) {
                rob1.setWantRotate(3);
                rob2.setWantRotate(3);
            } else {
                rob1.setWantRotate(-3);
                rob2.setWantRotate(-3);
            }
        } else if (rob1.getX() < rob2.getX() && rob1.getY() < rob2.getY()) {
            double angle = Tool.calAngle(rob1.getX(), rob1.getY(), rob2.getX(), rob2.getY());
            angle = angle > 0 ? angle : angle + Math.PI;
            double intendAngle = rob1.getIntendAngle() > 0 ? rob1.getIntendAngle() : rob1.getIntendAngle() + 2 * Math.PI;
            //如果不在扇形范围内就不变了
            if(Math.abs(intendAngle - angle)>1){
                return;
            }
            if (intendAngle > angle) {
                rob2.setWantRotate(3); // 逆时针转
                rob1.setWantRotate(3);
            } else {
                rob1.setWantRotate(-3);
                rob2.setWantRotate(-3);
            }
        } else {
            double angle = Tool.calAngle(rob2.getX(), rob2.getY(), rob1.getX(), rob1.getY());
            angle = angle > 0 ? angle : angle + Math.PI;
            double intendAngle = rob2.getIntendAngle() > 0 ? rob2.getIntendAngle() : rob2.getIntendAngle() + 2 * Math.PI;
            //如果不在扇形范围内就不变了
            if(Math.abs(intendAngle - angle)>1){
                return;
            }
            if (intendAngle > angle) {
                rob1.setWantRotate(3);
                rob2.setWantRotate(3);
            } else {
                rob1.setWantRotate(-3);
                rob2.setWantRotate(-3);
            }
        }
        rob1.setWantForward(2);
        rob2.setWantForward(2);
        if(Main.who.get() == 4){
            rob1.setWantForward(4);
            rob2.setWantForward(4);
        }
    }

    //机器人对撞避免
    public static void avoid_Collide(ArrayList<Robot> robots) {
        double min_judge_distance = 3;
        Robot robot0 = robots.get(0);
        Robot robot1 = robots.get(1);
        Robot robot2 = robots.get(2);
        Robot robot3 = robots.get(3);

        if (lessAngle(robot0, robot1)) {
            if (cal_2_Robots_Distance(robot0, robot1) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot0, robot1);
            }
        }
        if (lessAngle(robot0, robot2)) {
            if (cal_2_Robots_Distance(robot0, robot2) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot0, robot2);
            }
        }
        if (lessAngle(robot0, robot3)) {
            if (cal_2_Robots_Distance(robot0, robot3) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot0, robot3);
            }
        }
        if (lessAngle(robot1, robot2)) {
            if (cal_2_Robots_Distance(robot1, robot2) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot1, robot2);
            }
        }
        if (lessAngle(robot1, robot3)) {
            if (cal_2_Robots_Distance(robot1, robot3) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot1, robot3);
            }
        }
        if (lessAngle(robot2, robot3)) {
            if (cal_2_Robots_Distance(robot2, robot3) < min_judge_distance) {
                adjust_2_Robots_Rotate(robot2, robot3);
            }
        }
    }

    //对每个机器人的思路，如果其前进道路上已经很近了，那就减速，别硬怼
    //也就是判断其朝向的一个扇形距离内有没有别的机器人
    public static void avoidTailgate(Robot robot, ArrayList<Robot> robots) {
        for (Robot robot2 : robots) {
            double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
            double rob_x = robot.getX();
            double rob_y = robot.getY();
            double rob2_x = robot2.getX();
            double rob2_y = robot2.getY();
            if (rob_x == rob2_x && rob_y == rob2_y) {
                continue;
            } else {
                double angle = Tool.calAngle(rob_x, rob_y, rob2_x, rob2_y);
                double distance = cal_2_Robots_Distance(robot, robot2);
                if (Math.abs(intendAngle - angle) < 1) {
//                        if(distance < 4 && Main.who.get() == 1) robot.setWantForward(2);
//                        if(distance < 3.2 && Main.who.get() == 2) robot.setWantForward(3);
//                        if(distance < 4 && Main.who.get() == 3) robot.setWantForward(1);
//                        if(distance < 4 && Main.who.get() == 4) robot.setWantForward(2);
                    if(distance < 2) robot.setWantForward(2);
                }
            }
        }

    }
}