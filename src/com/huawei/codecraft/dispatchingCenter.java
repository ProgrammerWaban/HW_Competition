package com.huawei.codecraft;

import java.util.*;

public class dispatchingCenter {
//    //保存地图初始化记录的工作台列表，下标为工作台号，内容为工作台类型
//    List<Integer> WBList = null;
    //1 2 3工作台都被预定了使一个机器人宕机，就用这个跳过预定
    private List<Integer> skip = new ArrayList<>();
    //每种物品的价值
    private final double[] valueArray = {0, 3000, 3200, 3400, 7100, 7800, 8300, 29000, 0, 0};
    //记录robots的目的工作台的ID
    private int[] robotsDestinationID = new int[]{-1, -1, -1, -1};
    //记录robots的next目的工作台的ID
    private int[] robotsNextDestinationID = new int[]{-1, -1, -1, -1};
    //记录robots预定的收购工作台的出售名额是否预定
    private int[] isBookSellBuyWB = new int[]{0, 0, 0, 0};
    //记录(商品ID,[工作台列表])  收购该商品的工作台   初始化后不变
    private HashMap<Integer, List<Integer>> mapWB = new HashMap<>();
    //记录(商品ID,[被占用的工作台列表])  收购该商品的工作台
    private HashMap<Integer, List<Integer>> mapWBX = new HashMap<>();
    //记录<被占用的工作台列表>  出售该商品的工作台
    private HashSet<Integer> setWBX = new HashSet<>();

    public dispatchingCenter() {
        skip.add(1);skip.add(2);skip.add(3);
    }

    //根据robot的编号找自己的目的工作台ID
    public int findDestinationIDByRobotID(int robotID){
        return robotsDestinationID[robotID];
    }

    //根据robot的编号找自己的next目的工作台ID
    public int findNextDestinationIDByRobotID(int robotID){
        return robotsNextDestinationID[robotID];
    }

    //修改robot的目的工作台的ID
    public void changeRobotDestinationIDByRobotID(int robotID, int destinationID){
        robotsDestinationID[robotID] = destinationID;
    }

    //修改robot的next目的工作台的ID
    public void changeRobotNextDestinationIDByRobotID(int robotID, int destinationID){
        robotsNextDestinationID[robotID] = destinationID;
    }

    //根据商品ID获取所有工作台列表
    public List<Integer> getWBListByGoodID(int goodID){
        return mapWB.get(goodID);
    }

    //根据商品ID获取被占用的工作台列表
    public List<Integer> getWBXListByGoodID(int goodID){
        return mapWBX.get(goodID);
    }

    //判断工作台的物质是否被预定
    public boolean isBookWB(int WBID){
        return setWBX.contains(WBID);
    }

    //释放被预定的出售物品工作台
    public void delete_setWBXWithWBID(int WBID){
        setWBX.remove(WBID);
    }

    //构建mapWB
    public void constructMapWB(List<Integer> WBList){
        //this.WBList = WBList;
        //int[][] a = new int[][]{{}, {}, {}, {}, {1, 2}, {1, 3}, {2, 3}, {4, 5, 6}, {7}, {1, 2, 3, 4, 5, 6, 7}};
        int[][] a = new int[][]{{}, {}, {}, {}, {1, 2}, {1, 3}, {2, 3}, {4, 5, 6}, {7}, {4, 5, 6, 7}};
        for(int i = 0; i < WBList.size(); i++){
            int[] aa = a[WBList.get(i)];
            for(int aaa : aa){
                List<Integer> aaaList = mapWB.getOrDefault(aaa, new ArrayList<>());
                aaaList.add(i);
                mapWB.put(aaa, aaaList);
            }
        }
    }

    //根据商品ID，预定收购该商品的工作台
    public void addWBXByGoodIDAndWBID(int goodID, int WBID){
        List<Integer> bookWBList = mapWBX.getOrDefault(goodID, new ArrayList<>());
        bookWBList.add(WBID);
        mapWBX.put(goodID, bookWBList);
    }

    //根据商品ID，释放收购该商品的工作台
    public void deleteWBXByGoodIDAndWBID(int goodID, int WBID){
        List<Integer> bookWBList = mapWBX.get(goodID);
        bookWBList.remove(Integer.valueOf(WBID));
        mapWBX.put(goodID, bookWBList);
    }

