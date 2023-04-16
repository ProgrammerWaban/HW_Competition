package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.List;

public class AvoidAlgorithm {
    public static int[][] safeLocation = new int[][]{{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
    public static int[][] safeFlag = new int[][]{{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}};
    private final static int[][] direction = new int[][]{{0, 0}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    private final static int[][] bigDirection = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
            {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}, {1, -2}, {0, -2}, {-1, -2}, {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1}, {-2, 2}, {1, 2}, {0, 2}, {-1, 2}};
    private final static int[] id = new int[]{0, 1, 2, 3};

    private static int[][] newMap;

    //检测解除安全点
    public static void liftTheBlock(ArrayList<Robot> robots, List<List<int[]>> robotsPath){
        for(int i = 0; i < robots.size(); i++){
            if(safeFlag[i][2] != -1){
                boolean flag = true;
                List<int[]> path = robotsPath.get(safeFlag[i][2]);
                for(int[] p : path){
                    if(p[0] == safeFlag[i][0] && p[1] == safeFlag[i][1]){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    safeLocation[i][0] = -1;
                    safeLocation[i][1] = -1;
                    safeFlag[i][0] = -1;
                    safeFlag[i][1] = -1;
                    safeFlag[i][2] = -1;
                }
            }
        }
    }

    //防堵塞的入口
    public static void avoidCongest(ArrayList<Robot> robots, List<List<int[]>> robotsPath, int[][] map){
        //复制地图
        newMap = new int[map.length][map[0].length];
        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[0].length; j++){
                newMap[i][j] = map[i][j];
            }
        }
        //22匹配防堵塞
        for(int i = 0; i < robots.size(); i++){
            for(int j = i + 1; j < robots.size(); j++){
                List<int[]> path1 = robotsPath.get(i);
                List<int[]> path2 = robotsPath.get(j);
                int flag = 0;
                if(robots.get(i).getGoodID() > 0 && robots.get(j).getGoodID() > 0)  flag = 1;
                else if(robots.get(i).getGoodID() > 0 || robots.get(j).getGoodID() > 0)  flag = 2;
                //判断路径有没有重叠冲突
                if(isCongest(path1, path2, newMap, flag)){
                    List<int[]> safePath1 = safeFlag[i][2] != -1 ? null : findSafePath(path2, path1.get(0), newMap, robots.get(i).getGoodID() == 0 ? 0 : 1);
                    List<int[]> safePath2 = safeFlag[j][2] != -1 ? null : findSafePath(path1, path2.get(0), newMap, robots.get(j).getGoodID() == 0 ? 0 : 1);
                    //再判断路径有没有重叠冲突
                    for(int k : id){
                        if(k == i || k == j)    continue;
                        List<int[]> pathK = robotsPath.get(k);
                        if(safePath1 != null && isCongest(pathK, safePath1, newMap, flag))    safePath1 = null;
                        if(safePath2 != null && isCongest(pathK, safePath2, newMap, flag))    safePath2 = null;
                    }

                    if(safePath1 == null && safePath2 == null){
                        System.err.println("都没有找到路径");
                        continue;
                    }
                    if(safePath1 == null){
                        robotsPath.set(j, safePath2);
                        safeLocation[j][0] = safePath2.get(safePath2.size() - 1)[0];
                        safeLocation[j][1] = safePath2.get(safePath2.size() - 1)[1];
                        safeFlag[j][0] = safePath2.get(safePath2.size() - 6)[0];
                        safeFlag[j][1] = safePath2.get(safePath2.size() - 6)[1];
                        safeFlag[j][2] = i;
                        for(int[] d : direction){
                            int newX = safeLocation[j][0] + d[0];
                            int newY = safeLocation[j][1] + d[1];
                            if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                            newMap[newX][newY] = -1;
                        }
                    }
                    if(safePath2 == null){
                        robotsPath.set(i, safePath1);
                        safeLocation[i][0] = safePath1.get(safePath1.size() - 1)[0];
                        safeLocation[i][1] = safePath1.get(safePath1.size() - 1)[1];
                        safeFlag[i][0] = safePath1.get(safePath1.size() - 6)[0];
                        safeFlag[i][1] = safePath1.get(safePath1.size() - 6)[1];
                        safeFlag[i][2] = j;
                        for(int[] d : direction){
                            int newX = safeLocation[i][0] + d[0];
                            int newY = safeLocation[i][1] + d[1];
                            if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                            newMap[newX][newY] = -1;
                        }
                    }
                    if(safePath1 != null && safePath2 != null){
                        if(robots.get(i).getGoodID() != 0 && robots.get(j).getGoodID() == 0){
                            robotsPath.set(j, safePath2);
                            safeLocation[j][0] = safePath2.get(safePath2.size() - 1)[0];
                            safeLocation[j][1] = safePath2.get(safePath2.size() - 1)[1];
                            safeFlag[j][0] = safePath2.get(safePath2.size() - 6)[0];
                            safeFlag[j][1] = safePath2.get(safePath2.size() - 6)[1];
                            safeFlag[j][2] = i;
                            for(int[] d : direction){
                                int newX = safeLocation[j][0] + d[0];
                                int newY = safeLocation[j][1] + d[1];
                                if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                                newMap[newX][newY] = -1;
                            }
                        }
                        else if(robots.get(i).getGoodID() == 0 && robots.get(j).getGoodID() != 0){
                            robotsPath.set(i, safePath1);
                            safeLocation[i][0] = safePath1.get(safePath1.size() - 1)[0];
                            safeLocation[i][1] = safePath1.get(safePath1.size() - 1)[1];
                            safeFlag[i][0] = safePath1.get(safePath1.size() - 6)[0];
                            safeFlag[i][1] = safePath1.get(safePath1.size() - 6)[1];
                            safeFlag[i][2] = j;
                            for(int[] d : direction){
                                int newX = safeLocation[i][0] + d[0];
                                int newY = safeLocation[i][1] + d[1];
                                if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                                newMap[newX][newY] = -1;
                            }
                        } else{
                            if(safePath1.size() < safePath2.size()){
                                robotsPath.set(i, safePath1);
                                safeLocation[i][0] = safePath1.get(safePath1.size() - 1)[0];
                                safeLocation[i][1] = safePath1.get(safePath1.size() - 1)[1];
                                safeFlag[i][0] = safePath1.get(safePath1.size() - 6)[0];
                                safeFlag[i][1] = safePath1.get(safePath1.size() - 6)[1];
                                safeFlag[i][2] = j;
                                for(int[] d : direction){
                                    int newX = safeLocation[i][0] + d[0];
                                    int newY = safeLocation[i][1] + d[1];
                                    if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                                    newMap[newX][newY] = -1;
                                }
                            }else{
                                robotsPath.set(j, safePath2);
                                safeLocation[j][0] = safePath2.get(safePath2.size() - 1)[0];
                                safeLocation[j][1] = safePath2.get(safePath2.size() - 1)[1];
                                safeFlag[j][0] = safePath2.get(safePath2.size() - 6)[0];
                                safeFlag[j][1] = safePath2.get(safePath2.size() - 6)[1];
                                safeFlag[j][2] = i;
                                for(int[] d : direction){
                                    int newX = safeLocation[j][0] + d[0];
                                    int newY = safeLocation[j][1] + d[1];
                                    if(newX < 0 || newX >= newMap.length || newY < 0 || newY >= newMap[0].length)   continue;
                                    newMap[newX][newY] = -1;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    //判断路径有没有重叠冲突
    public static boolean isCongest(List<int[]> path1, List<int[]> path2, int[][] map, int flag){
        if(path1.size() == 0 || path2.size() == 0)  return false;
        int loc1on2 = calLocOn(path2, path1.get(0));
        int loc2on1 = calLocOn(path1, path2.get(0));
        if(loc1on2 > 16 || loc2on1 > 16)    return false;
        boolean isBlock = false;
        if(loc1on2 != -1 && loc2on1 != -1){
            int[] midPoint1 = path1.get(loc2on1 / 2);
            int[] midPoint2 = path2.get(loc1on2 / 2);
            if(midPoint1[0] - 2 < 0 || midPoint1[0] + 2 >= map.length || midPoint1[1] - 2 < 0 || midPoint1[1] + 2 >= map[0].length) isBlock = true;
            if(midPoint2[0] - 2 < 0 || midPoint2[0] + 2 >= map.length || midPoint2[1] - 2 < 0 || midPoint2[1] + 2 >= map[0].length) isBlock = true;
            int n1 = 0;
            int n2 = 0;
            for(int[] d : bigDirection){
                int newX1 = midPoint1[0] + d[0];
                int newY1 = midPoint1[1] + d[1];
                int newX2 = midPoint2[0] + d[0];
                int newY2 = midPoint2[1] + d[1];
                if(map[newX1][newY1] == -1) n1++;
                if(map[newX2][newY2] == -1) n2++;
            }
            if(midPoint1[0] == midPoint2[0] && midPoint1[1] == midPoint2[1]){
                if(flag == 1){
                    if(n1 > 2)  isBlock = true;
                }
                if(flag == 2){
                    if(n1 > 6)  isBlock = true;
                }
                if(flag == 0){
                    if(n1 > 12)  isBlock = true;
                }
            }
            else if(midPoint1[0] - midPoint2[0] == 0 || midPoint1[1] - midPoint2[1] == 0){
                if(flag == 1){
                    if(n1 + n2 > 3)  isBlock = true;
                }
                if(flag == 2){
                    if(n1 + n2 > 9)  isBlock = true;
                }
                if(flag == 0){
                    if(n1 + n2 > 15)  isBlock = true;
                }
            }
            else {
                if(flag == 1){
                    if(n1 + n2 > 3)  isBlock = true;
                }
                if(flag == 2){
                    if(n1 + n2 > 9)  isBlock = true;
                }
                if(flag == 0){
                    if(n1 + n2 > 15)  isBlock = true;
                }
            }
        }
        return loc1on2 != -1 && loc2on1 != -1 && isBlock;
    }

    //判断点在路径的哪个位置
    public static int calLocOn(List<int[]> path, int[] point){
        int loc = -1;
        for (int i = 0; i < path.size(); i++) {
            int[] nowPoint = path.get(i);
            for(int[] d : direction){
                int newX = nowPoint[0] + d[0];
                int newY = nowPoint[1] + d[1];
                if(newX == point[0] && newY == point[1])    loc = i;
            }
            if(loc != -1)   break;
        }
        return loc;
    }

    //沿着对面路径去找安全点
    public static List<int[]> findSafePath(List<int[]> path, int[] point, int[][] map, int hasGood){
        List<int[]> newPath = new ArrayList<>();
        int start = calLocOn(path, point) + 1;
        for(int i = start; i < path.size() - 1; i++){
            newPath.add(path.get(i));
            int[] pre = path.get(i - 1);
            int[] mid = path.get(i);
            int[] post = path.get(i + 1);
            //看看能不能按对面的路径走
            int[] D = new int[]{mid[0] - pre[0], mid[1] - pre[1]};
            if(!SearchAlgorithm.isReachable(pre[0], pre[1], D, hasGood, map)){
                return null;
            }
            //排除方向
            int[] errorD1 = new int[]{pre[0] - mid[0], pre[1] - mid[1]};
            int[] errorD2 = new int[]{post[0] - mid[0], post[1] - mid[1]};
            //往其他方向找安全点
            for(int[] d : direction){
                if(d[0] == errorD1[0] && d[1] == errorD1[1])    continue;
                if(d[0] == errorD2[0] && d[1] == errorD2[1])    continue;
                if(d[0] == 0 && d[1] == 0)    continue;
                if(SearchAlgorithm.isReachable(mid[0], mid[1], d, hasGood, map)
                        && SearchAlgorithm.isReachable(mid[0] + d[0], mid[1] + d[1], d, hasGood, map)
                        && SearchAlgorithm.isReachable(mid[0] + d[0] + d[0], mid[1] + d[1] + d[1], d, hasGood, map)
                        && SearchAlgorithm.isReachable(mid[0] + d[0] + d[0] + d[0], mid[1] + d[1] + d[1] + d[1], d, hasGood, map)
                        && SearchAlgorithm.isReachable(mid[0] + d[0] + d[0] + d[0] + d[0], mid[1] + d[1] + d[1] + d[1] + d[1], d, hasGood, map)){
                    newPath.add(new int[]{mid[0] + d[0], mid[1] + d[1]});
                    newPath.add(new int[]{mid[0] + d[0] * 2, mid[1] + d[1] * 2});
                    newPath.add(new int[]{mid[0] + d[0] * 3, mid[1] + d[1] * 3});
                    newPath.add(new int[]{mid[0] + d[0] * 4, mid[1] + d[1] * 4});
                    newPath.add(new int[]{mid[0] + d[0] * 5, mid[1] + d[1] * 5});
                    return newPath;
                }
            }
        }
        return null;
    }
}
