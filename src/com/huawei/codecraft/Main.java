package com.huawei.codecraft;

import java.io.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class Main {

    private static final BufferedReader inStream = new BufferedReader(new InputStreamReader(System.in));

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out), true);

    public static ThreadLocal<Integer> tl = new ThreadLocal<>();
    public static String team = "";  //表示红蓝双方
    public static ThreadLocal<Integer> who = new ThreadLocal<>();
    private static dispatchingCenter dc = new dispatchingCenter();
    private static ArrayList<Workbench> workbenches = new ArrayList<>();
    public static ArrayList<Workbench> enemyWorkbenches = new ArrayList<>();
    public static ArrayList<Robot> robots = new ArrayList<>();
    public static ArrayList<Integer> robotsToAttack = new ArrayList<>();
    private static List<List<int[]>> robotsPath = new ArrayList<>();
    private static int[][] map;

    private static int [][] SafePlace = new int[][]{{-1,-1},{-1,-1},{-1,-1},{-1,-1}};

    private static  int [][][] leave_pos_and_robot = new int[][][]{
            {{-1,-1},{-1,-1},{-1,-1},{-1,-1}},
            {{-1,-1},{-1,-1},{-1,-1},{-1,-1}},
            {{-1,-1},{-1,-1},{-1,-1},{-1,-1}},
            {{-1,-1},{-1,-1},{-1,-1},{-1,-1}},
    };

    //永远弄死的工作台
    private static ArrayList<Workbench> dead_Workbench = new ArrayList<>();
    //记录一个工作台死了多少秒
    private static Map<Workbench, Integer> dead_time_wb = new HashMap<>();
    private static Map<Workbench, Integer> alive_time_wb = new HashMap<>();


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
        //判断机器人有无能去的工作台，决定是否去进攻
        for (int i = 0; i < robots.size(); i++) {
            Robot robot = robots.get(i);
            boolean canWork = false;
            for (Workbench wb : workbenches) {
                if (wb.getDistMatWithNoGood()[robot.getMatXY()[0]][robot.getMatXY()[1]] != Double.MAX_VALUE) {
                    canWork = true;
                    break;
                }
            }
            if(!canWork){
                robotsToAttack.add(i);
            }
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

            //进行地图深克隆
            int[][] map_clone = new int[map.length][];
            for (int i = 0; i < map.length; i++) {
                map_clone[i] = map[i].clone();
            }
            //改变map_clone
            if (team.equals("RED")) {
                for (Robot robot : robots) {
                    robot.radarCheck(map_clone, map, robots);
                }
            }

            //初始化下工作台状态
            ArrayList<Workbench> standOnWorkbench = null;
            if (team.equals("RED")) {
                standOnWorkbench = Tool.isStandOnWorkbench(map_clone, map, workbenches);
            }

            //如果一个工作台在50帧内被弄死了50次，就把这个工作台永远置为死
            if (team.equals("RED")) {
                for (Workbench workbench : workbenches) {
                    if (!workbench.isEnemyNotOn) {
                        Integer deadTime = dead_time_wb.getOrDefault(workbench, 0);
                        if (deadTime > 80 && !dead_Workbench.contains(workbench)) {
                            workbench.setAlive(false);
                            dead_Workbench.add(workbench);
                        } else if (deadTime <= 80) {
                            dead_time_wb.put(workbench, deadTime + 1);
                        }
                    } else {
                        if (dead_time_wb.keySet().contains(workbench)) {
                            dead_time_wb.remove(workbench);
                        }
                    }
                }
            }

            //调度中心
            dc.dispatching(robots, workbenches);

            //销毁物品
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                if(robot.isDestroy())   builder.append("destroy").append(' ').append(robotId).append('\n');
            }

            //如果一个工作台在10s(500帧)内都没有机器人在上面，那就让他复活
            if (team.equals("RED")) {
                Iterator<Workbench> iterator = dead_Workbench.iterator();
                while (iterator.hasNext()) {
                    Workbench workbench = iterator.next();
                    if (workbench.isEnemyNotOn) {
                        Integer aliveTime = alive_time_wb.getOrDefault(workbench, 0);
                        if (aliveTime > 500) {
                            workbench.setAlive(true);
                            iterator.remove();
                        } else {
                            alive_time_wb.put(workbench, aliveTime + 1);
                        }
                    } else {
                        //归零
                        if (alive_time_wb.keySet().contains(workbench)) {
                            alive_time_wb.remove(workbench);
                        }
                    }
                }
            }

            //要将刚才弄死的工作台还原一下
            if (team.equals("RED")) {
                for (Workbench onWorkbench : standOnWorkbench) {
                    onWorkbench.isEnemyNotOn = true;
                    //onWorkbench.setAlive(true);

                }
            }

            //计算 机器人的路线
            for (int robotId = 0; robotId < 4; robotId++) {

//                //测试用例
//                if (team.equals("BLUE") && robotId == 0) {
//                    Robot robot = robots.get(robotId);
//                    Workbench wb = new Workbench();
//                    wb.setyMap(30);
//                    wb.setxMap(29);
//                    int[] startXY = robot.getMatXY();
//                    int[] stopXY = new int[]{wb.getxMap(), wb.getyMap()};
//                    double[][] distMat = robot.getGoodID() == 0 ? wb.getDistMatWithNoGood() : wb.getDistMatWithGood();
//                    int hasGood = robot.getGoodID() == 0 ? 0 : 1;
//                    List<int[]> path;
//                    path = SearchAlgorithm.astar(startXY, stopXY, map_clone, hasGood, 1);
//                    Collections.reverse(path);
//                    robotsPath.add(path);
//                    continue;
//                }

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
                    //如果终点不可达，就直接返回空路径，不然找遍全地图
                    if(map_clone[stopXY[0]][stopXY[1]] == -1 || !wb.isEnemyNotOn){
                        robotsPath.add(new ArrayList<>());
                        continue;
                    }
                    double[][] distMat = robot.getGoodID() == 0 ? wb.getDistMatWithNoGood() : wb.getDistMatWithGood();
                    int hasGood = robot.getGoodID() == 0 ? 0 : 1;
                    List<int[]> path;
                    if(SafePlace[robotId][0]==-1&&SafePlace[robotId][1]==-1)
                        path = SearchAlgorithm.astar(startXY, stopXY, map_clone, distMat, hasGood);
                    else
                        path = SearchAlgorithm.astar(startXY, stopXY, map_clone, hasGood, 1);
                    Collections.reverse(path);
                    robotsPath.add(path);
                }
            }

            //打印输出地图和路径
            boolean f = false;
            if (f) {
                Tool.printMap(map_clone, robotsPath);
            }

            // 在合适的位置，释放安全位置的小车
            AvoidCongest.free_robot(robotsPath,SafePlace,leave_pos_and_robot);
            // 防堵车
            AvoidCongest.avoid_Congest(robotsPath,map,SafePlace,leave_pos_and_robot,robots);

            //计算移动和旋转
            for (int robotId = 0; robotId < 4; robotId++) {
                Robot robot = robots.get(robotId);
                int destinationID = dc.findDestinationIDByRobotID(robotId);
                Workbench wb = new Workbench();
                if(destinationID > -1){
                    List<int[]> path = robotsPath.get(robotId);
                    if (path.size() == 0 || path.size() == 1) {
                        //防止原地抽搐
                        wb = workbenches.get(destinationID);
                        BetterMove.adjustMovement(wb, robot);
                        continue;
                    }
                    int jump = BetterMove.binarySearchDestination(map, path, robot, wb);
                    if(jump == 0){
                        BetterMove.adjustMovement(wb, robot);
                    }else{
                        BetterMove.adjustMovementJump(wb, robot);
                    }
                }
                //靠经墙壁减速(其实这里也可以设置成只有红方才执行)
                //BetterMove.closeToWall_slowDown(map_clone, robot, 1.8);
                //计算到目的地所需要的Forward和Rotate
                //robot.calForwardAndRotate(wb);
                //BetterMove.adjustMovement(wb, robot);
                //AvoidCollide.avoidTailgate(robot, robots);
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
                    else if (c == '.' || c == 'A' || c == 'B'){
                        map[x][i] = -2;
                        if ("BLUE".equals(team) && c == 'A') {
                            robots.add(new Robot(i * 0.5 + 0.25, x * 0.5 + 0.25));
                        }
                        if ("RED".equals(team) && c == 'B') {
                            robots.add(new Robot(i * 0.5 + 0.25, x * 0.5 + 0.25));
                        }
                    }
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
                    } else if ("BLUE".equals(team) && c >= 'a' && c <= 'i') {
                        Workbench newWB = new Workbench();
                        newWB.setID(Integer.parseInt("" + (c - 'a' + 1)));
                        newWB.setxMap(x);
                        newWB.setyMap(i);
                        newWB.setX(i * 0.5 + 0.25);
                        newWB.setY(x * 0.5 + 0.25);
                        enemyWorkbenches.add(newWB);
                    } else if ("RED".equals(team) && c >= '1' && c <= '9') {
                        Workbench newWB = new Workbench();
                        newWB.setID(Integer.parseInt("" + c));
                        newWB.setxMap(x);
                        newWB.setyMap(i);
                        newWB.setX(i * 0.5 + 0.25);
                        newWB.setY(x * 0.5 + 0.25);
                        enemyWorkbenches.add(newWB);
                    }
                }
                x--;
            }
        }
        return false;
    }

    private static boolean readUtilOK() throws IOException {
        int i = 0;  //记录第几个工作台
        int j = 0; //用来遍历robots数组，存雷达信息
        int k = 0;  //记录第几个机器人
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
                Robot robot = robots.get(k);
                robot.robotUpdate(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Double.parseDouble(s[2]),
                        Double.parseDouble(s[3]), Double.parseDouble(s[4]), Double.parseDouble(s[5]),
                        Double.parseDouble(s[6]), Double.parseDouble(s[7]), Double.parseDouble(s[8]),
                        Double.parseDouble(s[9]));
                robot.setBuy(false);
                robot.setSell(false);
                robot.setDestroy(false);
                k++;
            } else if (s.length == 360) {
                double[] doubles = Arrays.asList(s).stream().mapToDouble(Double::parseDouble).toArray();
                robots.get(j).setRadar(doubles);
                j++;
            }
        }
        return false;
    }
}
