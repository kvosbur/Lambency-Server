import java.sql.SQLException;

public class EventHandler {

    /**
     *
     * @param event   Event that is to be added to the database
     * @return  (int)   Will return event_id on success, -1 on failure
     */


    public static int createEvent(Event event) {

        try {
            int event_id = LambencyServer.dbc.createEvent(event.getOrg_id(),event.getName(),event.getStart(),
                    event.getEnd(),event.getDescription(),event.getLocation(),event.getImage_path());
            return event_id;
        } catch (SQLException e) {
            System.out.println("Error in creating event: "+event.getName());
            return -1;
        }

    }


    public static int updateEvent(Event event) {
        try{
            LambencyServer.dbc.modifyEventInfo(event.getEvent_id(),event.getName(),event.getStart(),event.getEnd(),
                    event.getDescription(),event.getLocation(),event.getImage_path());
            return 1;
        }
        catch (SQLException e){
            System.out.println("Error in updating Event: "+event.getName());
            return 0;
        }
    }
}