    //调度中心入口（找工作台）
    public void dispatching(ArrayList<Robot> robots, ArrayList<Workbench> workbenches) {
//        //计算工作台优先级，并设置饥饿帧
//        calWorkBenchPriorityAndSetHungryFrame(workbenches);
        //分组，一组买(包括无目标),一组卖     只分组下标
        List<Integer> robotsToBuySell = new ArrayList<>();
        List<Integer> robotsToSell = new ArrayList<>();
        for (int robotId = 0; robotId < 4; robotId++) {
            //无目的地  放入robotsToBuySell
            if(robotsDestinationID[robotId] == -1 && robotsNextDestinationID[robotId] == -1){
                robotsToBuySell.add(robotId);
            }
            //去买    放入robotsToBuySell
            if(robotsDestinationID[robotId] != -1 && robotsNextDestinationID[robotId] != -1){
                robotsToBuySell.add(robotId);
            }
            //去卖    放入robotsToSell
            if(robotsDestinationID[robotId] != -1 && robotsNextDestinationID[robotId] == -1){
                robotsToSell.add(robotId);
            }
        }
        findRoadToSell(robotsToSell, robots, workbenches);
        findRoadToBuyAndSell(robotsToBuySell, robots, workbenches);
    }

    //计算工作台优先级，并设置饥饿帧
    public void calWorkBenchPriorityAndSetHungryFrame(ArrayList<Workbench> workbenches){
        //根据7工作台来判断是那张地图
        int numOfSeven = 0;
        for(Workbench wb : workbenches){
            if(wb.getID() == 7) numOfSeven++;
        }
        switch (numOfSeven) {
            case 8:
                optimizationForOne(workbenches);
                break;
            case 2:
                optimizationForTwo(workbenches);
                break;
            case 0:
                optimizationForThree(workbenches);
                break;
            case 1:
                optimizationForFour(workbenches);
                break;
            default:
                optimizationForOther(workbenches);
                break;
        }
    }

    //针对第一张地图来优化
    public void optimizationForOne(ArrayList<Workbench> workbenches){
        Main.who.set(1);
        //1,2,3,4,6,8,9,11,12,13,14,15,17,18,19,21,22,25,26,27,29,30,31,33,34,35,36,37,38,39,40
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 6, 8, 9, 11, 12, 13, 14, 15, 17, 18, 19, 21, 22, 24, 25, 26, 27, 29, 30, 31, 33, 34, 35, 36, 37, 38, 39, 40);
        for (int i = 0; i < workbenches.size(); i++) {
            Workbench workbench = workbenches.get(i);
            if (numbers.contains(i)) {
                workbenches.get(i).setAlive(false);
            }

//            if (workbench.getID() == 9) {
//                workbench.setHungryFrame(-2000);
//            }

            if (workbench.getRaw() != 0) {
                if (workbench.getID() == 4) {
                    workbench.setHungryFrame(workbench.getHungryFrame() + 5);
                } else {
                    workbench.setHungryFrame(workbench.getHungryFrame() + 5);
                }
            }
            if (workbench.getRaw_old() != 0 && workbench.getRaw() == 0) {
                workbench.setHungryFrame(0);
            }
        }

