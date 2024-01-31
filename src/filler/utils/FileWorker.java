package filler.utils;

import filler.Application;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class FileWorker {
   public static void write(String fileName, String gcodeString) {
      try {
         if (!fileName.isEmpty() && !fileName.endsWith(".gcode")) {
            fileName = fileName + ".gcode";
         }
      } catch (Exception var10) {
         Application.gui.statusBar.setText("Error. File no found.");
         throw new RuntimeException(var10);
      }

      File file = new File(fileName);

      try {
         if (!file.exists()) {
            file.createNewFile();
         }

         PrintWriter output;
         if (gcodeString.contains("M136")) {
            output = new PrintWriter(file.getAbsoluteFile());
         } else {
            output = new PrintWriter(new FileOutputStream(file.getAbsoluteFile(), true));
         }

         try {
            output.print(gcodeString);
         } finally {
            output.close();
         }

      } catch (IOException var9) {
         throw new RuntimeException(var9);
      }
   }

   public static String read(String fileName) throws FileNotFoundException {
      StringBuilder stringBuilder = new StringBuilder();
      exists(fileName);
      File headerFile = new File(fileName);

      try {
         BufferedReader input = new BufferedReader(new FileReader(headerFile.getAbsoluteFile()));

         String string;
         try {
            while((string = input.readLine()) != null) {
               stringBuilder.append(string);
               stringBuilder.append("\n");
            }
         } finally {
            input.close();
         }
      } catch (IOException var9) {
         throw new RuntimeException(var9);
      }

      if (fileName.contains("header")) {
         int index = stringBuilder.indexOf("#TABLE");
         if (index > -1) {
            stringBuilder.replace(index, index + 6, String.valueOf(ModelParameters.MODEL_SETTINGS.getTableTemp()));
         } else {
            Application.gui.statusBar.setText(String.format("Not found %s keyword. Check the templates.", "#TABLE"));
         }

         index = stringBuilder.indexOf("#EXTRU");
         if (index > -1) {
            stringBuilder.replace(index, index + 6, String.valueOf(ModelParameters.MODEL_SETTINGS.getExtruderTemp()));
         } else {
            Application.gui.statusBar.setText(String.format("Not found %s keyword. Check the templates.", "#EXTRU"));
         }
      }

      return stringBuilder.toString();
   }

   private static void exists(String fileName) throws FileNotFoundException {
      File file = new File(fileName);
      if (!file.exists()) {
         Application.gui.statusBar.setText("Cannot find file: " + fileName);
         throw new FileNotFoundException(file.getName());
      }
   }
}
