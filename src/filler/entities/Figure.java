package filler.entities;

import com.google.common.collect.ImmutableList;
import filler.utils.FileWorker;
import filler.utils.GCodeUtils;
import filler.utils.ModelParameters;
import java.awt.Component;
import java.io.FileNotFoundException;
import java.util.List;
import javax.swing.JFileChooser;

public class Figure {
   private final double stepX;
   private final double stepY;
   private String pathToFile;
   private byte layerType = 0;

   public Figure() {
      this.stepX = ModelParameters.MODEL_SETTINGS.getWidthX() * ModelParameters.MODEL_SETTINGS.getLayerHeight() / ModelParameters.MODEL_SETTINGS.getWidthZ();
      this.stepY = ModelParameters.MODEL_SETTINGS.getWidthY() * ModelParameters.MODEL_SETTINGS.getLayerHeight() / ModelParameters.MODEL_SETTINGS.getWidthZ();
      JFileChooser fileOpen = new JFileChooser();
      int ret = fileOpen.showDialog((Component)null, "Select file to save");
      if (ret == 0) {
         this.pathToFile = fileOpen.getSelectedFile().getAbsolutePath();
      }

   }

   public boolean addHeader() {
      if (!this.addHeader_()) {
         return false;
      } else {
         this.addRoundBase();
         return true;
      }
   }

   public boolean makePyramid() {
      int layersCount = (int)(ModelParameters.MODEL_SETTINGS.getWidthZ() / ModelParameters.MODEL_SETTINGS.getLayerHeight());

      for(int z = 0; z < layersCount; ++z) {
         Layer layers = new Layer(z, this.stepX, this.stepY);
         switch(this.layerType) {
         case 0:
            layers.setRotatedAndReflected(false, false);
            if (z % 2 == 1) {
               ++this.layerType;
            }
            break;
         case 1:
            layers.setRotatedAndReflected(true, false);
            if (z % 2 == 1) {
               ++this.layerType;
            }
            break;
         case 2:
            layers.setRotatedAndReflected(false, true);
            if (z % 2 == 1) {
               ++this.layerType;
            }
            break;
         case 3:
            layers.setRotatedAndReflected(true, true);
            if (z % 2 == 1) {
               this.layerType = 0;
            }
         }

         layers.calculate(true);
         layers.calculate(false);
         String layerGCode = layers.getLayerGCode();
         FileWorker.write(this.pathToFile, layerGCode);
      }

      return this.addTail();
   }

   private boolean addHeader_() {
      try {
         FileWorker.write(this.pathToFile, FileWorker.read("templates/header.gcode"));
         return true;
      } catch (FileNotFoundException var2) {
         var2.printStackTrace();
         return false;
      }
   }

   private void addRoundBase() {
      double x = ModelParameters.MODEL_SETTINGS.getWidthX();
      double y = ModelParameters.MODEL_SETTINGS.getWidthY();
      double extruderSize = ModelParameters.MODEL_SETTINGS.getExtruderSize();
      String baseGLine = this.addGLine(ImmutableList.of(Point.newCoord(-2.0D * extruderSize, -2.0D * extruderSize), Point.newCoord(-2.0D * extruderSize, y + 2.0D * extruderSize), Point.newCoord(x + 2.0D * extruderSize, y + 2.0D * extruderSize), Point.newCoord(x + 2.0D * extruderSize, -2.0D * extruderSize), Point.newCoord(-extruderSize, -2.0D * extruderSize), Point.newCoord(-extruderSize, y + extruderSize), Point.newCoord(x + extruderSize, y + extruderSize), Point.newCoord(x + extruderSize, -extruderSize), Point.newCoord(0.0D, -extruderSize)));
      FileWorker.write(this.pathToFile, baseGLine);
   }

   private String addGLine(List<Point> pointList) {
      int z = 0;

      StringBuilder str = new StringBuilder();
      String G1Format = "\nG1 X%s Y%s Z%s F%s A%s";
      pointList.forEach((point) -> {
         point.translate(Point.newCoord(-ModelParameters.MODEL_SETTINGS.getWidthX() / 2.0D, -ModelParameters.MODEL_SETTINGS.getWidthY() / 2.0D));
      });
      str.append(String.format(G1Format, String.format("%.4f", ((Point)pointList.get(0)).getX()), String.format("%.4f", ((Point)pointList.get(0)).getY()), String.format("%.4f", 0.2D), GCodeUtils.G_CODE_UTILS.getMovement_speed(z), GCodeUtils.G_CODE_UTILS.getAAxisValue()));

      for(int i = 1; i < pointList.size(); ++i) {
         double aValueMed = Math.pow(((Point)pointList.get(i - 1)).getX() - ((Point)pointList.get(i)).getX(), 2.0D) + Math.pow(((Point)pointList.get(i - 1)).getY() - ((Point)pointList.get(i)).getY(), 2.0D);
         double aValue = GCodeUtils.G_CODE_UTILS.getAAxisValue() + Math.sqrt(aValueMed) * GCodeUtils.G_CODE_UTILS.getMaterialCount();
         str.append(String.format(G1Format, String.format("%.4f", ((Point)pointList.get(i)).getX()), String.format("%.4f", ((Point)pointList.get(i)).getY()), String.format("%.4f", 0.2D), GCodeUtils.G_CODE_UTILS.getMovement_speed(z), aValue));
         GCodeUtils.G_CODE_UTILS.setAAxisValue(aValue);
      }

      return str.append("\n").toString();
   }

   private boolean addTail() {
      try {
         FileWorker.write(this.pathToFile, FileWorker.read("templates/tail.gcode"));
         return true;
      } catch (FileNotFoundException var2) {
         var2.printStackTrace();
         return false;
      }
   }
}
