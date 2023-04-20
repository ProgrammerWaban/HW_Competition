package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BetterMove {
    public static void test(Workbench destination, Robot robot) {
        robot.setWantForward(0);
        robot.setWantRotate(Math.PI);
    }

    public static void adjustMovement(Workbench destination, Robot robot) {
        if (destination == null) {
            robot.setWantRotate(1);
            robot.setWantForward(1);
            return;
        }

        //机器人 ==》 目的地 向量坐标
        double distance_x = destination.getX() - robot.getX();
        double distance_y = destination.getY() - robot.getY();
        double distance = Math.sqrt(distance_x * distance_x + distance_y * distance_y);
        //线速度坐标
        double v_x = robot.getLineSpend_x();
        double v_y = robot.getLineSpend_y();
        double v = Math.sqrt(v_x * v_x + v_y * v_y);
        //角速度
        double w = robot.getRotate();
        // v 叉乘 d
        double forkMulti = v_x * distance_y - v_y * distance_x;
        // v 点乘 d
        double pointMulti = v_x * distance_x + v_y * distance_y;

//        除数的0值判断(我感觉其实可以换成是不是要撞墙),这个函数表示已经很贴近墙了就得倒车了
//        if (closeToWall(robot, 0.8)) {
//            robot.setWantForward(-2);
//            //如果速度为0那这个叉乘一定也为0，此时得转
//            //如果速度为0了就得用intendAngle来判断转的方向了叭
//            double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
//            //假设这个向量的x为1
//            double intend_x = 1;
//            double intend_y = intend_x * Math.tan(intendAngle);
//            double forkIntend = intend_x * distance_y - intend_y * distance_x;
//            if (forkIntend > 0) {
//                robot.setWantRotate(Math.PI);
//            } else {
//                robot.setWantRotate(-Math.PI);
//            }
//            return;
//        }

        //先计算夹角，如果夹角小于满足线速度取6的值，则全速前进，角速度根据线速度为6来调整
        //若不满足，将线速度取1，角速度取最大（通过叉乘来判断角速度取正值还是负值）
        double theta = Math.acos(pointMulti / (v + Double.MIN_VALUE) / distance); //theta表示线速度和目的地向量的夹角
        double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
        //表示机器人的单位方向角向量
        double intend_x = Math.cos(intendAngle);
        double intend_y = Math.sin(intendAngle);
        //方向角向量和目的地向量的叉乘
        double forkIntend = intend_x * distance_y - intend_y * distance_x;
        double pointIntend = intend_x * distance_x + intend_y * distance_y;
        double alpha = Math.acos(pointIntend / distance);  //alpha表示倾向角和目的地向量的夹角

//        System.err.println("机器人线速度: " + v_x + "  " + v_y + " 目的地坐标: " + distance_x + "  " + distance_y + " 点乘=" + pointMulti + " 叉乘=" + forkMulti);
//        System.err.println(alpha + "   " + pointIntend + "     " + distance);

        if (alpha < Math.PI / 2) {
            double omega = 12 * Math.sin(theta) / distance;
            //如果角度已经很小了就降低角速度是吧
            if (omega < Math.PI) {
                //omega表示角速度
                robot.setWantRotate(omega);
                robot.setWantForward(7);
            } else {
                if (distance < 1) {
                    robot.setWantRotate(Math.PI);
                    double a = distance * Math.PI / 2 / Math.sin(theta);
                    robot.setWantForward(a);
                    if (theta == 0) {
                        robot.setWantForward(1);
                    }
                } else {
                    if (v < 2) {
                        robot.setWantForward(1.5);
                        robot.setWantRotate(Math.PI);
                    } else {
                        robot.setWantForward(3);
                        robot.setWantRotate(Math.PI);
                    }
                }
            }
        } else {
            robot.setWantForward(-1);
            robot.setWantRotate(Math.PI);
        }
        //判断角速度往哪边转
        //double asin = Math.asin(forkMulti / v / distance);
        if (forkIntend == 0) {
            //不转
            robot.setWantRotate(0);
        } else if (forkIntend < 0) {
            robot.setWantRotate(robot.getWantRotate() * (-1));
        } //如果大于零就什么都不做

//        //这里加一个靠近墙壁减速的活儿，防止碰撞损失
//        if (robot.getWantForward() >= 0) {
//            if (closeToWall(robot, 1.8)) {
//                robot.setWantForward(1);
//            }
//        }

    }

    public static void adjustMovementJump(Workbench destination, Robot robot) {
        if (destination == null) {
            robot.setWantRotate(1);
            robot.setWantForward(1);
            return;
        }

        //机器人 ==》 目的地 向量坐标
        double distance_x = destination.getX() - robot.getX();
        double distance_y = destination.getY() - robot.getY();
        double distance = Math.sqrt(distance_x * distance_x + distance_y * distance_y);
        //线速度坐标
        double v_x = robot.getLineSpend_x();
        double v_y = robot.getLineSpend_y();
        double v = Math.sqrt(v_x * v_x + v_y * v_y);
        //角速度
        double w = robot.getRotate();
        // v 叉乘 d
        double forkMulti = v_x * distance_y - v_y * distance_x;
        // v 点乘 d
        double pointMulti = v_x * distance_x + v_y * distance_y;

//        除数的0值判断(我感觉其实可以换成是不是要撞墙),这个函数表示已经很贴近墙了就得倒车了
//        if (closeToWall(robot, 0.8)) {
//            robot.setWantForward(-2);
//            //如果速度为0那这个叉乘一定也为0，此时得转
//            //如果速度为0了就得用intendAngle来判断转的方向了叭
//            double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
//            //假设这个向量的x为1
//            double intend_x = 1;
//            double intend_y = intend_x * Math.tan(intendAngle);
//            double forkIntend = intend_x * distance_y - intend_y * distance_x;
//            if (forkIntend > 0) {
//                robot.setWantRotate(Math.PI);
//            } else {
//                robot.setWantRotate(-Math.PI);
//            }
//            return;
//        }

        //先计算夹角，如果夹角小于满足线速度取6的值，则全速前进，角速度根据线速度为6来调整
        //若不满足，将线速度取1，角速度取最大（通过叉乘来判断角速度取正值还是负值）
        double theta = Math.acos(pointMulti / v / distance); //theta表示线速度和目的地向量的夹角
        double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
        //表示机器人的单位方向角向量
        double intend_x = Math.cos(intendAngle);
        double intend_y = Math.sin(intendAngle);
        //方向角向量和目的地向量的叉乘
        double forkIntend = intend_x * distance_y - intend_y * distance_x;
        double pointIntend = intend_x * distance_x + intend_y * distance_y;
        double alpha = Math.acos(pointIntend / distance);  //alpha表示倾向角和目的地向量的夹角

//        System.err.println("机器人线速度: " + v_x + "  " + v_y + " 目的地坐标: " + distance_x + "  " + distance_y + " 点乘=" + pointMulti + " 叉乘=" + forkMulti);
//        System.err.println(alpha + "   " + pointIntend + "     " + distance);

        if (alpha < Math.PI / 2) {
            double omega = 12 * Math.sin(theta) / distance;
            if (omega < Math.PI) {
                robot.setWantRotate(omega * 3 + 1);
                robot.setWantForward(7);
            } else {
                if (distance < 2) {
                    robot.setWantRotate(Math.PI);
                    double a = distance * Math.PI / 2 / Math.sin(theta);
                    robot.setWantForward(a);
                } else {
                    if (v < 2) {
                        robot.setWantForward(1.5);
                        robot.setWantRotate(Math.PI);
                    } else {
                        robot.setWantForward(3);
                        robot.setWantRotate(Math.PI);
                    }
                }
            }
        } else {
//            robot.setWantRotate(Math.PI);
//            robot.setWantForward(-1);
            robot.setWantForward(-1);
            robot.setWantRotate(Math.PI);
        }
        //判断角速度往哪边转
        //double asin = Math.asin(forkMulti / v / distance);
        if (forkIntend == 0) {
            //不转
            robot.setWantRotate(0);
        } else if (forkIntend < 0) {
            robot.setWantRotate(robot.getWantRotate() * (-1));
        } //如果大于零就什么都不做

        //这里加一个靠近墙壁减速的活儿，防止碰撞损失
        //加到外面去了
//        if (robot.getWantForward() >= 0) {
//            if (closeToWall(robot, 1.8)) {
//                robot.setWantForward(1);
//            }
//        }

    }

    public static void nextPointToGo(int[][] map, List<int[]> path, Robot robot, Workbench wb) {
        if (path.size() > 2) {
            int[] ints = path.get(2);
//                        int dx = path.get(path.size() - 3)[0] - path.get(path.size() - 2)[0];
//                        int dy = path.get(path.size() - 3)[1] - path.get(path.size() - 2)[1];
//                        for(int i = path.size() - 3; i > 0; i--){
//                            if(path.get(i)[0] - path.get(i + 1)[0] == dx && path.get(i)[1] - path.get(i + 1)[1] == dy){
//                                ints = path.get(i);
//                            }else{
//                                break;
//                            }
//                        }
            if (Math.pow(robot.getLineSpend_x(), 2) + Math.pow(robot.getLineSpend_y(), 2) < 3) {
                ints = path.get(1);
            }
            //如果是个正方形的四个点就需要特殊处理
            if (path.size() > 4 && isSquare(path)) {
                //System.err.println("=======");
                ints = path.get(4);
            }
            //根据目标点是否靠墙判断走哪一根线
            if (ints[0] + 1 >= 100 || map[ints[0] + 1][ints[1]] == -1) {
                //障碍物在上面,走下面这个线
                wb.setY(ints[0] * 0.5);
                wb.setX(ints[1] * 0.5 + 0.25);
            } else if (ints[0] - 1 < 0 || map[ints[0] - 1][ints[1]] == -1) {
                //障碍物在下面,走上面这个线
                wb.setY(ints[0] * 0.5 + 0.5);
                wb.setX(ints[1] * 0.5 + 0.25);
            } else if (ints[1] + 1 >= 100 || map[ints[0]][ints[1] + 1] == -1) {
                wb.setY(ints[0] * 0.5 + 0.25);
                wb.setX(ints[1] * 0.5);
            } else if (ints[1] - 1 < 0 || map[ints[0]][ints[1] - 1] == -1) {
                wb.setY(ints[0] * 0.5 + 0.25);
                wb.setX(ints[1] * 0.5 + 0.5);
            }
//            else if (map[ints[0] - 1][ints[1] - 1] == -1) {
//                //障碍物在左下角,走右上角这个线
//                wb.setY(ints[0] * 0.5 + 0.5);
//                wb.setX(ints[1] * 0.5 + 0.5);
//            } else if (map[ints[0] + 1][ints[1] - 1] == -1) {
//                //障碍物在左上角
//                wb.setY(ints[0] * 0.5);
//                wb.setX(ints[1] * 0.5 + 0.5);
//            } else if (map[ints[0] - 1][ints[1] + 1] == -1) {
//                //障碍物在右下角
//                wb.setY(ints[0] * 0.5 + 0.5);
//                wb.setX(ints[1] * 0.5);
//            } else if (map[ints[0] + 1][ints[1] + 1] == -1) {
//                //障碍物在右上角
//                wb.setY(ints[0] * 0.5);
//                wb.setX(ints[1] * 0.5);
//            }
            else {
                wb.setY(ints[0] * 0.5 + 0.25);
                wb.setX(ints[1] * 0.5 + 0.25);
            }
        } else {

            int[] ints = path.get(path.size() - 1);
            wb.setY(ints[0] * 0.5 + 0.25);
            wb.setX(ints[1] * 0.5 + 0.25);
        }
    }


    public static void closeToWall_slowDown(int[][] map_clone, Robot robot, double distance_avoid) {
        //根据激光雷达来判断障碍物（只取机器人朝向的扇形区域（ 2*i 度））
        for (int i = 0; i < 20; i++) {
            if (robot.getRadar()[i] < distance_avoid || robot.getRadar()[360 - 1 - i] < distance_avoid) {
                robot.setWantForward(2);
                return;
            }
        }
    }

//    public static void closeToRobot_avoid(int[][] map_clone, int[][] map, Robot robot){
//        for (int i = 0; i < 30; i++) {
//            double angle_willAdd = Math.toRadians(i);
//            singleLineOfRadarIsEnemyRobot(map_clone, map, robot, i);
//            singleLineOfRadarIsRobot(map_clone, map, robots, intendAngle - angle_willAdd, radar[360 - 1 - i]);
//        }
//    }
//
//    //用于判断在一条线上是否有障碍物，进而来更新地图
//    private static void singleLineOfRadarIsEnemyRobot(int[][] map_clone, int[][] map, Robot robot, int angleIndex) {
//        double angle_willAdd = Math.toRadians(angleIndex);
//        robot.getX()+ (robot.getRadar()[angleIndex] + 0.6) * Math.cos(robot.getIntendAngle()+angle_willAdd)
//
//
//        double x_add = x + (distance + 0.6) * Math.cos(theta);
//        double y_add = y + (distance + 0.6) * Math.sin(theta);
//        int mapIndex0 = (int) (y_add / 0.5);
//        int mapIndex1 = (int) (x_add / 0.5);
//        if (mapIndex0 < 1 || mapIndex0 > 98 || mapIndex1 < 1 || mapIndex1 > 98) return;
//        if (isBarrierAround(mapIndex0, mapIndex1, map)) {
//            //return false; // 表示雷达遍历到的是原生障碍物墙,不做处理
//        } else {
//            if (!isTeammate(robots, distance)) {
//                map_clone[mapIndex0][mapIndex1] = -1;
//                System.err.println("前方有机器人，更改地图");
//                //return true;   //表示遍历到的是机器人（对面方的）
//            }
//            //return false; //对于友方机器人不做处理，直接忽略
//        }
//    }


    //与墙的碰撞避免============这个版本已经不适用了（得用雷达最好）
//    public static boolean closeToWall(Robot robot, double distance_avoid) {
//        //地图为50m*50m
//        //机器人最大半径为0.53~0.45
//        double x = robot.getX();
//        double y = robot.getY();
//        double angleThread = 1;
////        double bevel_len = distance_avoid / Math.sin(Math.PI / 4);
////        double max_len = 50 / Math.sin(Math.PI / 4);
////        double bevel_xy = Math.sqrt(x * x + y * y);
//
//        double intendAngle = robot.getIntendAngle() > 0 ? robot.getIntendAngle() : robot.getIntendAngle() + 2 * Math.PI;
//        if (y < distance_avoid && Math.abs(intendAngle - 3 * Math.PI / 2) < angleThread) {
//            return true;
//        } else if (y > 50 - distance_avoid && Math.abs(intendAngle - Math.PI / 2) < angleThread) {
//            return true;
//        } else if (x < distance_avoid && Math.abs(intendAngle - Math.PI) < angleThread) {
//            return true;
//        } else if (x > 50 - distance_avoid && (intendAngle < angleThread || intendAngle > 2 * Math.PI - angleThread)) {
//            return true;
//        } else if (y < distance_avoid && x < distance_avoid && Math.abs(intendAngle - 5 * Math.PI / 4) < angleThread) {
//            //从这里就开始判断斜边,左下
//            return true;
//        } else if (y < distance_avoid && x > 50 - distance_avoid && Math.abs(intendAngle - 7 * Math.PI / 4) < angleThread) {
//            //右下
//
//            return true;
//        } else if (y > 50 - distance_avoid && x < distance_avoid && Math.abs(intendAngle - 3 * Math.PI / 4) < angleThread) {
//            //左上
//            return true;
//        } else if (y > 50 - distance_avoid && x > 50 - distance_avoid && Math.abs(intendAngle - Math.PI / 4) < angleThread) {
//            //右上
//            return true;
//        }
//        return false;
//    }

    //二分查找可直线行驶的路径
    public static int binarySearchDestination(int[][] map, List<int[]> path, Robot robot, Workbench wb) {
        int left = 0, right = path.size() - 1;
        while (left <= right) {
            int mid = (right - left) / 2 + left;
            int[] destination = path.get(mid);
            if (barrierBetweenRoad(map, robot, destination)) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        //System.err.println("机器人取了第 " + left + " 个点");
        if (right < 4) {
            nextPointToGo(map, path, robot, wb);
            return 0;
        } else {
            wb.setY(path.get(right)[0] * 0.5 + 0.25);
            wb.setX(path.get(right)[1] * 0.5 + 0.25);
            return 1;
        }
    }

    //判断两个点之间的直线路径上是否有障碍物阻塞
    public static boolean barrierBetweenRoad(int[][] map, Robot robot, int[] point) {
        int[] matXY = robot.getMatXY();
        double distance = Tool.calDistanceByXY(matXY[0], matXY[1], point[0], point[1]);
        double theta = Math.atan2(point[1] - matXY[1], point[0] - matXY[0]); //斜率对应的角

        if (matXY[0] == point[0]) {
            if (matXY[1] < point[1]) {
                int dd = 1;
                while (dd < distance) {
                    double[] p1 = new double[2];
                    p1[0] = matXY[0];
                    p1[1] = matXY[1] + dd;
                    if (isBarrierAround(map, p1)) {
                        return true;
                    }
                    dd++;
                }
                return false;
            } else {
                int dd = 1;
                while (dd < distance) {
                    double[] p1 = new double[2];
                    p1[0] = matXY[0];
                    p1[1] = matXY[1] - dd;
                    if (isBarrierAround(map, p1)) {
                        return true;
                    }
                    dd++;
                }
                return false;
            }
        } else if (matXY[1] == point[1]) {
            if (matXY[0] < point[0]) {
                int dd = 1;
                while (dd < distance) {
                    double[] p1 = new double[2];
                    p1[0] = matXY[0] + dd;
                    p1[1] = matXY[1];
                    if (isBarrierAround(map, p1)) {
                        return true;
                    }
                    dd++;
                }
                return false;
            } else {
                int dd = 1;
                while (dd < distance) {
                    double[] p1 = new double[2];
                    p1[0] = matXY[0] - dd;
                    p1[1] = matXY[1];
                    if (isBarrierAround(map, p1)) {
                        return true;
                    }
                    dd++;
                }
                return false;
            }
        }

        int dd = 1;
        while (dd < distance) {
            double[] p1 = new double[2];
            p1[0] = matXY[0] + Math.cos(theta) * dd;
            p1[1] = matXY[1] + Math.sin(theta) * dd;
            if (isBarrierAround(map, p1)) {
                return true;
            }
            dd++;
        }
        return false;
    }

    //判断某个点周围是不是有障碍物
    public static boolean isBarrierAround(int[][] map, double[] p1) {
        int[] ints = new int[2];
        ints[0] = (int) p1[0];
        ints[1] = (int) p1[1];
        if (ints[0] + 2 < 100 && ints[0] - 2 >= 0 && ints[1] + 2 < 100 && ints[1] - 2 >= 0) {
            if (map[ints[0] + 1][ints[1]] == -1 || map[ints[0] - 1][ints[1]] == -1 || map[ints[0]][ints[1] + 1] == -1 ||
                    map[ints[0]][ints[1] - 1] == -1 || map[ints[0] - 1][ints[1] - 1] == -1 ||
                    map[ints[0] + 1][ints[1] - 1] == -1 || map[ints[0] - 1][ints[1] + 1] == -1 ||
                    map[ints[0] + 1][ints[1] + 1] == -1 || map[ints[0]][ints[1]] == -1 ||
                    map[ints[0] + 2][ints[1]] == -1 ||
                    map[ints[0] - 2][ints[1]] == -1 || map[ints[0]][ints[1] + 2] == -1 ||
                    map[ints[0]][ints[1] - 2] == -1 || map[ints[0] - 2][ints[1] - 2] == -1 ||
                    map[ints[0] + 2][ints[1] - 2] == -1 || map[ints[0] - 2][ints[1] + 2] == -1 ||
                    map[ints[0] + 2][ints[1] + 2] == -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    //判断是不是存在正方形路径
    public static boolean isSquare(List<int[]> path) {
        //如果是个正方形的四个点就需要特殊处理
        ArrayList<int[]> sqr = new ArrayList<>();
        int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for (int i = 0; i < direction.length; i++) {
            int mapIndex0 = path.get(0)[0] + direction[i][0];
            int mapIndex1 = path.get(0)[1] + direction[i][1];
            sqr.add(new int[]{mapIndex0, mapIndex1});
        }
        for (int i = 0; i < 4; i++) {
            if (!contains_(sqr, path.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains_(ArrayList<int[]> sqr, int[] ints) {
        for (int[] ints1 : sqr) {
            if (ints[0] == ints1[0] && ints[1] == ints1[1]) {
                return true;
            }
        }
        return false;
    }
}