public class Test {
    int[][] array;
    String word;
    Test () {
        this("");
        this.word = "Ths is my text!";
    }
    Test(String w){
        this.array = new int[10][10];
        for(int i = 0; i < 10;i++){
            for(int j = 0; j < 10; j++){
                this.array[i][j] = i * j;
            }
        }
        this.word = "This is my text! Hello " + w;
    }
    Test(String w, String g){
        this.array = new int[10][10];
        for(int i = 0; i < 10;i++){
            for(int j = 0; j < 10; j++){
                this.array[i][j] = i * j;
            }
        }
        this.word = "This is my text! Hello " + w+ " and "+g;
    }
}
