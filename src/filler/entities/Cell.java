package filler.entities;

import filler.utils.GCodeUtils;
import filler.utils.ModelParameters;
import java.util.ArrayList;
import java.util.List;

public class Cell {
   public static final String NUMBER_FORMAT = "%.4f";
   private Point coord = new Point();
   private Point sizes = new Point();
   private Point layer_size = new Point();
   private int layerNumber;
   private List<Point> points;
   private List<Double> branch = new ArrayList();
   private int maxBranchCount;
   private double epsilonCoef;
   private double extruderSize;
   private double fixPadding;

   public void setCoordX(double coordX) {
      this.coord.setX(coordX);
   }

   public void setCoordY(double coordY) {
      this.coord.setY(coordY);
   }

   public void setSizesX(double sizesX) {
      this.sizes.setX(sizesX);
   }

   public void setSizesY(double sizesY) {
      this.sizes.setY(sizesY);
   }

   public Cell() {
      this.extruderSize = ModelParameters.MODEL_SETTINGS.getExtruderSize();
      this.fixPadding = this.extruderSize / 2.0D;
      this.points = new ArrayList();
      this.maxBranchCount = 15;
   }

   public void setLayer_size(Point sizes) {
      this.layer_size.setX(sizes.getX());
      this.layer_size.setY(sizes.getY());
   }

   public void calculateCoef(int z) {
      this.layerNumber = z;

      double h_2 = Math.pow(ModelParameters.MODEL_SETTINGS.getWidthZ() - (double)this.layerNumber * 0.2D, 2.0D);
      double x_2 = Math.pow(this.layer_size.getX() / 2.0D - this.coord.getX() - this.sizes.getX() / 2.0D, 2.0D);
      double y_2 = Math.pow(this.layer_size.getY() / 2.0D - this.coord.getY() - this.sizes.getY() / 2.0D, 2.0D);
      this.epsilonCoef = ModelParameters.MODEL_SETTINGS.getEpsilonRate() * (ModelParameters.MODEL_SETTINGS.getEpsilonZero() * h_2 / (h_2 + x_2 + y_2)) * 0.714D - 0.658D;

      if (this.epsilonCoef < 0.0D) {
         System.out.println("Epsilon < 0");
      }

   }

   public void calc_points(boolean reflected, boolean rotated) {
      int leg_max = this.maxBranchCount;
      double horPadding = 0.0D;
      double verPadding = 0.0D;
      double verPaddingStep = 0.01D;
      double horPaddingStep = 0.01D;

      if (rotated) {
         this.sizes.replace();
      }

      boolean horOrVerPadding = this.sizes.getX() >= this.sizes.getY();

      while(this.epsilonCoef <= this.calc_square(horPadding, verPadding)) {
         if (leg_max != this.maxBranchCount) {
            horOrVerPadding = !horOrVerPadding;
            leg_max = this.maxBranchCount;
         }

         if (horOrVerPadding) {
            horPadding += horPaddingStep;
         } else {
            verPadding += verPaddingStep;
         }
      }

      List<Point> listFwd = new ArrayList();
      List<Point> listBck = new ArrayList();
      listFwd.add(Point.newCoord(this.extruderSize / 2.0D, this.extruderSize / 2.0D));
      listBck.add(Point.newCoord(this.sizes.getX() - this.extruderSize / 2.0D, this.sizes.getY() - this.extruderSize / 2.0D));

      int orient = 0;

      for(int i = 1; i < this.branch.size(); ++i) {
         if ((Double)this.branch.get(i) != 0.0D) {
            switch(orient) {
            case 0:
               listFwd.add(Point.newCoord(((Point)listFwd.get(listFwd.size() - 1)).getX() + (Double)this.branch.get(i - 1), ((Point)listFwd.get(listFwd.size() - 1)).getY()));
               listBck.add(0, Point.newCoord(((Point)listBck.get(0)).getX() - (Double)this.branch.get(i - 1), ((Point)listBck.get(0)).getY()));
               orient = 1;
               break;
            case 1:
               listFwd.add(Point.newCoord(((Point)listFwd.get(listFwd.size() - 1)).getX(), ((Point)listFwd.get(listFwd.size() - 1)).getY() + (Double)this.branch.get(i - 1)));
               listBck.add(0, Point.newCoord(((Point)listBck.get(0)).getX(), ((Point)listBck.get(0)).getY() - (Double)this.branch.get(i - 1)));
               orient = 2;
               break;
            case 2:
               listFwd.add(Point.newCoord(((Point)listFwd.get(listFwd.size() - 1)).getX() - (Double)this.branch.get(i - 1), ((Point)listFwd.get(i - 1)).getY()));
               listBck.add(0, Point.newCoord(((Point)listBck.get(0)).getX() + (Double)this.branch.get(i - 1), ((Point)listBck.get(0)).getY()));
               orient = 3;
               break;
            case 3:
               listFwd.add(Point.newCoord(((Point)listFwd.get(i - 1)).getX(), ((Point)listFwd.get(i - 1)).getY() - (Double)this.branch.get(i - 1)));
               listBck.add(0, Point.newCoord(((Point)listBck.get(0)).getX(), ((Point)listBck.get(0)).getY() + (Double)this.branch.get(i - 1)));
               orient = 0;
               break;
            default:
               System.out.println("ERROR: variable: 'orient' is overstate.T");
            }
         }
      }

      this.points.addAll(listFwd);
      this.points.addAll(listBck);
      if (reflected) {
         this.reflectX();
      }

      if (rotated) {
         this.rotate90(reflected);
      }

   }

