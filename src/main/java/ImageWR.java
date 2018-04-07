import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;

public class ImageWR {






    /*
        Returns a string representing the file path to the image
        the input should be an image encoded using a base 64 encoding.


        @param imgEncoding : the base 64 encoded image string
        @return            : the file path to where the image is located in the file system
        @throws     `      : IOException when there is an issue writing to a file
     */

    /*
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
    */

    public static String saveImage(BufferedImage img) throws IOException{

            String path = "photos";

            File directory = new File(path);
            Printing.println("dir exists: " + directory.exists());
            if (!directory.exists()) {
                throw new IOException("photos directory is missing");
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }

            Date date = new Date();
            String fileName = date.toString() + ".jpg";
            File image = new File(path + "/" + fileName);

            ImageIO.write(img, "jpg", image);

            return fileName;

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


    public static void fix(){
        System.out.println("here");


        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        File folder = new File("photos");

        File [] list = folder.listFiles();

        try {
            for (File file : list) {
                Printing.println(file.getName());
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
                fileReader.close();
                String name = "photos/" + file.getName();

                int index = name.lastIndexOf(".");
                name = name.substring(0, index);
                name = name + ".jpg";

                Printing.println("new filename: " + name);


                BASE64Decoder decoder = new BASE64Decoder();
                byte[] imageByte = decoder.decodeBuffer(stringBuffer.toString());

                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                BufferedImage image = ImageIO.read(bis);

                File f = new File(name);
                ImageIO.write(image, "jpg", f);

                break;

            }
        }catch(Exception e){
            Printing.printlnException(e);
        }
    }


    public static void main(String[] args){
        BufferedImage img = null;
        System.out.println("here");
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        /*
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
        */


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
