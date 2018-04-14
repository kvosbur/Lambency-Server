import java.io.Serializable;

public class ChatModel implements Serializable{

    private int chatID;
    private String name;
    private int recent_msg_id;
    private String recent_msg_text;

    public ChatModel(int chatID, String name, int recent_msg_id, String recent_msg_text) {
        this.chatID = chatID;
        this.name = name;
        this.recent_msg_id = recent_msg_id;
        this.recent_msg_text = recent_msg_text;
    }

    public int getChatID() {
        return chatID;
    }

    public int getRecent_msg_id() {
        return recent_msg_id;
    }

    public void setRecent_msg_id(int recent_msg_id) {
        this.recent_msg_id = recent_msg_id;
    }

    public String getRecent_msg_text() {
        return recent_msg_text;
    }

    public void setRecent_msg_text(String recent_msg_text) {
        this.recent_msg_text = recent_msg_text;
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
