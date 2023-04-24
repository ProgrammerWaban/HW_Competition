package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttackStrategy {

    //(机器人ID,(目标点位(mapIndex0,mapIndex1),  距离))
    public static HashMap<Integer, List<Double>> toEnemyRobotDistance = new HashMap<>();

    public static double tmp_distance = Double.MAX_VALUE;

    //蓝方攻击启动函数
    public static int[] blueTeamAttack(int robotID) {
//        if(Main.robotsToAttack.size() == 1 && Main.robotsToAttack.get(0) != robotID){
//            Main.robotsToAttack.clear();
//        }

        avoidMultiAttack();

        //上面的数组中已经存了各个机器人对应的共计目标了，此时只需判断距离是否小于一定值就去冲撞目标
        List<Double> list = toEnemyRobotDistance.getOrDefault(robotID, null);
        //距离小于5的时候才触发冲撞逻辑
        if (list != null && list.get(2) < 3.6) {
            return new int[]{(int) list.get(0).doubleValue(), (int) list.get(1).doubleValue()};
        } else {
            return null;
        }
    }

    //避免两个机器人同时去撞一个目标
    public static void avoidMultiAttack() {
        ArrayList<int[]> enemyRobots = new ArrayList<>();
        //记录一下每个机器人最近的敌方机器人
        for (int i = 0; i < 4; i++) {
            List<Double> list = toEnemyRobotDistance.getOrDefault(i, null);
            if (list != null) {
                int[] enemyRobot = new int[]{(int) list.get(0).doubleValue(), (int) list.get(1).doubleValue()};
                enemyRobots.add(enemyRobot);
            } else {
                enemyRobots.add(null);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (toEnemyRobotDistance.containsKey(i)) {
                int[] tmp_enemyRobot = enemyRobots.get(i);
                if (tmp_enemyRobot != null) {
                    boolean flag = false;
                    for (int j = i + 1; j < 4; j++) {
                        if (flag) {
                            continue;
                        }
                        int[] otherEnemyRobot = enemyRobots.get(j);
                        if (isSameEnemyRobot(tmp_enemyRobot, otherEnemyRobot)) {
                            List<Double> list = toEnemyRobotDistance.get(i);
                            double tmp_enemyRobotDistance = list.get(2).doubleValue();
                            list = toEnemyRobotDistance.get(j);
                            if(list == null){
                                continue;
                            }
                            double otherEnemyRobotDistance = list.get(2).doubleValue();
                            if (tmp_enemyRobotDistance < otherEnemyRobotDistance) {
                                toEnemyRobotDistance.remove(j);
                            } else {
                                toEnemyRobotDistance.remove(i);
                                flag = true; //当删掉当前的遍历目标时就不需要往后遍历了
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isSameEnemyRobot(int[] robotPos1, int[] robotPos2) {
        if (robotPos1 == null || robotPos2 == null) {
            return true;
        }
        int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for (int i = 0; i < direction.length; i++) {
            int tmp_0 = robotPos1[0] + direction[i][0];
            int tmp_1 = robotPos1[1] + direction[i][1];
            if (tmp_0 == robotPos2[0] && tmp_1 == robotPos2[1]) {
                return true;
            }
        }
        return false;
    }


    public static void collideEnemyRobot(int[][] map_clone, int[][] map, ArrayList<Robot> robots) {
        //敌方机器人潜在可能存在点
        ArrayList<int[]> enemyRobotPos = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] != map_clone[i][j]) {
                    enemyRobotPos.add(new int[]{i, j});
                }
            }
        }

        //用最近的且不带4567商品的机器人去冲撞这个潜在障碍物
        double distance = Integer.MAX_VALUE;
        int[] collideRobotPos = null;
        Robot toCollideRobot = null;
        for (Robot robot : robots) {
            if (robot.getGoodID() == 4 || robot.getGoodID() == 5 || robot.getGoodID() == 6 || robot.getGoodID() == 7) {
                continue;
            }
            for (int[] enemyRobot : enemyRobotPos) {
                double distance_tmp = Tool.calDistanceByXY(enemyRobot[0], enemyRobot[1], robot.getMatXY()[0], robot.getMatXY()[1]);
                if (distance_tmp < distance) {
                    distance = distance_tmp;
                    collideRobotPos = enemyRobot;
                    toCollideRobot = robot;
                }
            }
        }
    }

    //对工作台进行聚类，让蓝机器人在聚类的工作台群内往复移动
    public static void clusterWorkbench(ArrayList<Workbench> enemyWorkbenches, int[][] map_clone, int[][] map) {
        int[][] tmp_map = new int[map.length][];

        for (Workbench enemyWorkbench : enemyWorkbenches) {
            tmp_map[enemyWorkbench.getxMap()][enemyWorkbench.getyMap()] = -1;
        }

        //如果一个工作台的两格子范围内还有工作台，就表示这俩工作台是一类
        for (Workbench enemyWorkbench : enemyWorkbenches) {
            int[][] directions = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
                    {1, 2}, {2, 1}, {-1, -2}, {-2, -1}, {1, -2}, {-1, 2}, {-2, 1}, {2, -1},
                    {0, 2}, {0, -2}, {2, 0}, {-2, 0}, {2, 2}, {-2, 2}, {2, -2}, {-2, -2}};
            for (int[] direction : directions) {
                int mapIndex1 = enemyWorkbench.getyMap() + direction[0];
                int mapIndex0 = enemyWorkbench.getxMap() + direction[1];
                if (mapIndex1 > map.length - 1 || mapIndex1 < 0 || mapIndex0 < 0 || mapIndex0 > map[0].length - 1)
                    continue;
                if (tmp_map[mapIndex0][mapIndex1] == -1) {
                    return;
                }
            }
        }
    }
}
