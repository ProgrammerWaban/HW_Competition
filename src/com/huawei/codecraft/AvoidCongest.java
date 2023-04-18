package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



class Result{
    public List<int[]> path;
    public int leave_index;

    public Result(){
        this.path = null;
        leave_index = -2;
    }

}

public class AvoidCongest {

    // 判断当前位置是否在单行道上, 距离为1的对角线不能有障碍，距离为2的对角线最多有一个障碍
    public static boolean iSInSingleWay(int y,int x,int [][]map){
        // 横向两个格子和纵向两个格子没有障碍物
        int count = 0;
        int count1 = 0;
        int [][]directions = new int[][]{{-1,1},{-1,-1},{1,-1},{1,1}};
        // 记录对角线方向的空的位置数
        for (int[] direction : directions) {
            int tmp_y = y + direction[0];
            int tmp_x = x + direction[1];
            int tmp_y_1 = y * 2 + direction[0];
            int tmp_x_1 = x * 2 + direction[1];
            if (tmp_y < map.length && tmp_y >= 0 && tmp_x < map.length && tmp_x >= 0 && map[tmp_y][tmp_x] != -1) {
                count++;
            }

            if (tmp_y_1 < map.length && tmp_y_1 >= 0 && tmp_x_1 < map.length && tmp_x_1 >= 0 && map[tmp_y_1][tmp_x_1] != -1) {
                count1++;
            }
        }
        if(count<4)
            return true;
        else return count1 < 3;

    }

    // length只少为 1, 已y,x为起点，direction 方向长度在length 内的位置是否有障碍点
    public static boolean isLineBarrier(int y,int x,int []direction,int length,int [][]map){
        int next_y = y;
        int next_x = x;
        for(int i=0;i<length;i++){
            next_y = next_y + direction[0];
            next_x = next_x + direction[1];
            if(next_y >= map.length || next_y < 0 || next_x < 0 || next_x >= map[0].length || map[next_y][next_x]==-1)  return true;
        }
        return false;
    }

//    public static boolean isAffectOtherSafePlace(int target_y,int target_x,int robotId,int [][] safePlace){
//        for(int i=0;i<safePlace.length;i++){
//            if(i==robotId||(safePlace[i][0]==-1&&safePlace[i][1]==-1))
//                continue;
//            int other_y = safePlace[i][0];
//            int other_x = safePlace[i][1];
//            int dy = other_y - target_y;
//            int dx = other_x - target_x;
//            if(dy*dy+dx*dx < 16)
//                return true;
//
//        }
//        return false;
//    }



    public static boolean iSInSingleWay(List<int[]> path,int [][]map) {
        int path_length = path.size();
        if(path_length<2){
            int y = path.get(0)[0];
            int x = path.get(0)[1];
            return iSInSingleWay(y,x,map);
        }
        int available_length = Math.min(path_length,4);
        for(int i=0;i<available_length-1;i++){
            int y = path.get(i)[0];
            int x = path.get(i)[1];
            int next_y = path.get(i+1)[0];
            int next_x = path.get(i+1)[1];
            // 移动方向
            int [] move_direction = new int []{next_y-y,next_x-x};
            int [] vertical_direction_1, vertical_direction_2;
            if(move_direction[0]==0||move_direction[1]==0){
                vertical_direction_1 = new int []{move_direction[1],move_direction[0]};
                vertical_direction_2 = new int [] {-move_direction[1],-move_direction[0]};
            }else{
                vertical_direction_1 = new int []{move_direction[0],-move_direction[1]};
                vertical_direction_2 = new int [] {-move_direction[0],move_direction[1]};
            }
            if(isLineBarrier(next_y,next_x,vertical_direction_1,2,map)||isLineBarrier(next_y,next_x,vertical_direction_2,2,map)){
                return true;
            }

            if(move_direction[0]!=0&&move_direction[1]!=0) {
                int temp_y = y+move_direction[0];
                int temp_x = x+move_direction[1];
                if(isLineBarrier(temp_y,x,vertical_direction_1,1,map)||isLineBarrier(y,temp_x,vertical_direction_2,1,map)){
                    return true;
                }
            }

        }
        return false;

    }

