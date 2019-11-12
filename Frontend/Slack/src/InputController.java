import Controllers.DBSupport;
import Models.Message;
import Models.Channel;
import Models.User;
import Models.Workspace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;


/**
 * This will be the main controller for the application.
 * It will take the initial input for User Input
 * and then pass it along to other classes to handle the actual functionality
 *
 * @Author Dylan Mrzlak
 * Original Framework and Use handle for CREATE_WORKSPACE and JOIN_WORKSPACE
 */
public class InputController {
    private static final String CREATE_WORKSPACE = "create workspace";
    private static final String JOIN_WORKSPACE = "join";
    private static final String CREATE_CHANNEL = "create channel";
    private static final String VIEW_USERS = "view users";
    private static final String SEND = "send";
    private static final String SEND_DM = "send to";
    private static final String ADD_USER = "create user";
    private static final String PIN_MESSAGE = "pin message";
    private static final String LOG_MESSAGES = "log messages";
    private static final String VIEW_MENTIONS = "view mentions";
    private static final String LOGIN = "login";
    private static final String HELP = "help";
    private static final String SEARCH_WORKSPACE = "search workspace";
    private static final String SEARCH_USER = "search user";
    private static final String SEND_TEXTFILE = "send textfile";
    private static final String DOWNLOAD_TEXTFILE = "download textfile";


    private static Gson gson = new Gson();
    private static User curUser = null;
    private static Workspace curWorkspace = null;
    private static Channel curChannel = null;

    public static void main(String[] args) {
        //If this line get mad, check your dependencies, may have dropped
        Scanner input = new Scanner(System.in);
        String userInput = "";

        printInstructions();
        do {
            userInput = input.nextLine();
            //By forcing commands to be in a format of COMMAND - ARGUMENT
            //We can easily manage the input and decide what is needed
            int substringBegin = userInput.indexOf('-');
            //Now that we have commands without args, we need to be able to take commands without the delimitter set
            if (substringBegin == -1) substringBegin = userInput.length();
            String command = "";
            String[] userArgs = {};
            if (userInput.length() == substringBegin) {
                command = userInput;
            } else {
                command = userInput.substring(0, substringBegin).trim();
                userArgs = userInput.substring(substringBegin + 1).trim().split(" ");
            }
            //Have updated the switch to be more readable and move into methods, rather than holding all the logic in here.
            //We were simply expanding this too much that it was becoming hard to read. This is much more followable
            switch (command) {
                case HELP:
                    printHelp();
                    break;
                case LOGIN:
                    SignIn(userArgs);
                    break;
                case ADD_USER:
                    AddUser(userArgs);
                    break;
                case CREATE_WORKSPACE:
                    CreateWorkspace(userArgs);
                    break;
                case JOIN_WORKSPACE:
                    JoinWorkspace(userArgs);
                    break;
                case CREATE_CHANNEL:
                    CreateChannel(userArgs);
                    break;
                case VIEW_USERS:
                    ViewUsers(userArgs);
                    break;
                case PIN_MESSAGE:
                    PinMessage(userArgs);
                    break;
                case SEND_DM:
                    SendDM(userArgs);
                    break;
                case SEND:
                    SendMessage(userArgs);
                    break;
                case LOG_MESSAGES:
                    LogMessage(userArgs);
                    break;
                case VIEW_MENTIONS:
                    ViewMentions(userArgs);
                    break;
                case SEARCH_USER:
                    searchUser(userArgs);
                    break;
                case SEARCH_WORKSPACE:
                    searchWorkspace(userArgs);
                    break;
                case DOWNLOAD_TEXTFILE:
                    downloadTextfile(userArgs);
                    break;
                case SEND_TEXTFILE:
                    sendTextfile(usedArgs);
                    break;
                default:
                    System.out.println("Invalid Input please try again :(");
                    break;
            }
        } while (input.hasNextLine());

    }


    private static void sendTextfile(){
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (curChannel == null) {
            System.out.println("User not in Channel;");
            return;
        }
        String filename = userArgs[0];
        filename = filename.subString(lastIndexOf('/',filname.length));

        String Content = "";
        Scanner scan = Scanner(new File(userArgs[0]));
        String temp;
        while (scan.hasNext()){
            temp = scan.nextLine();
            temp.replace(' ',"_SS_");
            temp.replace('\t',"_TT_");
            temp+="_NN_";
            temp.replace('&', "_AA_");
            temp.replace('?',"_QQ_");
            //temp.replace('',"")
            content += temp;
        }
        if(content.length > 2048) {
            content = content.subString(0, 2048);
            System.out.print("File too long shortened to send");
        }

        DBSupport.HTTPResponse Textfile.sendText(filename,content);

        if (response.code >= 300) {
            System.out.println(response.response);
        } else {
            System.out.print("file sent!");
        }

    }


