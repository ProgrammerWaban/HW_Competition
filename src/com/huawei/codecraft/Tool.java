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

    //  打印输出路径
    public static void printMap(int[][] map, List<List<int[]>> robotsPath) {
        int[][] look = new int[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                look[i][j] = map[i][j];
            }
        }
        for (List<int[]> p : robotsPath) {
            for (int[] pp : p) {
                look[pp[0]][pp[1]] = -3;
            }
        }
        for (int i = map.length - 1; i >= 0; i--) {
            for (int j = 0; j < map[0].length; j++) {
                if (look[i][j] == -3) System.err.print('+');
                if (look[i][j] == -2) System.err.print(' ');
                if (look[i][j] == -1) System.err.print('#');
                if (look[i][j] >= 0) System.err.print(' ');
            }
            System.err.print("\n");
        }
    }

    //如果有一个机器人站在工作台上，就把这个工作台弄死
    public static ArrayList<Workbench> isStandOnWorkbench(int[][] map_clone, int[][] map, ArrayList<Workbench> workbenches) {
        ArrayList<Workbench> ret = new ArrayList<>();
        for (Workbench workbench : workbenches) {
            if (isEnemyRobotAround(map_clone, map, workbench)) {
                workbench.isEnemyNotOn = false;
                //workbench.setAlive(false);
                ret.add(workbench);
            }

        }
        return ret;
    }

    public static boolean isEnemyRobotAround(int[][] map_clone, int[][] map, Workbench workbench) {
        int mapIndex1 = workbench.getyMap();
        int mapIndex0 = workbench.getxMap();
        int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
                {1, 2}, {2, 1}, {-1, -2}, {-2, -1}, {1, -2}, {-1, 2}, {-2, 1}, {2, -1},
                {0, 2}, {0, -2}, {2, 0}, {-2, 0}, {2, 2}, {-2, 2}, {2, -2}, {-2, -2}};
        for (int i = 0; i < direction.length; i++) {
            int tmp_Y = mapIndex0 + direction[i][0];
            int tmp_X = mapIndex1 + direction[i][1];
            if (tmp_Y >= map.length - 1 || tmp_Y < 1 || tmp_X < 1 || tmp_X >= map[0].length - 1) continue; //边界条件
            if (map[tmp_Y][tmp_X] != map_clone[tmp_Y][tmp_X]) {
                return true;
            }
        }
        return false;
    }
}