        //最后15s
        int remainTime = (9000 - Main.tl.get()) / 50;
        if (remainTime < 15) {
            for (Workbench wb : workbenches) {
                if (wb.getID() == 9) {
                    wb.setAlive(true);
                }
            }
        }
    }
    //针对第二张地图来优化
    public void optimizationForTwo(ArrayList<Workbench> workbenches){
        Main.who.set(2);
        //0,9,15,24
        List<Integer> numbers = Arrays.asList(0, 9, 15, 24);
        for (int i = 0; i < workbenches.size(); i++) {
            if (numbers.contains(i)) {
                workbenches.get(i).setAlive(false);
            }
            Workbench wb = workbenches.get(i);
            if (wb.getID() == 6) wb.setHungryFrame(wb.getHungryFrame() + 10);
            if (wb.getID() == 6 && wb.getRaw_old() != 0 && wb.getRaw() == 0) wb.setHungryFrame(0);
        }
    }
    //针对第三张地图来优化
    public void optimizationForThree(ArrayList<Workbench> workbenches){
        Main.who.set(3);
        //ArrayList<Integer> deadList = new ArrayList<Integer>(Arrays.asList(1, 3, 8, 10, 14, 20, 25, 26, 29, 30, 47, 48, 49));
        //  0,1,2,3,4,5,6,7,8,9,10,15,17,18,19,21,26,27,30,31,35,36,38,40,41,42,43,44,45,46,47,48,49
        ArrayList<Integer> deadList = new ArrayList<Integer>(Arrays.asList(1,2,8,9,10,15,17,18,19,20,21,23,25,26,27,30,31,35,36,38,40,41,42,43,44,45,46,47,48,49));
        int size = workbenches.size();
        int j = 0;
        int deadList_size = deadList.size();
        int tmp = deadList.get(j);
        for (int i = 0; i < size; i++) {
            if (tmp == i) {
                workbenches.get(i).setAlive(false);
                j++;
                if (j < deadList_size) {
                    tmp = deadList.get(j);
                } else {
                    break;
                }
            }
        }
        for (Workbench wb : workbenches) {
            if (wb.getID() == 9) wb.setHungryFrame(10000);
        }
        //最后10s
        int remainTime = (9000 - Main.tl.get()) / 50;
        if (remainTime < 10) {
            for (Workbench wb : workbenches) {
                wb.setAlive(true);
            }
        }
    }
    //针对第四张地图来优化
    public void optimizationForFour(ArrayList<Workbench> workbenches){
        Main.who.set(4);
        List<Integer> numbers = Arrays.asList(12, 13, 14);
        for (int i = 0; i < workbenches.size(); i++) {
            if (numbers.contains(i)) {
                workbenches.get(i).setAlive(false);
            }
        }
        for(Workbench wb : workbenches){
            if(wb.getID() == 5 && wb.getRaw() != 0) wb.setHungryFrame(wb.getHungryFrame() + 10);
            if(wb.getID() == 4) wb.setHungryFrame(wb.getHungryFrame() + 150);
            if(wb.getID() == 4 && wb.getRaw_old() != 0 && wb.getRaw() == 0)    wb.setHungryFrame(0);
        }
        //最后10s
        int remainTime = (9000 - Main.tl.get()) / 50;
        if (remainTime < 10) {
            for (Workbench wb : workbenches) {
                wb.setAlive(true);
            }
        }
    }
    //针对其他地图来优化
    public void optimizationForOther(ArrayList<Workbench> workbenches){

    }

    //为去买卖和无目标的机器人看看有没有更佳的路线
    public void findRoadToBuyAndSell(List<Integer> robotsToBuySell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        if(robotsToBuySell.size() == 0) return;
        //取消去买去卖机器人的目的地、next目的地,取消预定
        for(Integer robotId : robotsToBuySell){
            if(robotsDestinationID[robotId] != -1 && robotsNextDestinationID[robotId] != -1){
                setOffDestAndNextDestAndNoBookWB(robotId, robotsDestinationID[robotId], robotsNextDestinationID[robotId], isBookSellBuyWB[robotId], workbenches);
            }
        }
        //计算买卖路线    [robotId, dest, nextDest, isBook]
        List<int[]> bestRoad = calBestRoadToBuyAndSell(robotsToBuySell, robots, workbenches);
        //设置目的地和next目的地，并且预定生产工作台和收购工作台
        for(int[] a : bestRoad){
            //无目的地，就只设置目的地和next目的地为-1
            if(a[1] == -1 && a[2] == -1){
                robotsDestinationID[a[0]] = -1;
                robotsNextDestinationID[a[0]] = -1;
            }
            //如果有目的地，就设置目的地和next目的地，并预定生产工作台和收购工作台
            if(a[1] != -1 && a[2] != -1){
                setDestAndNextDestAndBookWB(a[0], a[1], a[2], a[3], workbenches);
            }
        }
    }

    //为去卖的机器人看看有没有更佳的路线
    public void findRoadToSell(List<Integer> robotsToSell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        if(robotsToSell.size() == 0)    return;
        //取消预定的收购工作台，并取消目的地
        for(Integer robotId : robotsToSell){
            deleteWBXByGoodIDAndWBID(robots.get(robotId).getGoodID(), robotsDestinationID[robotId]);
            robotsDestinationID[robotId] = -1;
            if(isBookSellBuyWB[robotId] == 1){
                isBookSellBuyWB[robotId] = 0;
                setWBX.remove(robotsDestinationID[robotId]);
            }
        }
        //计算卖路线     [robotId, dest, isBook]
        List<int[]> bestRoad = calBestRoadToSell(robotsToSell, robots, workbenches);
        //设置目的地，预定该商品的收购工作台
        for(int[] a : bestRoad){
            robotsDestinationID[a[0]] = a[1];
            addWBXByGoodIDAndWBID(robots.get(a[0]).getGoodID(), a[1]);
            if(a[2] == 1){
                isBookSellBuyWB[a[0]] = 1;
                setWBX.add(a[1]);
            }
        }
    }

    //为机器人们计算最佳买卖路线
    private List<int[]> robotsBestRoad1 = null;
    private double max1 = 0;
    private double tempMax1 = 0;
    public List<int[]> calBestRoadToBuyAndSell(List<Integer> robotsToBuySell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        //[robotId, dest, nextDest, isBook] 返回结果
        robotsBestRoad1 = new ArrayList<>();
        for(Integer robotId : robotsToBuySell){
            robotsBestRoad1.add(new int[]{robotId, -1, -1, 0});
        }
        //临时中间值
        max1 = 0;
        tempMax1 = 0;
        //递归遍历所有顺序，只拿最优解
        recurAllRobotsBuySellRoad(0, robotsToBuySell, robots, workbenches);
        return robotsBestRoad1;
    }

    public void recurAllRobotsBuySellRoad(int n, List<Integer> robotsToBuySell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        //递归终点
        if(n == robotsToBuySell.size()){
            if(tempMax1 > max1){
                max1 = tempMax1;
                for(int[] road : robotsBestRoad1){
                    road[1] = robotsDestinationID[road[0]];
                    road[2] = robotsNextDestinationID[road[0]];
                    road[3] = isBookSellBuyWB[road[0]];
                }
            }
        }
        //未到终点
        for(int i = n; i < robotsToBuySell.size(); i++){
            Tool.changeListValueByAB(robotsToBuySell, n, i);
            //当前机器人编号
            int robotId = robotsToBuySell.get(n);
            //当前机器人
            Robot robot = robots.get(robotId);
            //计算当前机器人的最佳路线  [dest, nextDest, isBook, value]
            double[] road = calBestRoadToBuyAndSell(robot, workbenches);
            //如果有目的地和next目的地，就设置目的地和next目的地，并且预定生产工作台和收购工作台
            if((int) road[0] != -1 && (int) road[1] != -1){
                setDestAndNextDestAndBookWB(robotId, (int) road[0], (int) road[1], (int) road[2], workbenches);
            }
            tempMax1 += road[3];
            //递归
            recurAllRobotsBuySellRoad(n + 1, robotsToBuySell, robots, workbenches);
            //如果有目的地和next目的地，就取消目的地和next目的地，并且取消预定生产工作台和收购工作台
            if((int) road[0] != -1 && (int) road[1] != -1){
                setOffDestAndNextDestAndNoBookWB(robotId, (int) road[0], (int) road[1], (int) road[2], workbenches);
            }
            tempMax1 -= road[3];
            Tool.changeListValueByAB(robotsToBuySell, n, i);
        }
    }

    //计算最佳买卖路线
    public double[] calBestRoadToBuyAndSell(Robot robot, ArrayList<Workbench> workbenches){
        //返回[dest, nextDest, isBook, value]
        //帧数
        int frame = Main.tl.get();
        int remainFrame = 12000 - frame;

        int destinationID = -1;
        int nextDestinationID = -1;
        int isBook = 0;
        double maxValuePerDistance = 0;
        for(int i = 0; i < workbenches.size(); i++){
            //买-工作台
            Workbench wb = workbenches.get(i);
            //有产品 && 没被预定   也就是能买
            if(wb.getProduct_status() == 1 && !setWBX.contains(i)){
                //需要该商品的工作台-需要该商品的被预定的工作台=需要该商品的没被预定的工作台
                List<Integer> list = Tool.calDifferenceWithABList(getWBListByGoodID(wb.getID()), getWBXListByGoodID(wb.getID()));
                for(Integer l : list){
                    Workbench wb1 = workbenches.get(l);
                    //工作台是否有格子放这个商品 也就是能卖
                    if(!Tool.changeRawToList(wb1.getRaw()).contains(wb.getID()) && wb1.isAlive()){
                        //求距离
                        int[] robotMatXY = robot.getMatXY();
                        double robotToBuyDistance = wb.getDistMatWithNoGood()[robotMatXY[0]][robotMatXY[1]];
                        if(robotToBuyDistance == Double.MAX_VALUE)  continue;
                        double buyToSellDistance = wb1.getDistMatWithGood()[wb.getxMap()][wb.getyMap()];
                        if(buyToSellDistance == Double.MAX_VALUE)  continue;
                        double robotToSellDistance = robotToBuyDistance + buyToSellDistance;
                        //判断剩余时间是否能完成买卖
                        if(!isEnoughTimeToBuySell(robotToSellDistance, 6, remainFrame, 0))    continue;
                        robotToSellDistance += 50;
                        //求价值
                        double goodValue = valueArray[wb.getID()];
                        double futureValue = valueArray[wb1.getID()];
                        int n = Tool.changeRawToList(wb1.getRaw()).size();
                        int nn = wb1.getNeeds().size() - n;
                        futureValue /= nn;
                        if(wb1.getID() == 7)    futureValue *= 2;
                        if(wb1.getProduct_status() == 1 || wb1.getRemain_frame() > -1){
                            if(wb1.getID() != 7)    futureValue /= 6;
                            else    futureValue /= 2;
                        }
                        if(wb.getID() == 7) futureValue += valueArray[7];
                        double totalValue = goodValue + futureValue;
                        //如果7缺一个原材料就提高对应的权值
                        if(wb.getID() == 1 || wb.getID() == 2 || wb.getID() == 3){
                            List<Integer> list1 = Tool.calDifferenceWithABList(getWBListByGoodID(wb1.getID()), getWBXListByGoodID(wb1.getID()));
                            for(Integer l1 : list1){
                                Workbench wb2 = workbenches.get(l1);
                                //获取机器人手上的物品+工作台的原材料
                                HashSet<Integer> robotsGoods = new HashSet<>();
                                for(Robot r : Main.robots){
                                    if(r.getGoodID() == 4 || r.getGoodID() == 5 || r.getGoodID() == 6)  robotsGoods.add(r.getGoodID());
                                }
                                for(Workbench w : workbenches){
                                    if(w.getID() == 4 || w.getID() == 5 || w.getID() == 6){
                                        if(w.getProduct_status() == 1)  robotsGoods.add(w.getID());
                                    }
                                }
                                robotsGoods.addAll(Tool.changeRawToList(wb2.getRaw()));
                                if(!robotsGoods.contains(wb1.getID()) && wb2.getNeeds().size() - robotsGoods.size() < 3){
                                    totalValue += (valueArray[wb2.getID()] / nn / (wb2.getNeeds().size() - robotsGoods.size()));
                                    break;
                                }
                            }
                        }
                        //出售赚的钱 / 总路径长度     取最大的
                        double valuePerDistance = totalValue / robotToSellDistance;
                        if(valuePerDistance > maxValuePerDistance){
                            maxValuePerDistance = valuePerDistance;
                            destinationID = i;
                            nextDestinationID = l;
                            if(wb1.getProduct_status() == 1){
                                isBook = 1;
                                for(Robot r : Main.robots){
                                    if(r == robot)  continue;
                                    if(r.getGoodID() == 0 && wb1.getDistMatWithNoGood()[r.getMatXY()[0]][r.getMatXY()[1]] < (robotToSellDistance - 50)) isBook = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(destinationID == -1){
            for(int i = 0; i < workbenches.size(); i++){
                //买-工作台
                Workbench wb = workbenches.get(i);
                //有产品 && 没被预定   也就是能买
                if(wb.getProduct_status() == 1 && (!setWBX.contains(i) || skip.contains(wb.getID()))){
                    //需要该商品的工作台-需要该商品的被预定的工作台=需要该商品的没被预定的工作台
                    List<Integer> list = Tool.calDifferenceWithABList(getWBListByGoodID(wb.getID()), getWBXListByGoodID(wb.getID()));
                    for(Integer l : list){
                        Workbench wb1 = workbenches.get(l);
                        //工作台是否有格子放这个商品 也就是能卖
                        if(!Tool.changeRawToList(wb1.getRaw()).contains(wb.getID()) && wb1.isAlive()){
                            //求距离
                            int[] robotMatXY = robot.getMatXY();
                            double robotToBuyDistance = wb.getDistMatWithNoGood()[robotMatXY[0]][robotMatXY[1]];
                            if(robotToBuyDistance == Double.MAX_VALUE)  continue;
                            double buyToSellDistance = wb1.getDistMatWithGood()[wb.getxMap()][wb.getyMap()];
                            if(buyToSellDistance == Double.MAX_VALUE)  continue;
                            double robotToSellDistance = robotToBuyDistance + buyToSellDistance;
                            //判断剩余时间是否能完成买卖
                            if(!isEnoughTimeToBuySell(robotToSellDistance, 6, remainFrame, 0))    continue;
                            robotToSellDistance += 50;
                            //求价值
                            double goodValue = valueArray[wb.getID()];
                            double futureValue = valueArray[wb1.getID()];
                            int n = Tool.changeRawToList(wb1.getRaw()).size();
                            int nn = wb1.getNeeds().size() - n;
                            futureValue /= nn;
                            if(wb1.getID() == 7)    futureValue *= 2;
                            if(wb1.getProduct_status() == 1 || wb1.getRemain_frame() > -1){
                                if(wb1.getID() != 7)    futureValue /= 6;
                                else    futureValue /= 2;
                            }
                            if(wb.getID() == 7) futureValue += valueArray[7];
                            double totalValue = goodValue + futureValue;
                            //如果7缺一个原材料就提高对应的权值
                            if(wb.getID() == 1 || wb.getID() == 2 || wb.getID() == 3){
                                List<Integer> list1 = Tool.calDifferenceWithABList(getWBListByGoodID(wb1.getID()), getWBXListByGoodID(wb1.getID()));
                                for(Integer l1 : list1){
                                    Workbench wb2 = workbenches.get(l1);
                                    //获取机器人手上的物品+工作台的原材料
                                    HashSet<Integer> robotsGoods = new HashSet<>();
                                    for(Robot r : Main.robots){
                                        if(r.getGoodID() == 4 || r.getGoodID() == 5 || r.getGoodID() == 6)  robotsGoods.add(r.getGoodID());
                                    }
                                    for(Workbench w : workbenches){
                                        if(w.getID() == 4 || w.getID() == 5 || w.getID() == 6){
                                            if(w.getProduct_status() == 1)  robotsGoods.add(w.getID());
                                        }
                                    }
                                    robotsGoods.addAll(Tool.changeRawToList(wb2.getRaw()));
                                    if(!robotsGoods.contains(wb1.getID()) && wb2.getNeeds().size() - robotsGoods.size() < 3){
                                        totalValue += (valueArray[wb2.getID()] / nn / (wb2.getNeeds().size() - robotsGoods.size()));
                                        break;
                                    }
                                }
                            }
                            //出售赚的钱 / 总路径长度     取最大的
                            double valuePerDistance = totalValue / robotToSellDistance;
                            if(valuePerDistance > maxValuePerDistance){
                                maxValuePerDistance = valuePerDistance;
                                destinationID = i;
                                nextDestinationID = l;
                                if(wb1.getProduct_status() == 1){
                                    isBook = 1;
                                    for(Robot r : Main.robots){
                                        if(r == robot)  continue;
                                        if(r.getGoodID() == 0 && wb1.getDistMatWithNoGood()[r.getMatXY()[0]][r.getMatXY()[1]] < (robotToSellDistance - 50)) isBook = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new double[]{destinationID, nextDestinationID, isBook, maxValuePerDistance};
    }

    //为机器人们计算最佳卖路线
    private List<int[]> robotsBestRoad2 = null;
    private double max2 = 0;
    private double tempMax2 = 0;
    public List<int[]> calBestRoadToSell(List<Integer> robotsToSell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        //[robotId, dest, isBook]   返回结果
        robotsBestRoad2 = new ArrayList<>();
        for(Integer robotId : robotsToSell){
            robotsBestRoad2.add(new int[]{robotId, -1, 0});
        }
        //临时中间值
        max2 = 0;
        tempMax2 = 0;
        //递归遍历所有顺序
        recurAllRobotsSellRoad(0, robotsToSell, robots, workbenches);
        return robotsBestRoad2;
    }

    public void recurAllRobotsSellRoad(int n, List<Integer> robotsToSell, ArrayList<Robot> robots, ArrayList<Workbench> workbenches){
        //递归终点
        if(n == robotsToSell.size()){
            if(tempMax2 > max2){
                max2 = tempMax2;
                for(int[] road : robotsBestRoad2){
                    road[1] = robotsDestinationID[road[0]];
                    road[2] = isBookSellBuyWB[road[0]];
                }
            }
        }
        //未到终点
        for(int i = n; i < robotsToSell.size(); i++){
            Tool.changeListValueByAB(robotsToSell, n, i);
            //当前机器人编号
            int robotId = robotsToSell.get(n);
            //当前机器人
            Robot robot = robots.get(robotId);
            //计算当前机器人的最佳卖路线 [dest, isBook, value]
            double[] road = calBestRoadToSell(robot, workbenches);
            //设置目的地，并且预定收购工作台
            robotsDestinationID[robotId] = (int) road[0];
            addWBXByGoodIDAndWBID(robot.getGoodID(), (int) road[0]);
            if((int) road[1] == 1){
                isBookSellBuyWB[robotId] = 1;
                setWBX.add((int) road[0]);
            }
            tempMax2 += road[2];
            //递归
            recurAllRobotsSellRoad(n + 1, robotsToSell, robots, workbenches);
            //取消目的地，并且取消预定收购工作台
            robotsDestinationID[robotId] = -1;
            deleteWBXByGoodIDAndWBID(robot.getGoodID(), (int) road[0]);
            if((int) road[1] == 1){
                isBookSellBuyWB[robotId] = 0;
                setWBX.remove((int) road[0]);
            }
            tempMax2 -= road[2];
            Tool.changeListValueByAB(robotsToSell, n, i);
        }
    }

    //计算最佳卖路线
    public double[] calBestRoadToSell(Robot robot, ArrayList<Workbench> workbenches){
        //返回 [dest, isBook, value]
        //帧数
        int frame = Main.tl.get();
        int remainFrame = 12000 - frame;

        int destination = -1;
        int isBook = 0;
        double maxValuePerDistance = 0;
        int goodID = robot.getGoodID();
        List<Integer> list = Tool.calDifferenceWithABList(mapWB.get(goodID), mapWBX.get(goodID));
        for(Integer l : list){
            Workbench wb = workbenches.get(l);
            if(!Tool.changeRawToList(wb.getRaw()).contains(goodID) && wb.isAlive()){
                //求距离
                int[] robotMatXY = robot.getMatXY();
                double distance = wb.getDistMatWithGood()[robotMatXY[0]][robotMatXY[1]];
                if(distance == Double.MAX_VALUE)    continue;
                //判断剩余时间是否能完成卖
                if(!isEnoughTimeToBuySell(distance, 6, remainFrame, 1))    continue;
                distance += 50;
                //求价值
                double goodValue = valueArray[goodID];
                double futureValue = valueArray[wb.getID()];
                int n = Tool.changeRawToList(wb.getRaw()).size();
                int nn = wb.getNeeds().size() - n;
                futureValue /= nn;
                if(wb.getID() == 7)    futureValue *= 2;
                if(wb.getProduct_status() == 1 || wb.getRemain_frame() > -1){
                    if(wb.getID() != 7) futureValue /= 6;
                    else futureValue /= 2;
                }
                if(goodID == 7) futureValue += valueArray[7];
                double totalValue = goodValue + futureValue;
                //如果7缺一个原材料就提高对应的权值
                if(goodID == 1 || goodID == 2 || goodID == 3){
                    List<Integer> list1 = Tool.calDifferenceWithABList(getWBListByGoodID(wb.getID()), getWBXListByGoodID(wb.getID()));
                    for(Integer l1 : list1){
                        Workbench wb2 = workbenches.get(l1);
                        //获取机器人手上的物品+工作台的原材料
                        HashSet<Integer> robotsGoods = new HashSet<>();
                        for(Robot r : Main.robots){
                            if(r.getGoodID() == 4 || r.getGoodID() == 5 || r.getGoodID() == 6)  robotsGoods.add(r.getGoodID());
                        }
                        for(Workbench w : workbenches){
                            if(w.getID() == 4 || w.getID() == 5 || w.getID() == 6){
                                if(w.getProduct_status() == 1)  robotsGoods.add(w.getID());
                            }
                        }
                        robotsGoods.addAll(Tool.changeRawToList(wb2.getRaw()));
                        if(!robotsGoods.contains(wb.getID()) && wb2.getNeeds().size() - robotsGoods.size() < 3){
                            totalValue += (valueArray[wb2.getID()] / nn / (wb2.getNeeds().size() - robotsGoods.size()));
                            break;
                        }
                    }
                }
                //出售赚的钱 / 总路径长度     取最大的
                double valuePerDistance = totalValue / distance;
                if(valuePerDistance > maxValuePerDistance){
                    maxValuePerDistance = valuePerDistance;
                    destination = l;
                    if(wb.getProduct_status() == 1){
                        isBook = 1;
                        for(Robot r : Main.robots){
                            if(r == robot)  continue;
                            if(r.getGoodID() == 0 && wb.getDistMatWithNoGood()[r.getMatXY()[0]][r.getMatXY()[1]] < (distance - 50)) isBook = 0;
                        }
                    }
                }
            }
        }
        return new double[]{destination, isBook, maxValuePerDistance};
    }

    //设置目的地和next目的地，并且预定生产工作台和收购工作台
    public void setDestAndNextDestAndBookWB(int robotID, int destinationID, int nextDestinationID, int isBook, ArrayList<Workbench> workbenches){
        robotsDestinationID[robotID] = destinationID;
        robotsNextDestinationID[robotID] = nextDestinationID;
        //预定该商品的出售坑位
        setWBX.add(destinationID);
        //预定该商品的收购坑位
        int goodID = workbenches.get(destinationID).getID();
        addWBXByGoodIDAndWBID(goodID, nextDestinationID);
        //如果收购坑位的工作台也要预定买了，那就预定
        if(isBook == 1){
            setWBX.add(nextDestinationID);
            isBookSellBuyWB[robotID] = 1;
        }
    }

    //取消目的地和next目的地，并且取消预定生产工作台和收购工作台
    public void setOffDestAndNextDestAndNoBookWB(int robotID, int destinationID, int nextDestinationID, int isBook, ArrayList<Workbench> workbenches){
        robotsDestinationID[robotID] = -1;
        robotsNextDestinationID[robotID] = -1;
        //取消预定该商品的出售坑位
        setWBX.remove(destinationID);
        //取消预定该商品的收购坑位
        int goodID = workbenches.get(destinationID).getID();
        List<Integer> bookWBXList = mapWBX.get(goodID);
        bookWBXList.remove(Integer.valueOf(nextDestinationID));
        mapWBX.put(goodID, bookWBXList);
        //如果收购坑位的工作台也被预定买了，那就取消预定
        if(isBook == 1){
            isBookSellBuyWB[robotID] = 0;
            setWBX.remove(nextDestinationID);
        }
    }

    //根据距离计算时间，与剩余时间比较，是否足够去买卖
    public boolean isEnoughTimeToBuySell(double distance, double speed, int remainFrame, int buyOrSell){
        if("RED".equals(Main.team))    speed = 7;
        double multiple = buyOrSell == 0 ? 1.2 : 1.1;
        double time = distance / speed;
        time *= multiple;
        return time * 50 < remainFrame;
    }

}
