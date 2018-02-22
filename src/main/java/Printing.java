public class Printing {
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
}
