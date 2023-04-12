package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SearchAlgorithm {
    private final static int[][] direction = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    //初始化优先队列，小根堆   [x, y, f]
    private static PriorityQueue<double[]> pq;
    //记录每个点的g
    private static double[][] gMatrix;
    //记录每个点的父母点
    private static int[][][] parent;

    public static void bfs(int[] start, int[][] map, double[][] distanceMatrix, int hasGood){
        //初始化
        pq = new PriorityQueue<>(Comparator.comparingDouble(o -> o[2]));
        gMatrix = new double[map.length][map[0].length];
        for(int i = 0; i < gMatrix.length; i++){
            for(int j = 0; j < gMatrix[0].length; j++){
                gMatrix[i][j] = Double.MAX_VALUE;
            }
        }
        parent = new int[map.length][map[0].length][2];
        for(int i = 0; i < parent.length; i++){
            for(int j = 0; j < parent[0].length; j++){
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        //起点
        double[] startNode = new double[]{start[0], start[1], 0};
        pq.offer(startNode);
        gMatrix[start[0]][start[1]] = 0;
        parent[start[0]][start[1]][0] = start[0];
        parent[start[0]][start[1]][1] = start[1];
        //开始遍历
        while (!pq.isEmpty()){
            double[] now = pq.poll();
            int nowX = (int)now[0];
            int nowY = (int)now[1];

            //为每一点记录距离
            distanceMatrix[nowX][nowY] = now[2];

            //往8个方向走
            for(int[] d : direction) {
                //判断该方向是否可达，不可达就跳过
                if(!isReachable(nowX, nowY, d, hasGood, map))  continue;

                //更新消耗并进优先队列
                int newX = nowX + d[0];
                int newY = nowY + d[1];
                double gCos = d[0] == 0 || d[1] == 0 ? 1 : 1.4;
                double newCos = gMatrix[nowX][nowY] + gCos;
                //如果新点的newCos小于之前的cos，就更新
                if(newCos < gMatrix[newX][newY]){
                    gMatrix[newX][newY] = newCos;
                    parent[newX][newY][0] = nowX;
                    parent[newX][newY][1] = nowY;
                    pq.offer(new double[]{newX, newY, newCos});
                }
            }
        }
    }

    public static List<int[]> astar(int[] start, int[] stop, int[][] map, double[][] distMat, int hasGood){
        List<int[]> path = new ArrayList<>();

        //初始化
        pq = new PriorityQueue<>(Comparator.comparingDouble(o -> o[2]));
        gMatrix = new double[map.length][map[0].length];
        for(int i = 0; i < gMatrix.length; i++){
            for(int j = 0; j < gMatrix[0].length; j++){
                gMatrix[i][j] = Double.MAX_VALUE;
            }
        }
        parent = new int[map.length][map[0].length][2];
        for(int i = 0; i < parent.length; i++){
            for(int j = 0; j < parent[0].length; j++){
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        //起点
        double[] startNode = new double[]{start[0], start[1], 0};
        pq.offer(startNode);
        gMatrix[start[0]][start[1]] = 0;
        parent[start[0]][start[1]][0] = start[0];
        parent[start[0]][start[1]][1] = start[1];

        //开始遍历
        while (!pq.isEmpty()){
            double[] now = pq.poll();
            int nowX = (int)now[0];
            int nowY = (int)now[1];

            //判断是否到终点
            if(nowX == stop[0] && nowY == stop[1]){
                int m = stop[0];
                int n = stop[1];
                path.add(new int[]{m, n});
                while(m != start[0] || n != start[1]){
                    int tempM = parent[m][n][0];
                    int tempN = parent[m][n][1];
                    m = tempM;
                    n = tempN;
                    path.add(new int[]{m, n});
                }
                break;
            }

            //往8个方向走
            for(int[] d : direction) {
                //判断该方向是否可达，不可达就跳过
                if(!isReachable(nowX, nowY, d, hasGood, map))  continue;

                //更新消耗并进优先队列
                int newX = nowX + d[0];
                int newY = nowY + d[1];
                double gCos = d[0] == 0 || d[1] == 0 ? 1 : 1.4;
                double newCos = gMatrix[nowX][nowY] + gCos;
                //如果新点的newCos小于之前的cos，就更新
                if(newCos < gMatrix[newX][newY]){
                    gMatrix[newX][newY] = newCos;
                    parent[newX][newY][0] = nowX;
                    parent[newX][newY][1] = nowY;
                    //加上h就是astar
                    double f = newCos + distMat[newX][newY];
                    //如果贴墙，距离加2
                    if(isCloseWall(newX, newY, map))    f += 12;
                    pq.offer(new double[]{newX, newY, f});
                }
            }
        }

        return path;
    }

    public static List<int[]> astar(int[] start, int[] stop, int[][] map, int hasGood, int method){
        List<int[]> path = new ArrayList<>();

        //初始化
        pq = new PriorityQueue<>(Comparator.comparingDouble(o -> o[2]));
        gMatrix = new double[map.length][map[0].length];
        for(int i = 0; i < gMatrix.length; i++){
            for(int j = 0; j < gMatrix[0].length; j++){
                gMatrix[i][j] = Double.MAX_VALUE;
            }
        }
        parent = new int[map.length][map[0].length][2];
        for(int i = 0; i < parent.length; i++){
            for(int j = 0; j < parent[0].length; j++){
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        //起点
        double[] startNode = new double[]{start[0], start[1], 0};
        pq.offer(startNode);
        gMatrix[start[0]][start[1]] = 0;
        parent[start[0]][start[1]][0] = start[0];
        parent[start[0]][start[1]][1] = start[1];

        //开始遍历
        while (!pq.isEmpty()){
            double[] now = pq.poll();
            int nowX = (int)now[0];
            int nowY = (int)now[1];

            //判断是否到终点
            if(nowX == stop[0] && nowY == stop[1]){
                int m = stop[0];
                int n = stop[1];
                path.add(new int[]{m, n});
                while(m != start[0] || n != start[1]){
                    int tempM = parent[m][n][0];
                    int tempN = parent[m][n][1];
                    m = tempM;
                    n = tempN;
                    path.add(new int[]{m, n});
                }
                break;
            }

            //往8个方向走
            for(int[] d : direction) {
                //判断该方向是否可达，不可达就跳过
                if(!isReachable(nowX, nowY, d, hasGood, map))  continue;

                //更新消耗并进优先队列
                int newX = nowX + d[0];
                int newY = nowY + d[1];
                double gCos = d[0] == 0 || d[1] == 0 ? 1 : 1.4;
                double newCos = gMatrix[nowX][nowY] + gCos;
                //如果新点的newCos小于之前的cos，就更新
                if(newCos < gMatrix[newX][newY]){
                    gMatrix[newX][newY] = newCos;
                    parent[newX][newY][0] = nowX;
                    parent[newX][newY][1] = nowY;
                    //加上h就是astar
                    double f = newCos + calHValue(new int[]{newX, newY}, stop, method);
                    //如果贴墙，距离加2
                    if(isCloseWall(newX, newY, map))    f += 12;
                    pq.offer(new double[]{newX, newY, f});
                }
            }
        }

        return path;
    }


    public static boolean isReachable(int nowX, int nowY, int[] d, int hasGood, int[][] map){
        int newX = nowX + d[0];
        int newY = nowY + d[1];
        //如果越过边界跳过
        if(newX >= map.length || newX < 0 || newY < 0 || newY >= map[0].length)  return false;
        //如果是障碍物就跳过
        if(map[newX][newY] == -1) return false;
        //防止对角线穿墙
        if(d[0] != 0 && d[1] != 0){
            if(map[nowX + d[0]][nowY] == -1)  return false;
            if(map[nowX][nowY + d[1]] == -1)  return false;
        }

        //有货物
        if(hasGood == 1){
            //对角线行走
            if(d[0] != 0 && d[1] != 0){
                for(int[] direct : direction){
                    int tempX = newX + direct[0];
                    int tempY = newY + direct[1];
                    if(tempX >= map.length || tempX < 0 || tempY < 0 || tempY >= map[0].length || map[tempX][tempY] == -1) return false;
                }
            }
            //上下左右
            else{
                //x不动
                if(d[0] == 0){
                    if(newX + 1 >= map.length || map[newX + 1][newY] == -1)   return false;
                    if(newX - 1 < 0 || map[newX - 1][newY] == -1)   return false;
                    if(nowX + 1 >= map.length || map[nowX + 1][nowY] == -1)   return false;
                    if(nowX - 1 < 0 || map[nowX - 1][nowY] == -1)   return false;
                }
                //y不动
                if(d[1] == 0){
                    if(newY + 1 >= map[0].length || map[newX][newY + 1] == -1)   return false;
                    if(newY - 1 < 0 || map[newX][newY - 1] == -1)   return false;
                    if(nowY + 1 >= map[0].length || map[nowX][nowY + 1] == -1)   return false;
                    if(nowY - 1 < 0 || map[nowX][nowY - 1] == -1)   return false;
                }
            }
        }

        //无货物
        if(hasGood == 0){
            //对角线行走
            if(d[0] != 0 && d[1] != 0){
                for(int[] direct : direction){
                    int tempX = nowX + direct[0];
                    int tempY = nowY + direct[1];
                    if(tempX >= map.length || tempX < 0 || tempY < 0 || tempY >= map[0].length || map[tempX][tempY] == -1) return false;
                }
            }
            //上下左右
            else{
                if(d[1] == 1){
                    if(newX + 1 >= map.length || map[newX + 1][newY] == -1)   return false;
                    if(nowX + 1 >= map.length || map[nowX + 1][nowY] == -1)   return false;
                }
                if(d[1] == -1){
                    if(newX - 1 < 0 || map[newX - 1][newY] == -1)   return false;
                    if(nowX - 1 < 0 || map[nowX - 1][nowY] == -1)   return false;
                }
                if(d[0] == 1){
                    if(newY - 1 < 0 || map[newX][newY - 1] == -1)   return false;
                    if(nowY - 1 < 0 || map[nowX][nowY - 1] == -1)   return false;
                }
                if(d[0] == -1){
                    if(newY + 1 >= map[0].length || map[newX][newY + 1] == -1)   return false;
                    if(nowY + 1 >= map[0].length || map[nowX][nowY + 1] == -1)   return false;
                }
            }
        }

        return true;
    }

    public static boolean isCloseWall(int x, int y, int[][] map){
//        if(x + 1 >= map.length || map[x + 1][y] == -1)  return true;
//        if(x - 1 < 0 || map[x - 1][y] == -1)  return true;
//        if(y + 1 >= map[0].length || map[x][y + 1] == -1)  return true;
//        if(y - 1 < 0 || map[x][y - 1] == -1)  return true;
        for(int[] d : direction){
            int newX = x + d[0];
            int newY = y + d[1];
            if(newX >= map.length || newX < 0 || newY < 0 || newY >= map[0].length) return true;
            if(map[newX][newY] == -1)   return true;
        }
        return false;
    }

    public static double calHValue(int[] start, int[] stop, int method){
        //欧式距离
        if(method == 1){
            return Tool.calDistanceByXY(start[0], start[1], stop[0], stop[1]);
        }
        //横纵坐标差值绝对值
        if(method == 2){
            return Math.abs(start[0] - stop[0]) + Math.abs(start[1] - stop[1]);
        }
        //否则的话返回0，那就是bfs
        return 0;
    }
}
