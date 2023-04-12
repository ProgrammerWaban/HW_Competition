package com.huawei.codecraft;

import java.util.ArrayList;

class MyConstant {

    public static double M_PI = Math.PI;
    public static double MAX_ACCELERATE = 0.2;						//动态窗口：最大加速度

    public static double SAMPLING_VELOCITY = 0.01;				    //速度采样间隔
    public static double SAMPLING_OMEGA = 1 / 180.0 * M_PI;			//角速度采样间隔
    public static double DT = 1/(20*50);						    //采样时间间隔 20次/帧
    public static double PREDICT_TIME = DT*30;					     //预测时间 30帧  即0.6秒
    public static double WEIGHT_HEADING =0.05;						//HEADING权重
    public static double WEIGHT_CLEARANCE =0.2;					//CLEARANCE权重
    public static double WEIGHT_VELOCITY = 0.1;						//VELOCITY权重
    public static double ROBOT_RADIUS = 0.53;					//机器人半径

}

class RobotState{
    public double x, y, orientation, velocity, rotate;
    RobotState(double x_,double y_,double orientation_, double velocity_,double rotate_){
        x = x_;
        y = y_;
        orientation = orientation_;
        velocity = velocity_;
        rotate = rotate_;
    }

}

class EvaluationPara {
    public double heading, clearance, velocity, v, w;// velocity表示向量的速度,v 表示速度大小
}

public class DWA {
    public static ArrayList<Double> CreateDW(RobotState curState, double max_a, double max_ar, double max_v, double min_v, double max_rotate, double min_rotate) {
        ArrayList<Double> dw = new ArrayList<Double>();
        double tmpMinVelocity = curState.velocity - max_a * MyConstant.DT;
        double tmpMaxVelocity = curState.velocity + max_a * MyConstant.DT;
        double tmpMinRotate = curState.rotate - max_ar * MyConstant.DT;
        double tmpMaxRotate = curState.rotate + max_ar * MyConstant.DT;

        dw.add(tmpMinVelocity > min_v ? tmpMinVelocity : min_v);
        dw.add(tmpMaxVelocity < max_v ? tmpMaxVelocity : max_v);
        dw.add(tmpMinRotate > min_rotate ? tmpMinRotate : min_rotate);
        dw.add(tmpMaxRotate < max_rotate ? tmpMaxRotate : max_rotate);
        return dw;
    }

    public static RobotState Motion(RobotState curState, double v, double rotate) {


        double x = curState.x + v * MyConstant.DT * Math.cos(curState.orientation);
        double y = curState.y + v * MyConstant.DT * Math.sin(curState.orientation);

        double orientation = curState.orientation + rotate * MyConstant.DT;
        RobotState afterMoveState = new RobotState(x, y, orientation, v, rotate);

        return afterMoveState;
    }

    public static ArrayList<RobotState> GenerateTraj(RobotState initState, double vel, double ome) {
        RobotState tempState = initState;
        ArrayList<RobotState> trajectories = new ArrayList<RobotState>();
        double time = 0;
        trajectories.add(initState);
        while (time < MyConstant.PREDICT_TIME) {
            tempState = Motion(tempState, vel, ome);
            trajectories.add(tempState);
            time += MyConstant.DT;
        }

        return trajectories;
    }

    public static double CalcHeading(RobotState rState, double[] goal) {
        double heading;

        double dy = goal[1] - rState.y;
        double dx = goal[0] - rState.x;

        double goalTheta = Math.atan2(dy, dx);
        double targetTheta;
        if (goalTheta > rState.orientation) {
            targetTheta = goalTheta - rState.orientation;
        } else {
            targetTheta = rState.orientation - goalTheta;
        }

        heading = 180 - targetTheta / MyConstant.M_PI * 180;

        return heading;
    }

    public static double CalcClearance(RobotState rState, ArrayList<double[]> obs) {
        double dist = 100;
        double distTemp;
        for (int i = 0; i < obs.size(); i++) {

            double dx = rState.x - obs.get(i)[0];
            double dy = rState.y - obs.get(i)[1];
            distTemp = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) - MyConstant.ROBOT_RADIUS;

            if (dist > distTemp) {
                dist = distTemp;
            }
        }

