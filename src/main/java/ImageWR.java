import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ImageWR {






    /**
        Returns a string representing the file path to the image
        the input should be an image encoded using a base 64 encoding.


        @param imgEncoding : the base 64 encoded image string
        @return            : the file path to where the image is located in the file system
        @throws     `      : IOException when there is an issue writing to a file
     */

    public static String writeImageToFile(String imgEncoding) throws IOException{
        if(imgEncoding == null){
            Printing.println("imgEncoding read null");
            return null;
        }

        String path = "~/Lambency/files/images/";
        //String path = "../../Lambency/files/images";
        Date date = new Date();
        String fileName = date.toString() + ".txt";

        File directory = new File(path);
        Printing.println("dir exists: "+directory.exists());
        if (!directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        Path destinationFile = Paths.get(path, fileName);
        Files.write(destinationFile, imgEncoding.getBytes());


        return path+fileName;
    }

    /**

        Returns the base 64 encoded string stored in a file represented by 'filepath'

        @param filepath : string representing the filepath to where the encoded image is stored
        @return         : string representing the base 64 encoded image
        @throws         : IOException if error writing to file

     */
    public static String getEncodedImageFromFile(String filepath){
        try {
            if (filepath == null) {
                Printing.println("Filepath read null;");
                return null;
            }
            File file = new File(filepath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            //Printing.println("Contents of file:");
            //Printing.println(stringBuffer.toString());

            return stringBuffer.toString();
        }
        catch(IOException e){
            System.out.println("Could nt find filepath with image so setting null image");
            return null;
        }
    }

    public static void main(String[] args){
        BufferedImage img = null;
        try {
            Printing.println(System.getProperty("user.dir"));
            img = ImageIO.read(new File(System.getProperty("user.dir")+"/resources/image.png"));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            os.flush();
            BASE64Encoder encoder = new BASE64Encoder();
            String imageString = encoder.encode(os.toByteArray());
            //Printing.println("Encoded: "+imageString);
            os.close();
            ImageWR iwr = new ImageWR();
            String ptName = iwr.writeImageToFile(imageString);
            Printing.println(ptName);

            byte[] imageByte;
            BASE64Decoder decoder = new BASE64Decoder();
            String returned = iwr.getEncodedImageFromFile(ptName);
            //Printing.println("returned: "+returned);
            imageByte = decoder.decodeBuffer(returned);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            BufferedImage image = ImageIO.read(bis);
            showImage(image);
            bis.close();


        } catch (IOException e) {
            Printing.println("ERROR Main: "+e);
        }

    }

    public static void showImage(BufferedImage img){
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        JLabel label = new JLabel( new ImageIcon(img) );
        dialog.add( label );
        dialog.pack();
        dialog.setVisible(true);
    }

}
