public class Printing {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static boolean verbose = true;
    public static void print(Object o){
        if(verbose) {
            System.out.print(o);
        }
    }
    public static void println(Object o){
        if(verbose){
            System.out.println(o);
        }
    }

    public static void printlnEndpoint(Object o){
        if(verbose){
            System.out.println(ANSI_RED + o + ANSI_RESET);
        }
    }

    public static void printlnError(Object o){
        if(verbose){
            System.out.println(ANSI_YELLOW + o + ANSI_RESET);
        }
    }

}
