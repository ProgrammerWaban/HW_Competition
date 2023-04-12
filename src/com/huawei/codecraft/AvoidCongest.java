package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AvoidCongest {

    // 判断当前位置是否在单行道上
    public static boolean iSInSingleWay(int y,int x,int [][]map){
        // 横向两个格子和纵向两个格子没有障碍物
        int count = 0;
        int count1 = 0;
        int [][]directions = new int[][]{{-1,1},{-1,-1},{1,-1},{1,1}};
        // 记录对角线方向的空的位置数
        for(int i=0;i<directions.length;i++){
            int tmp_y = y+directions[i][0];
            int tmp_x = x+directions[i][1];
            int tmp_y_1 = y*2+directions[i][0];
            int tmp_x_1 = x*2+directions[i][1];
            if(tmp_y<map.length&&tmp_y>=0&&tmp_x<map.length&&tmp_x>=0&&map[tmp_y][tmp_x]!=-1){
                count++;
            }

            if(tmp_y_1<map.length&&tmp_y_1>=0&&tmp_x_1<map.length&&tmp_x_1>=0&&map[tmp_y_1][tmp_x_1]!=-1){
                count1++;
            }
        }
        if(count<4)
            return true;
        else if(count1<3)
            return true;

        if(((y+2< map.length&&map[y+2][x]!=-1)||(y-2>=0&&map[y-2][x]!=-1))&&
                ((x+2< map.length&&map[y][x+2]!=-1)||(x-2>=0&&map[y][x-2]!=-1))
        )
        {
            return false;
        }
        return true;
    }

    // 判读一个坐标是否在路径上，在路径上的定义为横向距离为1，纵向距离为0，横向距离为0，纵向距离为1，或横向距离为0，纵向距离为0
    // 存在则返回距离最近的索引，否则返回-1，表示该位置不在路径上。
    public static int isInPath(int y,int x,List<int[]> path){
        int length =path.size();
        length = length>12?12:length;
        int start = -1;
        int tmp1 = -1,tmp2 = -1;
        for(int i=0;i<length;i++){
            int []current = path.get(i);
            int dy = Math.abs(current[0]-y);
            int dx = Math.abs(current[1]-x);
            if(dy==0&&dx==0){
                tmp1 = i;
                break;
            }else if((dy==0&&dx==1)||(dy==1&&dx==0)){
                tmp2 = i;
            }
        }
        if(tmp1!=-1){
            start = tmp1;
        }else {
            start = tmp2;
        }
        return start;
    }

    // 在路径上寻找可以达到安全位置的路径，start指在路径上的开始寻找的位置的索引，start索引对应的位置为当前小车的位置
    public static List<int []> findSafePlace(List<int []>src_path, int start,int [][]map, int has_good){
        int path_length = src_path.size();
        List<int[]> safe_path;
        if(start==-1){
            //System.err.println("索引位置不在路径上");
            return null;
        }

        int i1 = start;
        if(path_length<2){
            //System.err.println("路径长度为1，无法判断将要去的方向");
            return null;
        }

        while(i1<path_length){
            // 分别表示当前位置，下一个位置和上一个位置
            int []current = src_path.get(i1);
            int []next;
            int []before;
            // 在第一个位置，不知到上一个位置，则用下一个位置预估上一个位置
            if(i1-1>=0)
                before = src_path.get(i1-1);
            else {
                next = src_path.get(i1+1);
                before = new int[]{2 * current[0] - next[0], 2 * current[1] - next[1]};
            }
            // 在第最后一个位置，不知到下一个位置，则用上一个位置预估下一个位置
            if(i1+1<path_length)
                next = src_path.get(i1+1);
            else
                next = new int[]{2*current[0]-before[0],2*current[1]-before[1]};


            // 错误的方向，指该位置接下来移动的方向，上一个移动方向的反方向，或原地不动
            int []last_direction = new int[]{before[0]-current[0],before[1]-current[1]};//注意这是上个方向的反方向
            int []next_direction = new int[]{next[0]-current[0],next[1]-current[1]};
            List<int[]> wrong_direct;
            wrong_direct = Arrays.asList(last_direction, next_direction, new int[]{0, 0});
            List<int []> directions = new ArrayList<int []>();// 存储可能去的方向
            int w_size = wrong_direct.size();
            for(int i=-1;i<2;i++){
                for(int j=-1;j<2;j++){
                    int k = 0;
                    for(;k< w_size;k++){
                        int [] t_direction = wrong_direct.get(k);
                        if(t_direction[0]==i&&t_direction[1]==j)
                            break;
                    }
                    if(k==w_size)
                        directions.add(new int[]{i,j});
                }
            }
            // 遍历所有可行的方向，寻找可能的避免碰撞的长度为3的直线路径
            for(int[] direct : directions) {

                if (isReachable(current[0], current[1], direct, map)
                        && isReachable(current[0] + direct[0], current[1] + direct[1], direct, map)
                        && isReachable(current[0] + 2 * direct[0], current[1] + 2 * direct[1], direct, map)) {
                    // 无商品直接添加路径，有商品需要多向前看一格
                    if(has_good==-1 || isReachable(current[0] + 3 * direct[0], current[1] + 3 * direct[1], direct, map)) {

                        List<int[]> temp_path = src_path.subList(start, i1 + 1);
                        safe_path = new ArrayList<>();
                        safe_path.addAll(temp_path);
                        // 下面三个位置，相当于新生成的路径
                        safe_path.add(new int[]{current[0] + direct[0], current[1] + direct[1]});
                        safe_path.add(new int[]{current[0] + direct[0] * 2, current[1] + direct[1] * 2});
                        safe_path.add(new int[]{current[0] + direct[0] * 3, current[1] + direct[1] * 3});
                        return safe_path;
                    }
                }

            }
            // 未找到，则去下一个位置寻找路径
            i1++;
        }
        //System.err.println("未找到路径");
        return null;
    }


    // 判断位置是否可达，可达位置的相邻方块的8个方向都没有墙
    public static boolean isReachable(int nowY,int nowX,int[] dircet,int [][]map){
        int nextY = nowY+dircet[0];
        int nextX = nowX+dircet[1];
        if(nowY >= map.length || nowY < 0 || nowX < 0 || nowX >= map[0].length)  return false;
        if(nextY >= map.length || nextY < 0 || nextX < 0 || nextX >= map[0].length)  return false;
        if(map[nowY][nowX]==-1)  return false;
        if(map[nextY][nextX]==-1)  return false;
        if(!aroundReachable(nowY,nowX,map)) return false;
        return true;
    }

    // 判读一个格子的8个相邻方向是否无墙且可达
    public static boolean aroundReachable(int nowY,int nowX,int[][]map){
        int[][] direction = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for(int i=0;i<direction.length;i++){
            int tmp_Y = nowY+direction[i][0];
            int tmp_X = nowX+direction[i][1];
            if(tmp_Y >= map.length || tmp_Y < 0 || tmp_X < 0 || tmp_X >= map[0].length)  return false;
            if(map[tmp_Y][tmp_X]==-1)  return false;
        }
        return true;
    }

    //
    public static void avoid_Congest(List<List<int[]>> robotsPath,int [][]map, int[][] safePlace,ArrayList<int []> [] left_pos_and_robot,ArrayList<Robot> robots) {
        for (int robotId = 0; robotId < 3; robotId++) {
            int otherId = robotId+1;
            List<int[]> cur_path = robotsPath.get(robotId);
            int cur_good = robots.get(robotId).getGoodID();
            while(otherId<4) {
                List<int []> other_path = robotsPath.get(otherId);
                if(other_path.size()>0&&cur_path.size()>0) {
                    int other_start_y = other_path.get(0)[0];
                    int other_start_x = other_path.get(0)[1];
                    int cur_start_y = cur_path.get(0)[0];
                    int cur_start_x = cur_path.get(0)[1];
                    int other_good = robots.get(otherId).getGoodID();
                    // 获取另外一个小车在当前小车路径上的索引位置
                    int other_path_index = isInPath(other_start_y, other_start_x, cur_path);
                    // 获取当前小车在另一个小车的路径上的索引位置
                    int cur_path_index = isInPath(cur_start_y, cur_start_x, other_path);

                    boolean other_way_status = true;//iSInSingleWay(other_start_y, other_end_x, map);
                    boolean cur_way_status =  true;//iSInSingleWay(cur_start_y, cur_end_x, map);

                    // 同时在窄道，且两车都在对方的路径上
                    if (other_path_index != -1 && cur_way_status && cur_path_index != -1 && other_way_status) {
                        // 双方寻找安全位置的路径
                        List<int[]> cur_safe_path = findSafePlace(other_path, cur_path_index, map, cur_good);
                        List<int[]> other_safe_path = findSafePlace(cur_path, other_path_index, map, other_good);
                        if(cur_safe_path==null&&other_safe_path==null){
                            //System.err.println("出现未考虑的情况");
                        }else if(cur_safe_path!=null&&other_safe_path==null){
                            updateStatus(robotsPath,robotId,otherId,cur_safe_path,safePlace,left_pos_and_robot);

                        }else if(cur_safe_path == null){
                            updateStatus(robotsPath,otherId,robotId,other_safe_path,safePlace,left_pos_and_robot);
                        }
                        else if (cur_safe_path.size() < other_safe_path.size()) {
//                            Tool.showPath(map, cur_path);
                            updateStatus(robotsPath,robotId,otherId,cur_safe_path,safePlace,left_pos_and_robot);
//                            Tool.showPath(map,cur_safe_path);
                        }
                        else {
//                            Tool.showPath(map,other_path);
                            updateStatus(robotsPath,otherId,robotId,other_safe_path,safePlace,left_pos_and_robot);
//                            Tool.showPath(map,other_path);
                        }
                    }
                }
                otherId++;
            }
        }
    }


    // 更新robotId的路径为安全路径，并设置岔路口信息，以及在安全位置等待的小车id, 以方便小车在通过岔路口后，可以被释放
    public static void updateStatus(List<List<int[]>> robotsPath,int robotId,int otherId,List<int[]> safe_path, int[][] safePlace,ArrayList<int []> [] left_pos_and_robot) {
        robotsPath.set(robotId, safe_path);
        int last_index = safe_path.size()-1;
        int leave_index = safe_path.size()-4;
        safePlace[robotId][0] = safe_path.get(last_index)[0];
        safePlace[robotId][1] = safe_path.get(last_index)[1];
        // 去安全位置小车  添加岔路口的位置以及走该条路径的小车
        left_pos_and_robot[otherId].add(new int[]{safe_path.get(leave_index)[0],safe_path.get(leave_index)[1],robotId});
    }


    // 检测小车是不是离开了岔路口的的位置，岔路口是指小车生成新路径开始的位置
    public static void free_robot(List<List<int[]>> robotsPath, int[][] safePlace,ArrayList<int []> [] left_pos_and_robots){
        for(int robotId =0;robotId<left_pos_and_robots.length;robotId++){
            ArrayList<int []> left_pos_and_robot = left_pos_and_robots[robotId];
            Iterator iterator = left_pos_and_robot.iterator();
            while (iterator.hasNext()){
                int [] y_x_robot = (int[]) iterator.next();
                int y = y_x_robot[0];
                int x = y_x_robot[1];
                int otherId = y_x_robot[2];
                List<int[]> cur_path = robotsPath.get(robotId);
                if(isInPath(y,x,cur_path)==-1){
                    iterator.remove();
                    safePlace[otherId][0]=-1;
                    safePlace[otherId][1]=-1;
                }
                ArrayList<int []> other_left_pos_and_robot = left_pos_and_robots[robotId];
                Iterator other_iterator = other_left_pos_and_robot.iterator();
                while (other_iterator.hasNext()){
                    int [] other_y_x_robot = (int[]) other_iterator.next();
                    if(other_y_x_robot[2]==robotId){
                        safePlace[otherId][0]=-1;
                        safePlace[otherId][1]=-1;
                        iterator.remove();
                    }
                }
            }
        }
    }
}
