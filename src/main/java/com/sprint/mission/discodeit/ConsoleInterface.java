package com.sprint.mission.discodeit;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleInterface {
    private final Scanner sc;
    private ServiceType flag;

//    private final HashMap<> consoleTable;
//    public ConsoleInterface(Scanner sc, ServiceType flag) {
//        this.sc = sc;
//        this.flag = flag;
//    }
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



    public <T> void PrintOut(ArrayList<T> in){
        for (T i : in){
            System.out.println(i);
        }
    }

}