    /**
     * Takes "no" arguments and will print the mentions for the current user in a channel.
     * Our mentions right now just search for a username, but can simply be expanded to be "@/USERNAME/"
     * @param userArgs
     * @author Dylan Mrzlak
     */
    private static void ViewMentions(String[] userArgs) {
        //View mentions does not need user args whatsoever, so we'll just ignore them. They are passed in for consistency
        //First we want to make sure that nothing is null (We want to be in a workspace and a channel, and then the user needs to be signed in
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (curChannel == null) {
            System.out.println("User not in Channel;");
            return;
        }
        //Now that those are out of the way, we need to get the actual mentions
        // depending on what the server returns, we handle it accordingly. We either get a list and print it,
        //  or an error and print that
        DBSupport.HTTPResponse response = Channel.viewMentions(curUser.getName(), curWorkspace.getName(), curChannel.getName());
        if (response.code >= 300) {
            System.out.println(response.response);
        } else {
            Message[] mentions = gson.fromJson(response.response, Message[].class);
            System.out.println("These are the your mentions:");
            for (Message mention : mentions) {
                String printMention = "\t" + mention.getContent().replaceAll("_SS_", " ");
                System.out.println(printMention);
            }
        }

    }

    /**
     * Take the messages from a workspace, grouped by channel and order based on time. The write it to a file
     * @param userArgs
     * @author Dylan Mrzlak
     */
    private static void LogMessage(String[] userArgs) {
        //Logging does not need user args whatsoever, so we'll just ignore them. They are passed in for consistency
        //First we want to make sure that nothing is null (We want to be in a workspace and a channel, and then the user needs to be signed in
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (curChannel == null) {
            System.out.println("User not in Channel;");
            return;
        }
        //Now that those are out of the way, we need to get the actual messages.
        // They will be grouped by channel in the backend,
        // but we do some lifting here as well to properly make the strings we need
        //Note like with all of our methods, we can get an error or the data we want so we have to deal with it properly
        System.out.println("Getting the messages for: " + curWorkspace.getName());
        DBSupport.HTTPResponse response = Message.getAllMessages(curWorkspace.getName());
        if (response.code >= 300) {
            System.out.println(response.response);
        }
        else {
            System.out.println("Retrieval for: " + curWorkspace.getName() + " successful");
            Message[] messages = gson.fromJson(response.response, Message[].class);
            String workspaceName = curWorkspace.getName();
            //There is a real possibility that this could take a long time (and it's , so I'm going to run it asynchronously maybe later)
            //For the log file, as of now, we'll put it into the out folder under a folder logs
            //and with the name:
            //      "LOG_<WORKSPACENAME>_<DATE>
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
            Date date = new Date();
            String filePath = "\\LOG_" + workspaceName + "_" + dateFormat.format(date);
            System.out.println("Formatting");
            //We want to format the data as we want, and then take the new list and write the file with it
            String[] linesToWrite = LogMessagesFormat(messages);
            System.out.println("Writing");
            //write said file
            WriteFile(linesToWrite, filePath);
        }
    }

    /**
     * searches for user
     * args name of user
     */
    private static void searchWorkspace(String[] userArgs){
            if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        String Wname;
        if(userArgs.length == 0){
            Wname = "-1";
        }else{
            Wname = userArgs[0];
        }
        System.out.println("Searching for workspace...");
        DBSupport.HTTPResponse response = Workspace.searchWorkspace(Wname);
        if (response.code >= 300) {
            System.out.println(response.response);
        }
        else {
            System.out.println("Workspaces like: " + Wname);
            Workspace[] workspacesFound = gson.fromJson(response.response, workspacesFound[].class);
            for(int i = 0; i < workspacesFound.length;i++) {
                System.out.println(workspacesFound[i].getName());
            }
        }
    }
    private static void searchUser(String[] userArgs){
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        String Uname;
        if(userArgs.length == 0){
            Uname = "-1";
        }else{
            Uname = userArgs[0];
        }
        System.out.println("Searching for User...");
        DBSupport.HTTPResponse response = User.searchUser(Uname);
        if (response.code >= 300) {
            System.out.println(response.response);
        }
        else {
            System.out.println("User like: " + Wname);
            User[] userFound = gson.fromJson(response.response, userFound[].class);
            for(int i = 0; i < userFound.length;i++) {
                System.out.println(userFound[i].getName());
            }
        }
    }

