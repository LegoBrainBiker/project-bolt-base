package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.stream.Collectors;


public class ConfigurableOpMode extends OpMode {
    @SuppressLint("DiscouragedApi")
    @OpModeRegistrar
    public static void register(OpModeManager manager) {
        Context context = AppUtil.getDefContext(); // how is this not a memory leak?
        Resources resources = context.getResources();
        ConfigurableOpMode.setRobotConfig((String) resources.getText(resources.getIdentifier("robot_config","raw",context.getPackageName())));
        int i = 0;
        while (true) {
            try {
                String OpModeDescription = (String) resources.getText(resources.getIdentifier("opMode"+i,"raw",context.getPackageName()));
                OpModeMeta.Builder opModeDetails = new OpModeMeta.Builder();
                if (OpModeDescription.startsWith("auto\n")) {
                    opModeDetails.flavor = OpModeMeta.Flavor.AUTONOMOUS;
                    OpModeDescription = OpModeDescription.substring(5);
                }
                else { // use teleOp\n
                    opModeDetails.flavor = OpModeMeta.Flavor.TELEOP;
                    OpModeDescription = OpModeDescription.substring(7);
                }
                int x = OpModeDescription.indexOf("\n");
                opModeDetails.name = OpModeDescription.substring(0,x);
                OpModeDescription = OpModeDescription.substring(x+1);
                x = OpModeDescription.indexOf("\n");
                opModeDetails.group = OpModeDescription.substring(0,x);
                OpModeDescription = OpModeDescription.substring(x+1);
                x = OpModeDescription.indexOf("\n");
                opModeDetails.autoTransition = OpModeDescription.substring(0,x);
                OpModeDescription = OpModeDescription.substring(x+1);
                opModeDetails.source = OpModeMeta.Source.EXTERNAL_LIBRARY;
                manager.register(opModeDetails.build(),new ConfigurableOpMode(OpModeDescription));
            } catch (Resources.NotFoundException e) {break;}
            i++;
        }
        // use "./gradlew installDebug" to build the apk
    }
    List<DcMotor> motors;
    private static final ArrayList<String> motorNames = new ArrayList<>();
    List<DcMotor> driveMotors;
    private static final ArrayList<String> driveMotorNames = new ArrayList<>();
    Dictionary<String, value> variables;
    static double driveTicksPerCm;
    private static void setRobotConfig(String robotConfiguration) {
        int x = robotConfiguration.indexOf("\n");
        driveTicksPerCm = Double.parseDouble(robotConfiguration.substring(0,x));
        robotConfiguration = robotConfiguration.substring(x);
        for (int i = 0; i<4; i++) {
            x = robotConfiguration.indexOf("\n");
            driveMotorNames.add(robotConfiguration.substring(0,x));
            robotConfiguration = robotConfiguration.substring(x);
        }
        x = robotConfiguration.indexOf("\n");
        String y = robotConfiguration.substring(0,x);
        robotConfiguration = robotConfiguration.substring(x);
        while (!y.equals("\n")) {
            motorNames.add(y.stripTrailing());
            x = robotConfiguration.indexOf("\n");
            y = robotConfiguration.substring(0,x);
            robotConfiguration = robotConfiguration.substring(x);
        }
    }
    private final ArrayList<NetworkNode> nodes;
    private ConfigurableOpMode(String nodesConfiguration) {
        super();
        ArrayList<NetworkNode> nodes = new ArrayList<>();
        ArrayList<Integer> connectionQueue = new ArrayList<>(), connectionSlot = new ArrayList<>(), connectionOutSlot = new ArrayList<>(), connectionOut = new ArrayList<>();
        ArrayList<Integer> pulseConnectionQueue = new ArrayList<>(), pulseConnectionSlot = new ArrayList<>(), pulseConnectionOut = new ArrayList<>();
        while (!nodesConfiguration.isEmpty()) {
            // example - start:2,3,;\n\n would make a start node that has a pulse connection to 2 and 3
            // other example - someNodeType:4,5,6,;7,8,9,;\n5/1,6/2,;4/3,6/3,;\n would be a node with pulse connections from its first pulse output to 4 5 and 6, from its second pulse output to 7 8 and 9, data connections from it's first input slot to the first input of 5 and the second of 6, and from its second data output slot to  the third input slot of 4 the third input slot of 6
            if (nodesConfiguration.startsWith("start:")) {
                nodes.add(new startNode());
                nodesConfiguration = nodesConfiguration.substring(6);
            }
            else if (nodesConfiguration.startsWith("pulse-switch:")) {
                nodes.add(new pulseSwitch());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("value-switch:")) {
                nodes.add(new valueSwitch());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("constant:")) {
                nodesConfiguration = nodesConfiguration.substring(9);
                if (nodesConfiguration.startsWith("true-")) {
                    nodes.add(new constantNode(new bool(true)));
                    nodesConfiguration = nodesConfiguration.substring(5);
                } else if (nodesConfiguration.startsWith("false-")) {
                    nodesConfiguration = nodesConfiguration.substring(6);
                    nodes.add(new constantNode(new bool(false)));
                }
                else if(nodesConfiguration.startsWith("\"")) {
                    nodesConfiguration = nodesConfiguration.substring(1);
                    int x = nodesConfiguration.indexOf("\"");
                    nodes.add(new constantNode(new text(nodesConfiguration.substring(0,x))));
                    nodesConfiguration = nodesConfiguration.substring(x+2);
                }else {
                    int x = nodesConfiguration.indexOf("-");
                    nodes.add(new constantNode(new num(Double.parseDouble(nodesConfiguration.substring(0,x)))));
                    nodesConfiguration = nodesConfiguration.substring(x+1);
                }
            }
            else if (nodesConfiguration.startsWith("sum:")) {
                nodes.add(new sumNode());
                nodesConfiguration = nodesConfiguration.substring(9);
            }
            else if (nodesConfiguration.startsWith("product:")) {
                nodes.add(new productNode());
                nodesConfiguration = nodesConfiguration.substring(6);
            }
            else if (nodesConfiguration.startsWith("subtract:")) {
                nodes.add(new subtractNode());
                nodesConfiguration = nodesConfiguration.substring(6);
            }
            else if (nodesConfiguration.startsWith("divide:")) {
                nodes.add(new divideNode());
                nodesConfiguration = nodesConfiguration.substring(6);
            }
            else if (nodesConfiguration.startsWith("power:")) {
                nodes.add(new powerNode());
                nodesConfiguration = nodesConfiguration.substring(6);
            }
            else if (nodesConfiguration.startsWith("less-than:")) {
                nodes.add(new lessThanNode());
                nodesConfiguration = nodesConfiguration.substring(10);
            }
            else if (nodesConfiguration.startsWith("greater-than:")) {
                nodes.add(new greaterThanNode());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("equals:")) {
                nodes.add(new equalsNode());
                nodesConfiguration = nodesConfiguration.substring(7);
            }
            else if (nodesConfiguration.startsWith("not:")) {
                nodes.add(new notNode());
                nodesConfiguration = nodesConfiguration.substring(4);
            }
            else if (nodesConfiguration.startsWith("and:")) {
                nodes.add(new andNode());
                nodesConfiguration = nodesConfiguration.substring(4);
            }
            else if (nodesConfiguration.startsWith("xor:")) {
                nodes.add(new xorNode());
                nodesConfiguration = nodesConfiguration.substring(4);
            }
            else if (nodesConfiguration.startsWith("or:")) {
                nodes.add(new orNode());
                nodesConfiguration = nodesConfiguration.substring(3);
            }
            else if (nodesConfiguration.startsWith("log:")) {
                nodes.add(new logNode());
                nodesConfiguration = nodesConfiguration.substring(4);
            }
            else if (nodesConfiguration.startsWith("join:")) {
                nodes.add(new joinNode());
                nodesConfiguration = nodesConfiguration.substring(5);
            }
            else if (nodesConfiguration.startsWith("wait until:")) {
                nodes.add(new waitUntilNode());
                nodesConfiguration = nodesConfiguration.substring(11);
            }
            else if (nodesConfiguration.startsWith("wait time:")) {
                nodes.add(new waitTimeNode());
                nodesConfiguration = nodesConfiguration.substring(10);
            }
            else if (nodesConfiguration.startsWith("current position:")) {
                nodes.add(new currentPositionNode());
                nodesConfiguration = nodesConfiguration.substring(17);
            }
            else if (nodesConfiguration.startsWith("go to strafe:")) {
                nodes.add(new goToStrafingNode());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("run time:")) {
                nodes.add(new runtimeNode());
                nodesConfiguration = nodesConfiguration.substring(9);
            }
            else if (nodesConfiguration.startsWith("set variable:")) {
                nodes.add(new setVariableNode());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("get variable:")) {
                nodes.add(new getVariableNode());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("set power:")) {
                nodes.add(new setSpinnablePower());
                nodesConfiguration = nodesConfiguration.substring(10);
            }
            else if (nodesConfiguration.startsWith("set rotation:")) {
                nodes.add(new setSpinnableRotation());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("get rotation:")) {
                nodes.add(new getSpinnableRotation());
                nodesConfiguration = nodesConfiguration.substring(13);
            }
            else if (nodesConfiguration.startsWith("loop:")) {
                nodes.add(new loopNode());
                nodesConfiguration = nodesConfiguration.substring(5);
            }
            else {
                nodesConfiguration = nodesConfiguration.substring(1);
                continue;
            }
            int i = 0;
            while (!nodesConfiguration.startsWith("\n")) {
                while (!nodesConfiguration.startsWith(";")) {
                    int x = nodesConfiguration.indexOf(",");
                    pulseConnectionQueue.add(nodes.size());
                    pulseConnectionSlot.add(i);
                    pulseConnectionOut.add(Integer.parseInt(nodesConfiguration.substring(0,x)));
                    nodesConfiguration = nodesConfiguration.substring(x);
                }
                nodesConfiguration = nodesConfiguration.substring(1);
                i++;
            }
            i=0;
            nodesConfiguration = nodesConfiguration.substring(1);
            while (!nodesConfiguration.startsWith("\n")) {
                while (!nodesConfiguration.startsWith(";")) {
                    int x = nodesConfiguration.indexOf("/");
                    connectionQueue.add(nodes.size());
                    connectionSlot.add(i);
                    connectionOut.add(Integer.parseInt(nodesConfiguration.substring(0,x)));
                    nodesConfiguration = nodesConfiguration.substring(x);
                    x = nodesConfiguration.indexOf(",");
                    connectionOutSlot.add(Integer.parseInt(nodesConfiguration.substring(0,x)));
                    nodesConfiguration = nodesConfiguration.substring(x);
                }
                nodesConfiguration = nodesConfiguration.substring(1);
                i++;
            }
            nodesConfiguration = nodesConfiguration.substring(1);
        }
        int lastConnectionSlot = -1;
        for (int i = 0; i<pulseConnectionQueue.size(); i++) {
            if (pulseConnectionSlot.get(i) != lastConnectionSlot) {
                lastConnectionSlot++;
                nodes.get(pulseConnectionQueue.get(i)).addPulseConnectionSlot();
            }

            nodes.get(pulseConnectionQueue.get(i)).addPulseConnection(new pulseConnection(nodes.get(pulseConnectionOut.get(i))));
        } // adds pulseConnections from pulseConnectionQueue
        int j = 0;
        while (!connectionQueue.isEmpty()) {
            int i = 0;

            while ( i<connectionQueue.size()) {
                if (connectionOutSlot.get(i) == j) {
                    nodes.get(connectionOut.get(i)).addConnection(new connection(nodes.get(connectionQueue.get(i)),connectionSlot.get(i)));
                    connectionQueue.remove(i);
                }
                else
                    i++;
            }
            j++;
        }
        this.nodes = nodes;
        nodes.forEach(n->n.opMode = this);
    }
    public void init() {
        motors = motorNames.stream().map(n->hardwareMap.dcMotor.get(n)).collect(Collectors.toList());
        driveMotors = driveMotorNames.stream().map(n->hardwareMap.dcMotor.get(n)).collect(Collectors.toList());
        // code here
        nodes.stream().filter(n->n instanceof startNode).forEach(n->n.sendPulse(0));
        nodes.forEach(NetworkNode::init);
        posX = 0;
        posY = 0;
    }
    double posX;
    double posY;
    double positionalAccuracy;
    public void loop() {
        telemetry.update();
        nodes.forEach(NetworkNode::onLoop);
    }
    void goToStrafing(double x, double y) {
        // to be implemented
    }
}
abstract class NetworkNode {
    ConfigurableOpMode opMode;
    ArrayList<connection> inputs;
    ArrayList<ArrayList<pulseConnection>> outPulses;
    value getOutput(int Pos) {return null;}
    void receivePulse() {}
    void sendPulse(int output) {
        for (pulseConnection outPulse : outPulses.get(output))
            outPulse.sendPulse();
    }
    void onLoop() {}
    void init() {}
    void addConnection(connection c) {
        inputs.add(c);
    }
    void addPulseConnection(pulseConnection c) {
        outPulses.get(outPulses.size()-1).add(c);
    }
    void addPulseConnectionSlot() {
        outPulses.add(new ArrayList<>());
    }
}
class connection {
    private final NetworkNode inputNode;
    private final int inputSlotNumber;
    value getData() {
        return inputNode.getOutput(inputSlotNumber);
    }
    public connection(NetworkNode inNode, int slotNum) {
        inputNode = inNode;
        inputSlotNumber = slotNum;
    }
}
class pulseConnection {
    private final NetworkNode outputNode;
    void sendPulse() {
        outputNode.receivePulse();
    }
    public pulseConnection(NetworkNode outNode) {outputNode = outNode;}
}
class startNode extends NetworkNode { } // this has code in init() in the opMode
class pulseSwitch extends NetworkNode {
    @Override
    void receivePulse() {
        value v = inputs.get(0).getData();
        for (int i = 1; i < inputs.size(); i ++) {
            if (v.equals(inputs.get(i).getData())) {
                sendPulse(i-1);
            }
        }
        sendPulse(inputs.size()-1); // base case
    }
} // sends a pulse to the chanel of the fist input with the same value as the fist input
class valueSwitch extends NetworkNode {
    @Override
    value getOutput(int Pos) {
        value v = inputs.get(0).getData();
        for (int i = 1; i < inputs.size()-2; i += 2) {
            if (v.equals(inputs.get(i).getData())) {
                return inputs.get(i + 1).getData();
            }
        }
        return inputs.get(inputs.size() - 1).getData(); // base case
    }
} // gets a value from the input after the fist odd input that equals the fist input. if none of those equal the first input, gets the last input
class constantNode extends NetworkNode {
    private final value constantValue;