        if (dist >= 2 * MyConstant.ROBOT_RADIUS) {
            dist = 2 * MyConstant.ROBOT_RADIUS;
        }

        return dist;
    }

    public static double CalcBreakingDist(double v) {
        double stopDist = 0;
        while (v > 0) {
            stopDist = stopDist + v * MyConstant.DT;
            v = v - MyConstant.MAX_ACCELERATE * MyConstant.DT;
        }

        return stopDist;
    }

    public static double[] forecast(Robot robot, double inst_v, double inst_vr, double max_a, double max_ar) {
        int _each_frame = 20; // 每一帧的份数
        int frame_each_second = 50; // 每一秒的帧数
        int frame_count = 30; // 预测多少帧
        double dt = 1.0 / (_each_frame * frame_each_second); // 积分的时间间隔，单位秒
        double x = robot.getX(), y = robot.getY();
        double vx = robot.getLineSpend_x(), vy = robot.getLineSpend_y();
        double vr = robot.getRotate(), th = robot.getIntendAngle(); // vr 旋转速度, th朝向
        double a, ar, delta_x, delta_y, delta_th; // a 线加速度 ar 角加速度
        double v = Math.sqrt(vx * vx + vy * vy);
        int i = 0;
        double[] x_and_y_xv_and_vy = new double[2];
        while (true) {
            a = v > inst_v ? -max_a : max_a; // 当前线速度大于预期速度就加速，否则就减速
            ar = vr > inst_vr ? -max_ar : max_ar; // 当前角速度大于预期速度就加速，否则就减速
            delta_x = vx * dt; // 横向移动位移
            delta_y = vy * dt; // 纵向移动位移
            delta_th = vr * dt; // 转动角度
            x = x + delta_x;    // 更新横向位置
            y = y + delta_y;    // 更新纵向位置
            vx = v*Math.cos(th) + a * Math.cos(th) * dt; // 更新横向线速度
            vy = v*Math.sin(th) + a * Math.sin(th) * dt; // 更新纵向线速度
            th = th + delta_th; // 更新转向
            vr = vr + ar * dt;        // 更新转动速度
            v = Math.sqrt(vx * vx + vy * vy); // 更新速度
            if (i == _each_frame * frame_count - 1) {
                x_and_y_xv_and_vy[0] = x;
                x_and_y_xv_and_vy[1] = y;
                break;
            }
            i++;
        }
        return x_and_y_xv_and_vy;
    }

    public static double[] forecast(Robot robot, double max_a, double max_ar, Workbench wb) {
        //new一个新的Robot,防止原来的机器人被修改
        Robot newRobot = new Robot(robot.getWorkStationID(), robot.getGoodID(), robot.getTimeCoef(), robot.getCollideCoef(), robot.getRotate(), robot.getLineSpend_x(), robot.getLineSpend_y(), robot.getIntendAngle(), robot.getX(), robot.getY());
        newRobot.setWantRotate(robot.getWantRotate());
        newRobot.setWantForward(robot.getWantForward());

        int _each_frame = 10; // 每一帧的份数
        int frame_each_second = 50; // 每一秒的帧数
        int frame_count = 30; // 预测多少帧
        double dt = 1.0 / (_each_frame * frame_each_second); // 积分的时间间隔，单位秒
        double a, ar, delta_x, delta_y, delta_th; // a 线加速度 ar 角加速度
        double v = Math.sqrt(newRobot.getLineSpend_x() * newRobot.getLineSpend_x() + newRobot.getLineSpend_y() * newRobot.getLineSpend_y());
        int i = 0;
        double[] x_and_y_xv_and_vy = new double[2];
        while (true) {
            a = v > newRobot.getWantForward() ? -max_a : max_a; // 当前线速度大于预期速度就加速，否则就减速
            ar = newRobot.getRotate() > newRobot.getWantRotate() ? -max_ar : max_ar; // 当前角速度大于预期速度就加速，否则就减速
            delta_x = newRobot.getLineSpend_x() * dt; // 横向移动位移
            delta_y = newRobot.getLineSpend_y() * dt; // 纵向移动位移
            delta_th = newRobot.getRotate() * dt; // 转动角度
            newRobot.setX(newRobot.getX() + delta_x);    // 更新横向位置
            newRobot.setY(newRobot.getY() + delta_y);    // 更新纵向位置
            newRobot.setLineSpend_x(v*Math.cos(newRobot.getIntendAngle()) + a * Math.cos(newRobot.getIntendAngle()) * dt); // 更新横向线速度
            newRobot.setLineSpend_y(v*Math.sin(newRobot.getIntendAngle()) + a * Math.sin(newRobot.getIntendAngle()) * dt); // 更新纵向线速度
            newRobot.setIntendAngle(newRobot.getIntendAngle() + delta_th); // 更新转向
            newRobot.setRotate(newRobot.getRotate() + ar * dt);        // 更新转动速度
            v = Math.sqrt(newRobot.getLineSpend_x() * newRobot.getLineSpend_x()+ newRobot.getLineSpend_y() * newRobot.getLineSpend_y()); // 更新速度
            if (i == _each_frame * frame_count - 1) {
                x_and_y_xv_and_vy[0] = newRobot.getX();
                x_and_y_xv_and_vy[1] = newRobot.getY();
                break;
            }
            i++;
            BetterMove.adjustMovement(wb, newRobot);
        }
        return x_and_y_xv_and_vy;
    }



    public static double[] DynamicWindowApproach(Robot robot, ArrayList<double[]> obs, double[] target) {
        // 0:minVelocity, 1:maxVelocity, 2:minRotate, 3:maxRotate
        double x, y, orientation, v, rotate, xv, yv;
        x = robot.getX();
        y = robot.getY();
        xv = robot.getLineSpend_x();
        yv = robot.getLineSpend_y();
        orientation = robot.getIntendAngle();
        rotate = robot.getRotate();
        v = Math.sqrt(xv * xv + yv * yv);
        RobotState rState = new RobotState(x, y, orientation, v, rotate);
        double max_a, max_ar, max_v = 6, min_v = 0, max_rotate = Math.PI, min_rotate = 0;
        if (robot.getGoodID() != 0) {
            max_a = 14.164683241367037; // 负载最大线加速度
            max_ar = 27.83863163047348; // 负载最大线角速度，这个数据不确定，还没测
        } else {
            max_a = 19.338183;                        //弧形轨迹：无负载最大角速度 0.38676367 m/帧
            max_ar = 38.61665;                       //动态窗口：无负载最大角加速度 0.772333 弧度/帧

        }
        ArrayList<Double> velocityAndRotateRange = CreateDW(rState, max_a, max_ar, max_v, min_v, max_rotate, min_rotate);
        ArrayList<EvaluationPara> evalParas = new ArrayList<EvaluationPara>();
        double sumHeading = 0;
        double sumClearance = 0;
        double sumVelocity = 0;
        double tmp_v, tmp_max_v, tmp_min_v, tmp_w, tmp_max_w, tmp_min_w;
        tmp_min_v = velocityAndRotateRange.get(0);
        tmp_max_v = velocityAndRotateRange.get(1);
        tmp_min_w = velocityAndRotateRange.get(2);
        tmp_max_w = velocityAndRotateRange.get(3);
        for (tmp_v = tmp_min_v; tmp_v < tmp_max_v; tmp_v += MyConstant.SAMPLING_VELOCITY) {
            for (tmp_w = tmp_min_w; tmp_w < tmp_max_w; tmp_w += MyConstant.SAMPLING_OMEGA) {
                ArrayList<RobotState> trajectories = GenerateTraj(rState, v, tmp_w);

                //评价参数
                EvaluationPara tempEvalPara = new EvaluationPara();
                int last = trajectories.size() - 1;
                double tempClearance = CalcClearance(trajectories.get(last), obs);
                double stopDist = CalcBreakingDist(tmp_v);
                if (tempClearance > stopDist) {
                    tempEvalPara.heading = CalcHeading(trajectories.get(last), target);
                    tempEvalPara.clearance = tempClearance;
                    tempEvalPara.velocity = Math.abs(tmp_v);
                    tempEvalPara.v = tmp_v;
                    tempEvalPara.w = tmp_w;

                    sumHeading = sumHeading + tempEvalPara.heading;
                    sumClearance = sumHeading + tempEvalPara.clearance;
                    sumVelocity = sumVelocity + tempEvalPara.velocity;

                    evalParas.add(tempEvalPara);
                }
            }
        }
        double selectedVelocity = 0;
        double selectedRotate = 0;
        double G = 0;
        for (int k = 0; k < evalParas.size(); k++) {
            EvaluationPara i = evalParas.get(k);
            double smoothHeading = i.heading / sumHeading;
            double smoothClearance = i.clearance / sumClearance;
            double smoothVelocity = i.velocity / sumVelocity;

            double tempG = MyConstant.WEIGHT_HEADING * smoothHeading + MyConstant.WEIGHT_CLEARANCE * smoothClearance + MyConstant.WEIGHT_VELOCITY * smoothVelocity;

            if (tempG > G) {
                G = tempG;
                selectedVelocity = i.v;
                selectedRotate = i.w;
            }
        }

        double[] selVelocity = new double[2];
        selVelocity[0] = selectedVelocity;
        selVelocity[1] = selectedRotate;
        return selVelocity;
    }

    public static ArrayList<double[]> getObs(ArrayList<Robot> robots, int cur_robot_id) {
        ArrayList<double[]> obstacle = new ArrayList<double[]>();
        int obstacleID[][] = new int[][]{{1,2,3},{2,3},{3},{}};
        int i = 0;
        while( i < obstacleID[cur_robot_id].length){
            Robot robot = robots.get(obstacleID[cur_robot_id][i]);
            double max_a, max_ar;
            if (robot.getGoodID() != 0) {
                max_a = 14.164683241367037; // 负载最大线加速度
                max_ar = 27.83863163047348; // 负载最大线角速度，这个数据不确定，还没测
            } else {
                max_a = 19.338183;                        //弧形轨迹：无负载最大角速度 0.38676367 m/帧
                max_ar = 38.61665;                       //动态窗口：无负载最大角加速度 0.772333 弧度/帧

            }
            double x_and_y[] = forecast(robot, robot.getWantForward(), robot.getWantRotate(), max_a, max_ar);
            obstacle.add(x_and_y);
            i++;
        }
        return obstacle;
    }
    /*
    public static ArrayList<double[]> getObstacle(ArrayList<Robot> robots, int cur_robot_id) {
        ArrayList<double[]> obstacle = new ArrayList<double[]>();
        for (int i = 0; i < robots.size(); i++) {
            if (i != cur_robot_id) {
                Robot robot = robots.get(i);
                double max_a, max_ar;
                if (robot.getGoodID() != 0) {
                    max_a = 14.164683241367037; // 负载最大线加速度
                    max_ar = 27.83863163047348; // 负载最大线角速度，这个数据不确定，还没测
                } else {
                    max_a = 19.338183;                        //弧形轨迹：无负载最大角速度 0.38676367 m/帧
                    max_ar = 38.61665;                       //动态窗口：无负载最大角加速度 0.772333 弧度/帧

                }
                double x_and_y[] = forecast(robot, robot.getWantForward(), robot.getWantRotate(), max_a, max_ar);
                obstacle.add(x_and_y);
            }
        }
        return obstacle;
    }
    */
    public static void avoidCarsh(Robot robot, double target_x, double target_y, ArrayList<double[]> obstacle) {
        double[] goal = new double[]{target_x, target_y};
        double[] selectedVelocity;
        selectedVelocity = DynamicWindowApproach(robot, obstacle, goal);
        robot.setWantRotate(selectedVelocity[0]);
        robot.setWantForward(selectedVelocity[1]);

    }

    public static void avoid_Collide1(ArrayList<Robot> robots,dispatchingCenter dc, ArrayList<Workbench> workbenches) {
        for (int i = 0; i < 4; i++) {
            Robot robot = robots.get(i);
            ArrayList<double[]> obs = getObs(robots, i);
            int desId = dc.findDestinationIDByRobotID(i);
            if(desId!=-1) {
                double target_x = workbenches.get(desId).getX();
                double target_y = workbenches.get(desId).getY();
                // 根据障碍物，目标横纵坐标，直接调整线路
                avoidCarsh(robot, target_x, target_y, obs);
            }
        }
    }
}