    /**
     * Sign the user in (if the arguments are correct) and set them to the current user
     * @param userArgs
     * @author Logan Garrett
     */
    private static void SignIn(String[] userArgs) {
        if (userArgs.length != 2) {
            System.out.println("Invalid Number or Arguments");
            return;
        }
        //Either the user put in the right username and password, or they did not.
        //If they did not, tell them with the error
        //If they did, then the user is signed in and set the user to the user returned from the server
        DBSupport.HTTPResponse uResponse = User.signIn(userArgs[0], userArgs[1]);
        if (uResponse.code > 300) {
            System.out.println(uResponse.response);
        } else {
            System.out.println("Login Successful");
            User u = gson.fromJson(uResponse.response, User.class);
            curUser = u;
        }
    }

    /**
     * Create user in the server, then set the user to that
     * @param userArgs
     */
    private static void AddUser(String[] userArgs) {
        if (userArgs.length != 2) {
            System.out.println("Invalid Number or Arguments");
            return;
        }
        DBSupport.HTTPResponse uResponse = User.createUser(userArgs[0], userArgs[1]);
        if (uResponse.code > 300) {
            System.out.println(uResponse.response);
        } else {
            System.out.println("Saved User");
            User u = gson.fromJson(uResponse.response, User.class);
            curUser = u;
        }
    }

    /**
     * Create a new workspace (if possible) and then set the user's current workspace to that
     * @param userArgs
     * @author dylan mrzlak
     */
    private static void CreateWorkspace(String[] userArgs) {
        if (userArgs.length != 1) {
            System.out.println("Invalid Number or Arguments");
            return;
        }
        System.out.println("Creating Workspace...");
        DBSupport.HTTPResponse wResponse = Workspace.createWorkspace(userArgs[0]);
        if (wResponse.code > 300) {
            System.out.println(wResponse.response);
        } else {
            System.out.println("Saved Workspace");
            Workspace w = gson.fromJson(wResponse.response, Workspace.class);
            curWorkspace = w;
            System.out.println("Joining Workspace");
            //Whenever a new workspace is created, the creator should automatically join it
            JoinWorkspace(new String[]{w.getName()});
            //We don't want to force a user to need to create a channel just to send messages, so now we automatically
            //make one, We can change the name whenever, but every new workspace gets the same name for it's first channel
            System.out.println("Creating Default Channel");
            CreateChannel(new String[]{w.getName(), "Welcome"});
        }
    }

    /**
     * Join a workspace (put data into the server that the user is in the workspace) and then make the workspace the current
     * @param userArgs
     * @author Dylan Mrzlak
     */
    private static void JoinWorkspace(String[] userArgs) {
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (userArgs.length != 1) {
            System.out.println("Invalid Number or Arguments");
            return;
        }
        System.out.println("Joining Workspace");
        DBSupport.HTTPResponse joinWorkspace = Workspace.joinWorkspace(userArgs[0], curUser.getName());
        if (joinWorkspace.code > 300) {
            System.out.println(joinWorkspace.response);
        } else {
            Workspace w = gson.fromJson(joinWorkspace.response, Workspace.class);
            curWorkspace = w;
            System.out.println("Joined Workspace " + w.getName() + " and set it to your current workspace");
        }
    }

    /**
     * Create a new channel (if possible) and then set the user's current channel to that
     * @param userArgs
     * @author dylan mrzlak
     */
    private static void CreateChannel(String[] userArgs) {
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (userArgs.length != 2) {
            System.out.println("Wrong Number of arguments. Try: create channel - <workspace> <name> ");
            return;
        }
        DBSupport.HTTPResponse cResponse = Channel.createChannel(userArgs[0], userArgs[1]);
        if (cResponse.code > 300) {
            System.out.println(cResponse.response);
        } else {
            System.out.println("Saved Channel");
            Channel c = gson.fromJson(cResponse.response, Channel.class);
            curChannel = c;
        }
    }

    /**
     * Get all of the users in a workspace, this is not a focused search at all.
     * Simply a full list of users that are marked as having joined
     * @param userArgs
     * @author logan garrett
     */
    private static void ViewUsers(String[] userArgs) {
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        //We either get an error and want to consume it, or we get a list to print
        DBSupport.HTTPResponse viewUsers = Workspace.getUsersInWorkspace(curWorkspace.getName());
        if (viewUsers.code > 300) {
            System.out.println("There are no users in this workspace");
        } else {
            String[] userList = gson.fromJson(viewUsers.response, String[].class);
            System.out.println("\nUsers in workspace: " + curWorkspace.getName());
            for (int i = 0; i < userList.length; i++) {
                System.out.println("\t" + userList[i]);
            }
            System.out.println(userList.length + " Users in this workspace found. \n");
        }
    }

