package hc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Main {
    public static void main(String[] args) {


//        LocalDateTime currentDateTime = LocalDateTime.now();
//        currentDateTime = currentDateTime.minusWeeks(1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String formattedDateTime = currentDateTime.format(formatter);
//
//        System.out.println(formattedDateTime);
 //       System.out.println("\uD83D\uDE00");

        //decode(str);
//        String replace="NuaciDTml6UK";
//        String base64EncodedString = replace;
//        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
//        String decodedString = new String(decodedBytes);
//        System.out.println(decodedString);


        String str="相册-10-12";
        System.out.println(processTime(str));


    }

    private static void decode(String str){
        StringBuilder output = new StringBuilder();
        String[] lines = str.split("\n");
        for (String line : lines) {
            output.append(line).append("==").append("\n");
        }
        str=output.toString();
        String[] a = str.split("=\n");
        int i=1;
        for(String ss:a) {

            String replace = ss.replace("=","");
            String base64EncodedString = replace;
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
            String decodedString = new String(decodedBytes);
            System.out.println((i++)+"   "+decodedString);
        }
    }

    private static boolean processTime(String time)  {
        String format = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);

        try {
            dateFormat.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}