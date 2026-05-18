package com.sprint.mission.discodeit;



import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleInterface {
    private final Scanner sc;
    private ServiceType flag;

//    private final HashMap<> consoleTable;
    public ConsoleInterface(Scanner sc, ServiceType flag) {
        this.sc = sc;
        this.flag = flag;
    }
    public ConsoleInterface(Scanner sc) {
        this.sc = sc;
        this.flag = ServiceType.MAIN;
    }

    public void setFlag(ServiceType flag) {
        this.flag = flag;
    }

    public ServiceType getFlag() {
        return this.flag;
    }

    public void initMsg(){
        System.out.println(flag.getMsg());
    }

    public String inputMsg(){
        return this.sc.nextLine();
    }



    public void channelPrintOut(ArrayList<Channel> in){
        for (Channel i : in){
            System.out.println(i);
        }
    }
    public void messagePrintOut(ArrayList<Message> in){
        for (Message i : in){
            System.out.println(i);
        }
    }
    public void userPrintOut(ArrayList<User> in){
        for (User i : in){
            System.out.println(i);
        }
    }

}
