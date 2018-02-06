public class Test {
    int[][] array;
    String word;
    Test(String var1, String var2){
        this.array = new int[10][10];
        for(int i = 0; i < 10;i++){
            for(int j = 0; j < 10; j++){
                this.array[i][j] = i * j;
            }
        }
        this.word = var1 + var2;
    }
}
