public class ChatModel {

    private int chatID;
    private String name;

    public ChatModel(int chatID, String name) {
        this.chatID = chatID;
        this.name = name;
    }

    public int getChatID() {
        return chatID;
    }

    public void setChatID(int chatID) {
        this.chatID = chatID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
