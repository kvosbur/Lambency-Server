public class MessageModel {

    public String messageText;
    public String sender;
    public String createdAt;

    public MessageModel(){

    }

    public MessageModel(String messageText, String sender, String createdAt)
    {
        this.messageText = messageText;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getSender()
    {
        return sender;
    }
}
