package filler;

import java.awt.Component;
import filler.entities.Figure;

public class Application {
   public static final GUI gui = new GUI();
   public static void main(String[] args) {
      System.out.println(gui);

      gui.setVisible(true);
      gui.setLocationRelativeTo((Component)null);
   }
}