    /**
     * Mark a message as pinned (when marked as pinned a pin search will be able to get them)
     * A pinned message is technically important to the channel (but we're not enforcing that and leaving that to the users)
     * @param userArgs
     * @author Joe Hudson
     */
    private static void PinMessage(String[] userArgs) {
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (userArgs.length != 1) {
            System.out.println("Invalid Number or Arguments");
            return;
        }
        DBSupport.HTTPResponse pinMessage = Workspace.pinMessage(userArgs[0]);
        if (pinMessage.code > 300) {
            System.out.println(pinMessage.response);
        } else {
            System.out.println("Pinned message");
            Message m = gson.fromJson(pinMessage.response, Message.class);
            System.out.println("Message Pinned: \n\t" + "[" + m.getwId() + "." + m.getcID() + "." + m.getId() + "]"
                    + m.getContent().replaceAll("_SS_", " "));
        }
    }


    /**
     * Send a message to the channel. Takes the content and will put it into the server.
     * @param userArgs
     * @author thomas mcandrew
     */
    private static void SendTextFile(String[] userArgs) {
        //null checks for the stuff that's required to send a message
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (curChannel == null) {
            System.out.println("User not in Channel;");
            return;
        }
        if (userArgs.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        //Format the message in a way that the data can be sent fully, uncorrupted
        //using _SS_ to replace 'spaces' in the message
        //We don't use http bodies, so the url is not a fan of spaces
        String message = "";
        for (int i = 0; i < userArgs.length; i++) {
            message += userArgs[i] + "_SS_";
        }
        message = message.trim();
        //Send the message to the server, and acknowledge the search
        DBSupport.HTTPResponse sendMessage = Message.sendMessage(curUser.getName(), curWorkspace.getName(), curChannel.getName(), message);
        if (sendMessage.code > 300) {
            System.out.println(sendMessage.response);
        } else {
            Message m = gson.fromJson(sendMessage.response, Message.class);
            System.out.println("Message Sent: \n\t" + m.getContent().replaceAll("_SS_", " "));
        }
    }

    /**
     * Send a message to the channel. Takes the content and will put it into the server.
     * @param userArgs
     * @author thomas mcandrew
     */
    private static void SendMessage(String[] userArgs) {
        //null checks for the stuff that's required to send a message
        if (curUser == null) {
            System.out.println("You need to create a user or sign in to continue");
            return;
        }
        if (curWorkspace == null) {
            System.out.println("User not in workspace");
            return;
        }
        if (curChannel == null) {
            System.out.println("User not in Channel;");
            return;
        }
        if (userArgs.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        //Format the message in a way that the data can be sent fully, uncorrupted
        //using _SS_ to replace 'spaces' in the message
        //We don't use http bodies, so the url is not a fan of spaces
        String message = "";
        for (int i = 0; i < userArgs.length; i++) {
            message += userArgs[i] + "_SS_";
        }
        message = message.trim();
        //Send the message to the server, and acknowledge the search
        DBSupport.HTTPResponse sendMessage = Message.sendMessage(curUser.getName(), curWorkspace.getName(), curChannel.getName(), message);
        if (sendMessage.code > 300) {
            System.out.println(sendMessage.response);
        } else {
            Message m = gson.fromJson(sendMessage.response, Message.class);
            System.out.println("Message Sent: \n\t" + m.getContent().replaceAll("_SS_", " "));
        }
    }


    /**
     * Send a message to a user. Takes the content and will put it into the server.
     * DM's will be able to be seen by a user when a search for dm's is run
     * @param userArgs
     * @author thomas mcandrew
     */
    private static void SendDM(String[] userArgs) {
        if (userArgs.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        //Format the message in a way that the data can be sent fully, uncorrupted
        //using _SS_ to replace 'spaces' in the message
        //We don't use http bodies, so the url is not a fan of spaces
        String directMessage = "";
        for (int i = 1; i < userArgs.length; i++) {
            directMessage += userArgs[i] + "_SS_";
        }
        directMessage = directMessage.trim();
        DBSupport.HTTPResponse dm = Message.sendDirectMessage(curUser.getName(), userArgs[0], directMessage);
        if (dm.code > 300) {
            System.out.println(dm.response);
        } else {
            System.out.println("Joining Workspace");
            Message m = gson.fromJson(dm.response, Message.class);

            System.out.println("Message Sent: \n\t" + m.getContent().replaceAll("_SS_", " "));
        }
    }

    /**
     * Print the base instructions for the app, just a welcome to the app and a short description on how to operate it
      */
    private static void printInstructions() {
        System.out.println("Welcome to Slack# (patent pending), our cheeky, user un-friendly, clone of Slack\n" +
                "\t\tTo run this god forsaken app, type in a command and its arguments.\n" +
                "\t\tIf you dont know the commands or need a refresher. I suggest you git gud skrub\n\n\n\n" +
                "\t\t(Or enter \"help\", I'm not your mommy lol)");
    }

    /**
     * Print the commands that have been implemented thus far
     */
    private static void printHelp() {
        System.out.println("Commands are sent in the order COMMAND - ARGUMENTS\n" +
                "using ' ' to separate arguments\n\n" +
                "create user: create user - <name> <password>\n" +
                "login: login - <username> <password>\n" +
                "create workspace: create workspace - <name of workspace>\n" +
                "join workspace: join - <name of workspace>\n" +
                "search workspace: search workspace - <name of workspace>\n" +
                "create channel: create channel - <workspace name> <channel name>\n" +
                "view users: view users\n" +
                "search user: search user - <name of user>\n"+
                "send to group: send - <message>\n" +
                "direct message: send to - <user> <message>\n" +
                "pin message: pin message - <message>\n" +
                "log messages: log messages\n" +
                "view mentions: view mentions\n");
    }





    private static void WriteFile(String[] linesToWrite, String filePath) {
        //Below is how we'll write to a file
        try {
            //We want to put it in the source directory of the entire project so for Dylan (the author):
            //  "C:\Users\dmrz0\OneDrive\Desktop\Slack\logs\FILENAME"
            // Get that relative directory and if it doesn't exist. Make it
            File dir = new File("..\\..\\logs\\");
            if(!dir.exists()){
                dir.mkdir();
            }
            //Get the file for to write to.
            // It shouldn't really exist unless a user logs twice within a minute
            //If it does exist, delete it, and make a new one
            File toWrite = new File(dir + filePath + ".txt");
            FileWriter fw;
            if(toWrite.exists())
                toWrite.delete();
            toWrite.createNewFile();
            //Set it to be writable
            toWrite.setWritable(true);
            //Prepare to start writing the file. Making a file Writer, and then iteration through the data
            //and writing those lines into the file.
            fw = new FileWriter(toWrite);
            for(String line: linesToWrite){
                fw.write(line);
            }
            //Close the writer to prevent memory leaks
            fw.close();
            //set the file to read only. Gotta keep our logs pure and clean
            toWrite.setReadOnly();
            System.out.println("File " + filePath + "Written to: \n" +
                    "Absolute Path: " + toWrite.getCanonicalPath() + "\n" +
                    "Relative Path: " + toWrite.getPath() + "\n");
        }
        catch (IOException e) {
            //Lots of methods have the chance to throw an error (although they shouldn't now)
            //So we want to print that error.
            e.printStackTrace();
        }
    }

    private static String[] LogMessagesFormat(Message[] messages) {
        String[] file = new String[messages.length];
        //We want to show the Workspace and Channel along with sender for each
        //As channel will change (and workspace will not) we want to keep track of the channel and get its name
        // when it changes. So we'll keep track of a messages cId.
        //We also want to have the Sender's name for each message, and that's not grouped,
        // so we'll need to pull that each message :(
        int cId = -1;
        String channelName = "";
        for (int i = 0; i < messages.length; i++) {
            String messageString = "";
            Message message = messages[i];
            if (message.getcID() != cId) {
                cId = message.getcID();
                DBSupport.HTTPResponse cRepsonse = Channel.getChannelName(cId);
                if (cRepsonse.code >= 300) {
                    // as we want all messages that are public, should an issue from the backend happen,
                    // we want to still display the message. What we'll do is make just use a tab for that
                    channelName = "\t";
                } else {
                    channelName = cRepsonse.response;
                }
            }
            String senderName;
            DBSupport.HTTPResponse uRepsonse = User.getUserNameByID(message.getSenderId());
            if (uRepsonse.code >= 300) {
                // as we want all messages that are public, should an issue from the backend happen,
                // we want to still display the message. What we'll do is make just use a tab for that
                senderName = "\t";
            } else {
                senderName = uRepsonse.response;
            }
            messageString = "[" + curWorkspace.getName() + "].[" + channelName + "]\t" + "FROM: " + senderName +
                    "\n\tMESSAGE: " + message.getContent().replaceAll("_SS_", " ") + "\n";
            file[i] = messageString;
        }
        return file;
    }
}