    constantNode(value constantValue) {
        this.constantValue = constantValue;
    }
    @Override
    value getOutput(int Pos) {
        return constantValue;
    }
}
class sumNode extends NetworkNode {
    public num getOutput(int slot) {
        double i = 0;
        for (double n : inputs.stream().map(connection::getData).filter(n->n instanceof num).map(n->((num) n).num).collect(Collectors.toSet()))
            i +=n;
        return new num(i);
    }
}
class productNode extends NetworkNode {
    public num getOutput(int pos) {
        double i = 1;
        for (double n : inputs.stream().map(connection::getData).filter(n->n instanceof num).map(n->((num) n).num).collect(Collectors.toSet()))
            i *=n;
        return new num(i);
    }
}
class subtractNode extends NetworkNode {
    public num getOutput(int pos) {
        num x = (num) inputs.get(0).getData();
        num y = (num) inputs.get(1).getData();
        return new num(x.num-y.num);
    }
}
class divideNode extends NetworkNode {
    public num getOutput(int pos) {
        num x = (num) inputs.get(0).getData();
        num y = (num) inputs.get(1).getData();
        return new num(x.num/y.num);
    }
}
class powerNode extends NetworkNode {
    public num getOutput(int pos) {
        num x = (num) inputs.get(0).getData();
        num y = (num) inputs.get(1).getData();
        return new num(Math.pow(x.num,y.num));
    }
}
class lessThanNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        value y = inputs.get(1).getData();
        return new bool(x instanceof num && y instanceof num && ((num) x).num<((num) y).num);
    }
}
class equalsNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        return new bool(inputs.get(0).getData().equals(inputs.get(1).getData()));
    }
}
class greaterThanNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        value y = inputs.get(1).getData();
        return new bool(x instanceof num && y instanceof num && ((num) x).num>((num) y).num);
    }
}
class notNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        return new bool(x instanceof bool && !((bool) x).bool);
    }
}
class andNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        value y = inputs.get(1).getData();
        return new bool(x instanceof bool && y instanceof bool && ((bool) x).bool&&((bool) y).bool);
    }
}
class orNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        value y = inputs.get(1).getData();
        return new bool(x instanceof bool && y instanceof bool && ((bool) x).bool||((bool) y).bool);
    }
}
class xorNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        value x = inputs.get(0).getData();
        value y = inputs.get(1).getData();
        return new bool(x instanceof bool && y instanceof bool && ((bool) x).bool!=((bool) y).bool);
    }
}
class logNode extends NetworkNode {
    @Override public void onLoop() {
        opMode.telemetry.addData(inputs.get(0).getData().toString(),inputs.get(1).getData().toString());
    }
}
class joinNode extends NetworkNode {
    public value getOutput(int pos) {
        StringBuilder i = new StringBuilder();
        for (String n : inputs.stream().map(x->x.getData().toString()).collect(Collectors.toSet()))
            i.append(n);

        return new text(i.toString());
    }
}
class waitUntilNode extends NetworkNode {
    boolean isActive;
    @Override
    void init() {
        isActive = false;
    }
    @Override
    void receivePulse() {
        isActive = true;
    }
    @Override
    void onLoop() {
        if (isActive&&((bool)inputs.get(0).getData()).bool) {
            isActive = false;
            sendPulse(0);
        }
    }
}
class waitTimeNode extends NetworkNode {
    double outTime;
    @Override
    void init() {
        outTime = -1;
    }
    @Override
    public void receivePulse() {
        value x = inputs.get(0).getData();
        outTime = opMode.time + ((num)x).num;
    }
    void onLoop() {
        if (outTime > 0 && opMode.time > outTime) {
            outTime -= -1;
            sendPulse(0);
        }
    }
}
class currentPositionNode extends NetworkNode {
    value getOutput(int pos) {
        if (pos == 0) {
            return new num(opMode.posX);
        } else {
            return new num(opMode.posY);
        }
    }
}
class goToStrafingNode extends NetworkNode {
    @Override
    void receivePulse() {
        opMode.goToStrafing(((num) inputs.get(0).getData()).num,((num) inputs.get(1).getData()).num);
    }
}
class runtimeNode extends NetworkNode {
    @Override
    value getOutput(int pos) {
        return new num(opMode.time);
    }
}
class setVariableNode extends NetworkNode {
    @Override
    void receivePulse() {
        opMode.variables.put(inputs.get(0).getData().toString(), inputs.get(1).getData());
    }
}
class loopNode extends NetworkNode {
    @Override
    void onLoop() {
        sendPulse(0);
    }
}
class getVariableNode extends NetworkNode {
    @Override
    public value getOutput(int pos) {
        return opMode.variables.get(inputs.get(0).getData());
    }
}
class setSpinnablePower extends NetworkNode {
    @Override
    void receivePulse() {
        opMode.motors.get((int)((num)inputs.get(0).getData()).num).setPower(((num)inputs.get(0).getData()).num);
    }
}
class setSpinnableRotation extends NetworkNode {
    @Override
    void receivePulse() {
        opMode.motors.get((int)((num)inputs.get(0).getData()).num).setTargetPosition((int)((num)inputs.get(0).getData()).num);
    }

}
class getSpinnableRotation extends NetworkNode {
    @Override
    value getOutput(int pos) {
        return new num(opMode.motors.get((int)((num)inputs.get(0).getData()).num).getCurrentPosition());
    }

}
abstract class value {
    boolean equals(value other) {
        return other.toString().equals(toString());
    }
}
class num extends value {
    public final double num;
    public num(double number) {
        num = number;
    }
    @NonNull
    public String toString() {
        return ""+num;
    }
}
class bool extends value {
    public final boolean bool;
    public bool(boolean Boolean) {
        bool = Boolean;
    }
    @NonNull
    public String toString() {
        return ""+bool;
    }
}
class text extends value {
    public final String text;
    public text(String t) {
        text = t;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
