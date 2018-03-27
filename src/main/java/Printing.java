public class Printing {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";

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
}