   private double calc_square(double horPadding, double verPadding) {
      List<Double> semiBranch = new ArrayList();
      semiBranch.add(this.sizes.getX() / 2.0D - this.fixPadding);
      semiBranch.add(this.sizes.getX() / 2.0D - this.fixPadding - horPadding);
      semiBranch.add(this.sizes.getY() / 2.0D - this.fixPadding);
      semiBranch.add(this.sizes.getY() / 2.0D - this.fixPadding - verPadding - this.extruderSize);

      int i;
      for(i = 4; i < 20; ++i) {
         if (i % 2 == 0) {
            semiBranch.add(this.sizes.getX() / 2.0D - this.fixPadding - (double)i / 2.0D * horPadding - ((double)i / 2.0D - 1.0D) * this.extruderSize);
         } else {
            semiBranch.add(this.sizes.getY() / 2.0D - this.fixPadding - (double)(i - 1) / 2.0D * verPadding - (double)(i - 1) / 2.0D * this.extruderSize);
         }
      }

      label55:
      for(i = 2; i < semiBranch.size(); ++i) {
         if ((Double)semiBranch.get(i) < 0.0D) {
            int j = i;

            while(true) {
               if (j >= semiBranch.size()) {
                  break label55;
               }

               semiBranch.set(j, 0.0D);
               this.maxBranchCount = i - 2;
               ++j;
            }
         }
      }

      this.branch.clear();
      this.branch.add((Double)semiBranch.get(0) + (Double)semiBranch.get(1));
      this.branch.add((Double)semiBranch.get(2) + (Double)semiBranch.get(3));
      this.branch.add((Double)semiBranch.get(1) + (Double)semiBranch.get(4));

      for(i = 5; i < semiBranch.size() && !((Double)semiBranch.get(i) < 0.0D); ++i) {
         this.branch.add((Double)semiBranch.get(i - 2) + (Double)semiBranch.get(i));
      }

      double square = Math.pow(this.extruderSize, 2.0D);

      for(i = 0; i < this.branch.size(); ++i) {
         if ((Double)this.branch.get(i) > 0.0D) {
            double ex_2 = Math.pow(this.extruderSize / 2.0D, 2.0D);
            square += 2.0D * ((Double)this.branch.get(i) * this.extruderSize - ex_2 + 3.1415D * ex_2 / 8.0D);
         }
      }

      double s_s0 = square / this.sizes.getX() / this.sizes.getY();
      s_s0 = (double)Math.round(100.0D * s_s0) / 100.0D;
      return s_s0;
   }

