package com.sprint.mission.discodeit;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;


import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;


public class JavaApplication {
    static Boolean flag = true;
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ConsoleInterface console = new ConsoleInterface(in);

        UUID curUserID;
        UUID curChannelID;
        try {
            while (flag) {
                console.initMsg();
                switch (console.getFlag()) {
                    case MAIN:
                        mainService(console);
                        break;
                    case CHANNEL:
                        channelService(console);
                        break;
                    case MESSAGE:
                        messageService(console);
                        break;
                    case USER:
                        userService(console);
                        break;
                    default:
                        flag = false;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    static void mainService(ConsoleInterface console) {
        String input = console.inputMsg();
        switch (input) {
            case "1":
                console.setFlag(ServiceType.CHANNEL);
                System.out.println(" channel service selected");
                break;
            case "2":
                console.setFlag(ServiceType.MESSAGE);
                System.out.println(" message service selected");
                break;
            case "3":
                console.setFlag(ServiceType.USER);
                System.out.println("user service selected");
                break;
            case "exit":
                flag = false;
                break;
            default:
                System.out.println("invalid input");
        }
    }

    static void channelService(ConsoleInterface console){
        final String[] newName = new String[1];
        final String[] newDec = new String[1];
        final String[] newType = new String[1];
        ChannelType type;
        UUID id;
        ArrayList<Channel> channel;
        JCFChannelService chn = JCFChannelService.getInstance();

        class channelFunction {
            void getChannelInfo(){
                System.out.print("type channel name : ");
                newName[0] = console.inputMsg();
                System.out.println("type channel description : ");
                newDec[0] = console.inputMsg();
                while(true){
                    System.out.print("set channel type is public? (y/n) : ");
                    newType[0] = console.inputMsg();
                    if (newType[0].equals("y") ||  newType[0].equals("n")) break;
                    System.out.println("invalid input. type only 'y' or 'n'");
                }
            }
        }

        String input = console.inputMsg();
        channelFunction innerClass = new channelFunction();

        switch (input) {
            case "1": // create
                System.out.println(" === Create Channel Start === ");
                innerClass.getChannelInfo();
                type = newType[0].equals("y") ? ChannelType.PUBLIC : ChannelType.PRIVATE;
                chn.createChannel(newName[0], newDec[0], type);
                System.out.printf("%s channel created successfully\n", newName[0]);
                break;
            case "2": // get list
                System.out.println(" === List of channels === ");
                ArrayList<Channel> channels = chn.readChannelAll();
                console.PrintOut(channels);
                console.inputMsg();
                break;
            case "3": // get obj by id
                System.out.println(" === Get channel === ");
                id = UUID.fromString(console.inputMsg());
                channel = chn.readChannel(id);
                console.PrintOut(channel);
                console.inputMsg();
                break;
            case "4": // update
                System.out.println(" === Update channel === ");
                // select target
                System.out.print("type target id");
                id = UUID.fromString(console.inputMsg());
                channel = chn.readChannel(id);
                console.PrintOut(channel);
                console.inputMsg();
                // mod info
                innerClass.getChannelInfo();
                type = newType[0].equals("y") ? ChannelType.PUBLIC : ChannelType.PRIVATE;
                chn.updateChannel(id, newName[0], newDec[0], type);
                break;
            case "5": // remove
                System.out.println(" === Delete channel === ");
                System.out.print("type target id : ");
                id = UUID.fromString(console.inputMsg());
                System.out.print("\n");
                chn.deleteChannel(id);
                System.out.println("channel deleted successfully");
                break;
            case "exit": // return to menu
                console.setFlag(ServiceType.MAIN);
                break;
            default:
                System.out.println("invalid input");
        }
    }

    static void messageService(ConsoleInterface console) {
        final UUID[] newUser = new UUID[1];
        final UUID[] newChannel = new UUID[1];
        final String[] newData = new String[1];
        UUID id;
        ArrayList<Message> message;

        JCFMessageService msg = JCFMessageService.getInstance();


        class msgFunction {
            void getMessageInfo() {
                System.out.print("which user has this msg? : ");
                newUser[0] = UUID.fromString(console.inputMsg());
                System.out.println("which channel has this msg? : ");
                newChannel[0] = UUID.fromString(console.inputMsg());
                System.out.println("type user new Message");
                newData[0] = console.inputMsg();
            }

            boolean check(UUID userID, UUID channelID) {
                JCFChannelService chn = JCFChannelService.getInstance();
                JCFUserService user = JCFUserService.getInstance();
                try {
                    user.readUser(newUser[0]);
                    chn.readChannel(newChannel[0]);
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            }
        }

        String input = console.inputMsg();
        msgFunction innerClass = new msgFunction();

        switch (input) {
            case "1": // create
                System.out.println(" === Create message Start === ");
                innerClass.getMessageInfo();
                if(!innerClass.check(newUser[0], newChannel[0])){
                    System.out.println("invalid creation msg.");
                    break;
                };
                msg.createMessage(newUser[0], newChannel[0], newData[0]);
                System.out.printf("%s message created successfully\n", newUser[0]);
                break;

            case "2": // get list
                System.out.println(" === List of messages === ");
                ArrayList<Message> messages = msg.readMessageAll();
                console.PrintOut(messages);
                console.inputMsg();
                break;

            case "3": // get obj by id
                System.out.println(" === Get message === ");
                id = UUID.fromString(console.inputMsg());
                message = msg.readMessage(id);
                console.PrintOut(message);
                console.inputMsg();
                break;

            case "4": // update
                System.out.println(" === Update message === ");
                // select target
                System.out.print("type target id");
                id = UUID.fromString(console.inputMsg());
                message = msg.readMessage(id);
                console.PrintOut(message);
                //console.inputMsg();
                System.out.println("change info type in");
                // mod info
                innerClass.getMessageInfo();
                msg.updateMessage(id, newData[0]);
                break;

            case "5": // remove
                System.out.println(" === Delete channel === ");
                System.out.print("type target id : ");
                id = UUID.fromString(console.inputMsg());
                System.out.print("\n");
                msg.deleteMessage(id);
                System.out.println("channel deleted successfully");
                break;

            case "exit": // return to menu
                console.setFlag(ServiceType.MAIN);
                break;

            default:
                System.out.println("invalid input");
        }
    }

    static void userService(
            ConsoleInterface console
    ) {
        final String[] newName = new String[1];
        final String[] newId = new String[1];
        final String[] newPassword = new String[1];
        JCFUserService usr = JCFUserService.getInstance();
        UUID id;
        ArrayList<User> user;


        class userFunction {
            void getUserInfo() {
                System.out.print("New name? : ");
                newName[0] = console.inputMsg();
                System.out.println("New ID? : ");
                newId[0] = console.inputMsg();
                System.out.println("New Password? :");
                newPassword[0] = console.inputMsg();
            }
        }

        String input = console.inputMsg();
        userFunction innerClass = new userFunction();

        switch (input) {
            case "1": // create
                System.out.println(" === Create user Start === ");
                innerClass.getUserInfo();
                usr.createUser(newName[0], newId[0], newPassword[0]);
                System.out.printf("%s user created successfully\n", newName[0]);
                break;
            case "2": // get list
                System.out.println(" === List of Users === ");
                ArrayList<User> users = usr.readUserAll();
                console.PrintOut(users);
                console.inputMsg();
                break;
            case "3": // get obj by id
                System.out.println(" === Get user === ");
                id = UUID.fromString(console.inputMsg());
                user = usr.readUser(id);
                console.PrintOut(user);
                console.inputMsg();
                break;
            case "4": // update
                System.out.println(" === Update user === ");
                // select target
                System.out.print("type target id");
                id = UUID.fromString(console.inputMsg());
                user = usr.readUser(id);
                console.PrintOut(user);
                console.inputMsg();
                // mod info
                innerClass.getUserInfo();
                usr.updateUser(id, newName[0],newId[0],newPassword[0]);
                break;
            case "5": // remove
                System.out.println(" === Delete user === ");
                System.out.print("type target id : ");
                id = UUID.fromString(console.inputMsg());
                System.out.print("\n");
                usr.deleteUser(id);
                System.out.println("user deleted successfully");
                break;
            case "exit": // return to menu
                console.setFlag(ServiceType.MAIN);
                break;
            default:
                System.out.println("invalid input");
        }
    }

}


