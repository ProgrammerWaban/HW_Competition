package com.huawei.codecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    private static final BufferedReader inStream = new BufferedReader(new InputStreamReader(System.in));

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out), true);

    public static ThreadLocal<Integer> tl = new ThreadLocal<>();
    private static String team = "";  //表示红蓝双方
    public static ThreadLocal<Integer> who = new ThreadLocal<>();
    private static dispatchingCenter dc = new dispatchingCenter();
    private static ArrayList<Workbench> workbenches = new ArrayList<>();
    private static ArrayList<Robot> robots = new ArrayList<>();
    private static List<List<int[]>> robotsPath = new ArrayList<>();
    private static int[][] map;

    private static int [][] SafePlace = new int[][]{{-1,-1},{-1,-1},{-1,-1},{-1,-1}};

    private static  ArrayList<int []> [] left_pos_and_robot = new ArrayList[]{
            new ArrayList<int []>(),new ArrayList<int []>(),new ArrayList<int []>(),new ArrayList<int []>()
    };


    public static void main(String[] args) throws IOException, InterruptedException {
        // 如果在本地调试时不需要重启，在启动参数中添加restart，如：java -jar main.jar restart
        if (args.length <= 0) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("java", "-jar", "-Xmn512m", "-Xms1024m", "-Xmx1024m",
                "-XX:TieredStopAtLevel=1", "main.jar", "restart");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } else if (!args[0].equals("restart")) {
            System.out.println("err");
        } else {
            //do something
            //sleep(10000);
            schedule();
        }
    }

    private static void schedule() throws IOException {
//        long start = System.currentTimeMillis();
//        long stop = System.currentTimeMillis();
//        System.err.println(stop - start);
        initalUtilOK();
        who.set(0);
        //为每个工作台计算到各个点的距离
        for (Workbench wb : workbenches){
            double[][] distMatWithNoGood = new double[100][100];
            double[][] distMatWithGood = new double[100][100];
            for(int i = 0; i < 100; i++){
                for(int j = 0; j < 100; j++){
                    distMatWithNoGood[i][j] = Double.MAX_VALUE;
                    distMatWithGood[i][j] = Double.MAX_VALUE;
                }
            }
            //计算
            SearchAlgorithm.bfs(new int[]{wb.getxMap(), wb.getyMap()}, map, distMatWithNoGood, 0);
            SearchAlgorithm.bfs(new int[]{wb.getxMap(), wb.getyMap()}, map, distMatWithGood, 1);

            wb.setDistMatWithNoGood(distMatWithNoGood);
            wb.setDistMatWithGood(distMatWithGood);
        }
        //看看工作台是否在墙角，是就弄死
        for (Workbench wb : workbenches){
            //1 2 3工作台不需要弄死
            if(wb.getID() == 1 || wb.getID() == 2 || wb.getID() == 3)   continue;
            int n = 0;
            int[][] direction = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int[] d : direction){
                int wbNewX = wb.getxMap() + d[0];
                int wbNewY = wb.getyMap() + d[1];
                if(map[wbNewX][wbNewY] == -1)   n++;
            }
            if(n >= 2)  wb.setAlive(false);
        }
        //初始化结束
        outStream.print("OK\n");

        int frameID;
        String line;
        while ((line = inStream.readLine()) != null) {
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]);
            readUtilOK();
            StringBuilder builder = new StringBuilder();
            builder.append(frameID).append('\n');

            //记录当前帧
            tl.set(frameID);

            //检查当前位置
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                //检查是否到达目的地，并设置买卖行为
                robot.checkReachDestination(dc, robotId, workbenches);
                //进行卖行为
                if (robot.isSell()) builder.append("sell").append(' ').append(robotId).append('\n');
                if (robot.isBuy())  builder.append("buy").append(' ').append(robotId).append('\n');
            }

            //调度中心
            dc.dispatching(robots, workbenches);

            //计算 机器人的路线
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                int destinationID = dc.findDestinationIDByRobotID(robotId);
                Workbench wb = destinationID == -1 ? null : workbenches.get(destinationID);
                if(wb == null)  robotsPath.add(new ArrayList<>());
                else{
                    int[] startXY = robot.getMatXY();
                    robot.changeMapXY(map, startXY);
                    int[] stopXY;
                    if(SafePlace[robotId][0]==-1&&SafePlace[robotId][1]==-1)
                        stopXY = new int[]{wb.getxMap(), wb.getyMap()};
                    else
                        stopXY = new int[]{SafePlace[robotId][0],SafePlace[robotId][1]};
                    double[][] distMat = robot.getGoodID() == 0 ? wb.getDistMatWithNoGood() : wb.getDistMatWithGood();
                    int hasGood = robot.getGoodID() == 0 ? 0 : 1;
                    List<int[]> path;
                    if(SafePlace[robotId][0]==-1&&SafePlace[robotId][1]==-1)
                        path = SearchAlgorithm.astar(startXY, stopXY, map, distMat, hasGood);
                    else
                        path = SearchAlgorithm.astar(startXY, stopXY, map, hasGood, 1);
                    Collections.reverse(path);
                    robotsPath.add(path);
                }
            }

            // 在合适的位置，释放安全位置的小车
            AvoidCongest.free_robot(robotsPath,SafePlace,left_pos_and_robot);
            // 防堵车
            AvoidCongest.avoid_Congest(robotsPath,map,SafePlace,left_pos_and_robot,robots);

            //  打印输出路径