   public String createLayerTrace(List<Point> pointList) {
      this.points.clear();
      this.points = pointList;
      return this.createLayerTrace(true);
   }

   public String createLayerTrace(boolean last) {
      double z1 = (double)(this.layerNumber + 1) * 0.2D;

      StringBuilder gString = new StringBuilder();

      gString.append("\nG1 X").append(String.format("%.4f", ((Point)this.points.get(0)).getX()));
      gString.append(" Y").append(String.format("%.4f", ((Point)this.points.get(0)).getY()));
      gString.append(" Z").append(String.format("%.4f", z1));
      gString.append(" F").append(GCodeUtils.G_CODE_UTILS.getMovement_speed(this.layerNumber));
      gString.append(" A").append(GCodeUtils.G_CODE_UTILS.getAAxisValue(ModelParameters.MODEL_SETTINGS.getFwd()));

      for(int i = 1; i < this.points.size(); ++i) {
         gString.append("\nG1");
         gString.append(" X").append(String.format("%.4f", ((Point)this.points.get(i)).getX()));
         gString.append(" Y").append(String.format("%.4f", ((Point)this.points.get(i)).getY()));
         gString.append(" Z").append(String.format("%.4f", z1));
         gString.append(" F").append(GCodeUtils.G_CODE_UTILS.getMovement_speed(this.layerNumber));
         double aValueMed = Math.pow(((Point)this.points.get(i - 1)).getX() - ((Point)this.points.get(i)).getX(), 2.0D) + Math.pow(((Point)this.points.get(i - 1)).getY() - ((Point)this.points.get(i)).getY(), 2.0D);
         GCodeUtils.G_CODE_UTILS.setAAxisValue(GCodeUtils.G_CODE_UTILS.getAAxisValue() + Math.sqrt(aValueMed) * GCodeUtils.G_CODE_UTILS.getMaterialCount());
         String aValueStr = String.valueOf(GCodeUtils.G_CODE_UTILS.getAAxisValue());
         gString.append(" A").append(aValueStr);
      }

      gString.append("\n;");
      if (last) {
         gString.append("\nG1");
         gString.append(" X").append(String.format("%.4f", ((Point)this.points.get(this.points.size() - 1)).getX()));
         gString.append(" Y").append(String.format("%.4f", ((Point)this.points.get(this.points.size() - 1)).getY()));
         gString.append(" Z").append(String.format("%.4f", z1 + ModelParameters.MODEL_SETTINGS.getLayerHeight()));
         gString.append(" F4000");
         gString.append(" A").append(String.valueOf(GCodeUtils.G_CODE_UTILS.getAAxisValue() - ModelParameters.MODEL_SETTINGS.getBck())).append("\n; lineEnd");
      }

      return gString.toString();
   }

   // private void rotate90() {
   //    this.points.forEach((point) -> {
   //       point.set(Point.newCoord(-point.getY() + this.sizes.getY(), point.getX()));
   //    });
   // }

   private void rotate90(boolean ref) {
      if (ref) {
         this.points.forEach((point) -> {
            point.set(Point.newCoord(-point.getY() + this.sizes.getY(), point.getX()));
         });
      } else {
         this.points.forEach((point) -> {
            point.set(Point.newCoord(point.getY(), point.getX() * -1.0D + this.sizes.getX()));
         });
      }

   }

   public void translate(double additionX, double additionY) {
      for(int i = 0; i < this.points.size(); ++i) {
         this.points.set(i, Point.newCoord(((Point)this.points.get(i)).getX() + this.coord.getX() + additionX, ((Point)this.points.get(i)).getY() + this.coord.getY() + additionY));
      }

   }

   public List<Point> getPoints() {
      return this.points;
   }

   private void reflectX() {
      this.points.forEach((point) -> {
         point.setY(-point.getY() + this.sizes.getY());
      });
   }

   private void reflectY() {
      this.points.forEach((point) -> {
         point.setX(-point.getX() + this.sizes.getX());
      });
   }
}
