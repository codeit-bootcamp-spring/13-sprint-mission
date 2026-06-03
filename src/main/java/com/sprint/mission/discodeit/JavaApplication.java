package com.sprint.mission.discodeit;


import com.sprint.mission.discodeit.entity.ChannelType;

import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.UUID;


public class JavaApplication {
    static Boolean flag = true;
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ConsoleInterface console = new ConsoleInterface(in);

        // File * Repository test line
//        FileUserRepository user = FileUserRepository.open(Path.of("data/user.ser"));
//        FileChannelRepository channel = FileChannelRepository.open(Path.of("data/channel.ser"));
//        FileMessageRepository message = FileMessageRepository.open(Path.of("data/message.ser"));

        // JCF * Repository test line
        JCFUserRepository user = JCFUserRepository.open();
        JCFChannelRepository channel = JCFChannelRepository.open();
        JCFMessageRepository message = JCFMessageRepository.open();

        BasicChannelService chn = new BasicChannelService(channel);
        BasicUserService usr = new BasicUserService(user);
        BasicMessageService msg = new BasicMessageService(channel, user, message);

        try {
            while (flag) {
                console.initMsg();
                switch (console.getFlag()) {
                    case MAIN:
                        mainService(console);
                        break;
                    case CHANNEL:
                        channelService(console,chn);
                        break;
                    case MESSAGE:
                        messageService(console,msg);
                        break;
                    case USER:
                        userService(console,usr);
                        break;
                    default:
                        flag = false;
                }
            }
            user.close();
            channel.close();
            message.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
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

    static void channelService(ConsoleInterface console, ChannelService chn) {
        final String[] newName = new String[1];
        final String[] newDec = new String[1];
        final String[] newType = new String[1];
        ChannelType type;
        UUID id;


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


                console.PrintOut(chn.getChannelList());
                System.out.println("====================");
                break;
            case "3": // get obj by id
                System.out.println(" === Get channel === ");
                System.out.println("type the channel id");

                id = UUID.fromString(console.inputMsg());

                console.PrintOut(chn.getChannelById(id));
                System.out.println("====================");
                break;
            case "4": // update
                System.out.println(" === Update channel === ");
                // select target

                System.out.print("type target id");
                id = UUID.fromString(console.inputMsg());
                console.PrintOut(chn.getChannelById(id));

                innerClass.getChannelInfo();
                type = newType[0].equals("y") ? ChannelType.PUBLIC : ChannelType.PRIVATE;
                chn.updateChannelInfo(id, newName[0], newDec[0], type);
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

    static void messageService(ConsoleInterface console, MessageService msg) {
        final UUID[] newUser = new UUID[1];
        final UUID[] newChannel = new UUID[1];
        final String[] newData = new String[1];
        UUID id;


        class msgFunction {
            void getMessageInfo() {
                System.out.print("which user has this msg? : ");
                newUser[0] = UUID.fromString(console.inputMsg());
                System.out.println("which channel has this msg? : ");
                newChannel[0] = UUID.fromString(console.inputMsg());
                System.out.println("type user new Message");
                newData[0] = console.inputMsg();
            }
        }

        String input = console.inputMsg();
        msgFunction innerClass = new msgFunction();


        switch (input) {
            case "1": // create
                System.out.println(" === Create message Start === ");

                innerClass.getMessageInfo();

                msg.createMessage(newUser[0], newChannel[0], newData[0]);
                break;

            case "2": // get list
                System.out.println(" === List of messages === ");
                console.PrintOut(msg.getMessageList());
                break;
            case "3": // get obj by id
                System.out.println(" === Get message === ");
                id = UUID.fromString(console.inputMsg());
                console.PrintOut(msg.getMessageById(id));
                break;

            case "4": // update
                System.out.println(" === Update message === ");
                System.out.println("type target id");
                id = UUID.fromString(console.inputMsg());
                console.PrintOut(msg.getMessageById(id));
                //console.inputMsg();
                System.out.println("change info type in");
                // mod info
                innerClass.getMessageInfo();
                msg.updateMessageData(id, newData[0]);
                break;

            case "5": // remove
                System.out.println(" === Delete channel === ");
                System.out.println("type target id : ");
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

    static void userService(ConsoleInterface console, UserService usr ) {
        final String[] newName = new String[1];
        final String[] newId = new String[1];
        final String[] newPassword = new String[1];
        UUID id;


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
//                ArrayList<User> users = sr.readUserAll();
                console.PrintOut(usr.getUserList());
                console.inputMsg();
                break;
            case "3": // get obj by id
                System.out.println(" === Get user === ");
                id = UUID.fromString(console.inputMsg());
                console.PrintOut(usr.getUserById(id));
                console.inputMsg();
                break;
            case "4": // update
                System.out.println(" === Update user === ");
                // select target
                System.out.print("type target id");
                id = UUID.fromString(console.inputMsg());
                console.PrintOut(usr.getUserById(id));
                console.inputMsg();
                // mod info
                innerClass.getUserInfo();
                usr.updateUserInfo(id, newName[0],newId[0],newPassword[0]);
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