//            int[][] look = new int[map.length][map[0].length];
//            for(int i = 0; i < map.length; i++){
//                for(int j = 0; j < map[0].length; j++){
//                    look[i][j] = map[i][j];
//                }
//            }
//            for(List<int[]> p : robotsPath){
//                for(int[] pp : p){
//                    look[pp[0]][pp[1]] = -3;
//                }
//            }
//            for(int i = map.length - 1; i >= 0; i--){
//                for(int j = 0; j < map[0].length; j++){
//                    if(look[i][j] == -3)    System.err.print('+');
//                    if(look[i][j] == -2)    System.err.print(' ');
//                    if(look[i][j] == -1)    System.err.print('#');
//                    if(look[i][j] >= 0)    System.err.print(' ');
//                }
//                System.err.print("\n");
//            }

            //计算移动和旋转
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                int destinationID = dc.findDestinationIDByRobotID(robotId);
                Workbench wb = new Workbench();
                if(destinationID > -1){
                    List<int[]> path = robotsPath.get(robotId);
                    if(path.size() == 0)    continue;
                    int jump = BetterMove.binarySearchDestination(map, path, robot, wb);
                    if(jump == 0){
                        BetterMove.adjustMovement(wb, robot);
                    }else{
                        BetterMove.adjustMovementJump(wb, robot);
                    }
                }
                //计算到目的地所需要的Forward和Rotate
                //robot.calForwardAndRotate(wb);
                //BetterMove.adjustMovement(wb, robot);
                AvoidCollide.avoidTailgate(robot, robots);
//                //前进+旋转
//                outStream.printf("forward %d %f\n", robotId, robot.getWantForward());
//                outStream.printf("rotate %d %f\n", robotId, robot.getWantRotate());
            }

            //防碰撞
            AvoidCollide.avoid_Collide(robots);
            //DWA.avoid_Collide1(robots, dc, workbenches);

            //输出移动和旋转
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                builder.append("forward").append(' ').append(robotId).append(' ').append(robot.getWantForward()).append('\n');
                builder.append("rotate").append(' ').append(robotId).append(' ').append(robot.getWantRotate()).append('\n');
            }

            robotsPath.clear();
            robots.clear();

            builder.append("OK").append('\n');
            outStream.print(builder);
        }
    }

    private static boolean initalUtilOK() throws IOException {
        map = new int[100][100];
        int x = 99;
        List<Integer> WBList = new ArrayList<>();
        String line;
        while ((line = inStream.readLine()) != null) {
            if ("OK".equals(line)) {
                dc.constructMapWB(WBList);
                return true;
            }
            if ("BLUE".equals(line) || "RED".equals(line)) {
                team = line;
            } else {
                // do something;
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == '#') map[x][i] = -1;
                    else if (c == '.' || c == 'A' || c == 'B') map[x][i] = -2;
                    else if ("BLUE".equals(team) && c >= '1' && c <= '9') {
                        Workbench newWB = new Workbench();
                        newWB.setID(Integer.parseInt("" + c));
                        newWB.setNeeds();
                        newWB.setxMap(x);
                        newWB.setyMap(i);
                        workbenches.add(newWB);
                        map[x][i] = workbenches.size() - 1;
                        WBList.add(Integer.parseInt("" + c));
                    } else if ("RED".equals(team) && c >= 'a' && c <= 'i') {
                        Workbench newWB = new Workbench();
                        newWB.setID(Integer.parseInt("" + (c - 'a' + 1)));
                        newWB.setNeeds();
                        newWB.setxMap(x);
                        newWB.setyMap(i);
                        workbenches.add(newWB);
                        map[x][i] = workbenches.size() - 1;
                        WBList.add(Integer.parseInt("" + (c - 'a' + 1)));
                    }
                }
                x--;
            }
        }
        return false;
    }

    private static boolean readUtilOK() throws IOException {
        int i = 0;
        int j = 0; //用来遍历robots数组，存雷达信息
        String line;
        while ((line = inStream.readLine()) != null) {
            if ("OK".equals(line)) {
                return true;
            }
            // do something;
            String[] s = line.split(" ");
            //存工作台信息
            if (s.length == 6) {
                Workbench workbench = workbenches.get(i);
                workbench.setID(Integer.parseInt(s[0]));
                workbench.setX(Double.parseDouble(s[1]));
                workbench.setY(Double.parseDouble(s[2]));
                workbench.setRemain_frame(Integer.parseInt(s[3]));
                workbench.setRaw_old(workbench.getRaw());
                workbench.setRaw(Integer.parseInt(s[4]));
                workbench.setProduct_status_old(workbench.getProduct_status());
                workbench.setProduct_status(Integer.parseInt(s[5]));
                i++;
            } else if (s.length == 10) {
                Robot robot = new Robot(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Double.parseDouble(s[2]),
                        Double.parseDouble(s[3]), Double.parseDouble(s[4]), Double.parseDouble(s[5]),
                        Double.parseDouble(s[6]), Double.parseDouble(s[7]), Double.parseDouble(s[8]),
                        Double.parseDouble(s[9]));
                robots.add(robot);
            } else if (s.length == 360) {
                double[] doubles = Arrays.asList(s).stream().mapToDouble(Double::parseDouble).toArray();
                robots.get(j).setRadar(doubles);
                j++;
            }
        }
        return false;
    }
}