    // 判读一个坐标是否在路径上，在路径上的定义为横向距离为1，纵向距离为0，横向距离为0，纵向距离为1，或横向距离为0，纵向距离为0
    // 存在则返回距离最近的索引，否则返回-1，表示该位置不在路径上。
    public static int isInPath(int y,int x,List<int[]> path){
        int length =path.size();
        length = Math.min(length, 12);
        int start;
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
    public static Result findSafePlace(List<int []>src_path, int start, int end, int [][]map, int has_good, int robotId,int [][] safePlace){
        Result result = new Result();
        List<int[]> safe_path;
        if(start==-1){
//            System.err.println("无法生成安全路径，索引位置不在路径上");
            return result;
        }

        int cur_index = start;
        if(end <2){
//            System.err.println("无法生成安全路径，路径长度为1，无法判断将要去的方向");
            return result;
        }

        while(cur_index< end){
            // 分别表示当前位置，下一个位置和上一个位置
            int []current = src_path.get(cur_index);
            int []next;
            int []before;
            // 在第一个位置，不知到上一个位置，则用下一个位置预估上一个位置
            if(cur_index-1>=0)
                before = src_path.get(cur_index-1);
            else {
                next = src_path.get(cur_index+1);
                before = new int[]{2 * current[0] - next[0], 2 * current[1] - next[1]};
            }
            // 在第最后一个位置，不知到下一个位置，则用上一个位置预估下一个位置
            if(cur_index+1< end)
                next = src_path.get(cur_index+1);
            else
                next = new int[]{2*current[0]-before[0],2*current[1]-before[1]};

            if(!aroundReachable(current[0],current[1],map)){
//                System.err.printf("无法生成安全路径，路径上的 (%d,%d) 位置周围存在障碍%n",current[0],current[1]);
                return result;
            }


            // 错误的方向，指该位置接下来移动的方向，上一个移动方向的反方向，或原地不动
            int []last_direction = new int[]{before[0]-current[0],before[1]-current[1]};//注意这是上个方向的反方向
            int []next_direction = new int[]{next[0]-current[0],next[1]-current[1]};
            int[][] directions = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
            // 遍历所有可行的方向，寻找可能的避免碰撞的长度为3的直线路径
            for(int[] direct : directions) {
                if((direct[0]==last_direction[0]&&direct[1]==last_direction[1])||(direct[0]==next_direction[0]&&direct[1]==next_direction[1]))
                    continue;
                if (isReachable(current[0], current[1], direct, map)
                        && isReachable(current[0] + direct[0], current[1] + direct[1], direct, map)) {
                    // 无商品直接添加路径，有商品需要多向前看一格
                    int target_y,target_x;
                    if(has_good==0){
                        target_y = current[0]+2*direct[0];
                        target_x = current[1]+2*direct[1];
                    }else {
                        target_y = current[0]+3*direct[0];
                        target_x = current[1]+3*direct[1];
                    }
                    if(!isOverMinDis(src_path,cur_index,target_y,target_x))
                        continue;

//                    if(!isAffectOtherSafePlace(target_y,target_x,robotId,safePlace))
//                        continue;



                    if(has_good==0 || isReachable(current[0] + 2 * direct[0], current[1] + 2 * direct[1], direct, map)) {

                        List<int[]> temp_path = src_path.subList(start, cur_index + 1);
                        safe_path = new ArrayList<>(temp_path);
                        // 下面三个位置，相当于新生成的路径
                        safe_path.add(new int[]{current[0] + direct[0], current[1] + direct[1]});
                        safe_path.add(new int[]{current[0] + direct[0] * 2, current[1] + direct[1] * 2});
                        if(isReachable(current[0] + 2 * direct[0], current[1] + 2 * direct[1], direct, map)) {
                            safe_path.add(new int[]{current[0] + direct[0] * 3, current[1] + direct[1] * 3});
                            result.path = safe_path;
                            result.leave_index = safe_path.size()-4;
                            return result;
                        }
                        result.path = safe_path;
                        result.leave_index = safe_path.size()-3;
                        return result;
                    }
                }

            }
            // 未找到，则去下一个位置寻找路径
            cur_index++;
        }
//        System.err.println("已搜索完整条路径，未找到安全路径");
        return result;
    }

    public static boolean isOverMinDis(List<int []>src_path,int start,int target_y, int target_x){
        int start_index = Math.max(0,start-2);
        int end_index = Math.min(start+3,src_path.size());
        for(int i=start_index;i<end_index;i++){
            int [] tmp = src_path.get(i);
            int dy = tmp[0] - target_y;
            int dx = tmp[1] - target_x;
            if((dy*dy+dx*dx)<4)
                return false;
        }
        return true;
    }

    public static boolean isFarAwaySafePlace(int robotId, int target_y,int target_x,int [][] safePlace){
        for(int i=0;i<safePlace.length;i++){
            if(i==robotId)
                continue;
            int py = safePlace[i][0];
            int px = safePlace[i][0];
            if(py!=-1&&px!=-1){
                int dy = target_y - py;
                int dx = target_x - px;
                if((dy*dy+dx*dx)<16)
                    return false;
            }
        }
        return true;
    }
    // 判断位置是否可达，可达位置的相邻方块的8个方向都没有墙
    public static boolean isReachable(int nowY,int nowX,int[] dircetion,int [][]map){
        int nextY = nowY+dircetion[0];
        int nextX = nowX+dircetion[1];
        if(nowY >= map.length || nowY < 0 || nowX < 0 || nowX >= map[0].length)  return false;
        if(nextY >= map.length || nextY < 0 || nextX < 0 || nextX >= map[0].length)  return false;
        if(map[nowY][nowX]==-1)  return false;
        if(map[nextY][nextX]==-1)  return false;
        return aroundReachable(nextY, nextX, map);
    }

    // 判读一个格子的8个相邻方向是否无墙且可达
    public static boolean aroundReachable(int nowY,int nowX,int[][]map){
        int[][] directions = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        for (int[] direction : directions) {
            int tmp_Y = nowY + direction[0];
            int tmp_X = nowX + direction[1];
            if (tmp_Y >= map.length || tmp_Y < 0 || tmp_X < 0 || tmp_X >= map[0].length) return false;
            if (map[tmp_Y][tmp_X] == -1) return false;
        }
        return true;
    }

    //
    public static void avoid_Congest(List<List<int[]>> robotsPath,int [][]map, int[][] safePlace,int[][][] leave_pos_and_robot,ArrayList<Robot> robots) {
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

                    boolean other_way_status = iSInSingleWay(other_path,map);
                    boolean cur_way_status =  iSInSingleWay(cur_path,map);

                    // 同时在窄道，且两车都在对方的路径上
                    if (other_path_index != -1 && cur_way_status && cur_path_index != -1 && other_way_status) {
                        // 双方寻找安全位置的路径
                        int cur_end = other_path.size();
                        Result cur_result = findSafePlace(other_path, cur_path_index,cur_end, map,cur_good, robotId,safePlace);
                        List<int[]> cur_safe_path = cur_result.path;
                        int cur_leave_index = cur_result.leave_index;

                        int other_end = cur_path.size();
                        Result other_result = findSafePlace(cur_path, other_path_index,other_end, map,other_good,otherId, safePlace);
                        List<int[]> other_safe_path = other_result.path;
                        int other_leave_index = other_result.leave_index;

                        if(cur_safe_path==null){
                            List<int[]> tmp_path = other_path.subList(0,cur_path_index);
                            Collections.reverse(tmp_path);
                            cur_result = findSafePlace(tmp_path, 0,tmp_path.size()/2, map,cur_good, robotId,safePlace);
                            cur_safe_path = cur_result.path;
                            cur_leave_index = cur_result.leave_index;

                        }

                        if(other_safe_path==null){
                            List<int[]> tmp_path = cur_path.subList(0,other_path_index);
                            Collections.reverse(tmp_path);
                            other_result = findSafePlace(tmp_path, 0,tmp_path.size()/2, map,other_good, otherId,safePlace);
                            other_safe_path = other_result.path;
                            other_leave_index = other_result.leave_index;
                        }

                        if(cur_safe_path==null&&other_safe_path==null){
//                            System.err.printf("(%d, %d)都无法计算安全路径%n",robotId,otherId);
                        }else if(cur_safe_path!=null&&other_safe_path==null){
                            updateStatus(robotsPath,robotId,otherId,cur_safe_path,safePlace,cur_leave_index,leave_pos_and_robot);
                        }else if(cur_safe_path == null){
                            updateStatus(robotsPath,otherId,robotId,other_safe_path,safePlace,other_leave_index,leave_pos_and_robot);
                        }
                        else if (cur_safe_path.size() < other_safe_path.size()) {
                            updateStatus(robotsPath,robotId,otherId,cur_safe_path,safePlace,cur_leave_index,leave_pos_and_robot);
                        }
                        else {
                            updateStatus(robotsPath,otherId,robotId,other_safe_path,safePlace,other_leave_index,leave_pos_and_robot);
                        }
                    }
                } else if (other_path.size() == 0 && cur_path.size() > 0 || other_path.size() > 0) {
//                    System.err.println("出现路径为0的情况");
                    int MoveID = other_path.size()==0?otherId:robotId;
                    int tmp_ID = other_path.size()>0?otherId:robotId;
                    Robot move_robot = robots.get(MoveID);
                    int good_status = move_robot.getGoodID();
                    int move_x = (int) Math.floor(move_robot.getX()*2);
                    int move_y = (int)Math.floor(move_robot.getY()*2);
                    List<int[]> target_path = other_path.size()==0?cur_path:other_path;
                    int target_index = isInPath(move_y, move_x, target_path);
                    if(target_index!=-1) {
                        int target_end = target_path.size();
                        Result result = findSafePlace(target_path, target_index, target_end, map, good_status, MoveID, safePlace);
                        if (result.path != null) {
                            updateStatus(robotsPath, MoveID, tmp_ID, result.path, safePlace, result.leave_index, leave_pos_and_robot);
                        }
                    }

                }

                otherId++;
            }
        }
    }


    // 更新robotId的路径为安全路径，并设置岔路口信息，以及在安全位置等待的小车id, 以方便小车在通过岔路口后，可以被释放
    public static void updateStatus(List<List<int[]>> robotsPath,int robotId,int otherId,List<int[]> safe_path, int[][] safePlace,int leave_index,int [][][] leave_pos_and_robot) {
        robotsPath.set(robotId, safe_path);
        int last_index = safe_path.size()-1;
        safePlace[robotId][0] = safe_path.get(last_index)[0];
        safePlace[robotId][1] = safe_path.get(last_index)[1];
        // 去安全位置小车  添加岔路口的位置以及走该条路径的小车
        leave_pos_and_robot[otherId][robotId][0] = safe_path.get(leave_index)[0];
        leave_pos_and_robot[otherId][robotId][1] = safe_path.get(leave_index)[1];
    }


    // 检测小车是不是离开了岔路口的的位置，岔路口是指小车生成新路径开始的位置
    public static void free_robot(List<List<int[]>> robotsPath, int[][] safePlace,int[][][] leave_pos_and_robots) {
        for (int robotId = 0; robotId < leave_pos_and_robots.length; robotId++) {
            int[][] leave_pos_and_robot = leave_pos_and_robots[robotId];
            List<int[]> cur_path = robotsPath.get(robotId);
            for (int waitID = 0; waitID < leave_pos_and_robot.length; waitID++) {
                if(robotId==waitID)
                    continue;
                int y = leave_pos_and_robot[waitID][0];
                int x = leave_pos_and_robot[waitID][1];
                // 岔路口存在
                if(y!=-1&&x!=-1){
                    if(isInPath(y,x,cur_path)==-1){
                        safePlace[waitID][0]=-1;
                        safePlace[waitID][1]=-1;
                        leave_pos_and_robot[waitID][0]=-1;
                        leave_pos_and_robot[waitID][1]=-1;
                    }else{
                        int[][] other_leave_pos_and_robot = leave_pos_and_robots[waitID];
                        if(other_leave_pos_and_robot[robotId][0]!=-1&&other_leave_pos_and_robot[robotId][1]!=-1) {
                            safePlace[waitID][0]=-1;
                            safePlace[waitID][1]=-1;
                            leave_pos_and_robot[waitID][0]=-1;
                            leave_pos_and_robot[waitID][1]=-1;
                        }
                    }
                }
            }
        }
    }

    public static void showPath(List<int[]> p,int [][]map,int robotId){
        int[][] look = new int[map.length][map[0].length];
        for(int i = 0; i < map.length; i++){
            System.arraycopy(map[i], 0, look[i], 0, map[0].length);
        }
        for(int[] pp : p){
            look[pp[0]][pp[1]] = -3;
        }
        for(int i = map.length - 1; i >= 0; i--){
            for(int j = 0; j < map[0].length; j++){
                if(look[i][j] == -3)    System.err.print(robotId);
                if(look[i][j] == -2)    System.err.print(' ');
                if(look[i][j] == -1)    System.err.print('#');
                if(look[i][j] >= 0)    System.err.print(' ');
            }
            System.err.print("\n");
        }
    }
}
