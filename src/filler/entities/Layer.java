package filler.entities;

import com.google.common.collect.Lists;
import filler.utils.ModelParameters;
import java.util.ArrayList;
import java.util.List;

public class Layer {
   private double coord_x;
   private double coord_y;
   private double widthX;
   private double widthY;
   private int counterX;
   private int counterY;
   private double borderX;
   private double borderY;
   private double cellSizeX;
   private double cellSizeY;
   private boolean rotated;
   private boolean reflected;
   private final int layerNumber;

   public Layer(int z, double stepX, double stepY) {
      this.layerNumber = z;
      this.coord_x = (double)z * stepX / 2.0D;
      this.coord_y = (double)z * stepY / 2.0D;
      this.widthX = ModelParameters.MODEL_SETTINGS.getWidthX() - (double)z * stepX;
      this.widthY = ModelParameters.MODEL_SETTINGS.getWidthY() - (double)z * stepY;
      this.cellSizeX = ModelParameters.MODEL_SETTINGS.getDefaultCellSize();
      this.cellSizeY = ModelParameters.MODEL_SETTINGS.getDefaultCellSize();
      this.counterX = (int)(this.widthX / this.cellSizeX);
      this.counterY = (int)(this.widthY / this.cellSizeY);
      this.borderX = (this.widthX - (double)this.counterX * this.cellSizeX) / 2.0D;
      this.borderY = (this.widthY - (double)this.counterY * this.cellSizeY) / 2.0D;
   }

   public void calculate(boolean axis) {
      double minimumCellSize = 2.8D;
      double w;
      int count;
      double border;
      double cellSize;

      if (axis) {
         w = this.widthX;
         count = this.counterX;
         border = this.borderX;
         cellSize = this.cellSizeX;
      } else {
         w = this.widthY;
         count = this.counterY;
         border = this.borderY;
         cellSize = this.cellSizeY;
      }

      if (this.layerNumber % 2 == 1) {
         if (count % 2 == 1) {
            if (count == 1) {
               if (border < 1.0D) {
                  border = w;
               } else {
                  count = 2;
                  border += cellSize / 2.0D;
               }
            } else if (count == 3 && border < 1.0D && border >= 0.2D) {
               cellSize = minimumCellSize;
               count = 4;
               border = (w - (double)count * minimumCellSize) / 2.0D;
               if (border != 0.0D) {
                  border += minimumCellSize;
               }
            } else if (border < 1.8D && border >= 1.0D) {
               ++count;
               border += 0.5D * cellSize;
            } else {
               ++count;
               border += 0.5D * cellSize;
            }
         } else if (count == 0) {
            count = 1;
            border = 3.6D;
         } else if (count == 2) {
            border = w / 2.0D;
         } else if (border < 1.8D && border >= 1.0D) {
            border += cellSize;
         } else {
            --count;
            border += 1.5D * cellSize;
         }
      } else if (count % 2 == 1) {
         if (count == 1) {
            if (border > 1.0D) {
               count = 2;
               border += cellSize / 2.0D;
            } else {
               border = w;
            }
         } else if (count > 2 && border != 0.0D) {
            border += cellSize;
         }
      } else if (count == 0) {
         count = 1;
         border = 3.6D;
      } else if (count == 2) {
         if (border != 0.0D) {
            if (border < 1.8D && border > 1.0D) {
               ++count;
               border += cellSize / 2.0D;
            } else if (border >= 0.6D) {
               cellSize = minimumCellSize;
               count = 3;
               border = (w - minimumCellSize) / 2.0D;
            } else {
               border = w / 2.0D;
            }
         }
      } else if (count > 3) {
         if (border != 0.0D) {
            if (border < 1.8D && border > 1.0D) {
               ++count;
               border += 0.5D * cellSize;
            } else {
               --count;
               border += 1.5D * cellSize;
            }
         } else {
            --count;
            border += 1.5D * cellSize;
         }
      }

      if (axis) {
         this.counterX = count;
         this.borderX = border;
         this.cellSizeX = cellSize;
      } else {
         this.counterY = count;
         this.borderY = border;
         this.cellSizeY = cellSize;
      }

   }

   public String getLayerGCode() {
      StringBuilder stringBuffer = new StringBuilder();
      if (this.borderX == 0.0D) {
         this.borderX = this.cellSizeX;
      }

      if (this.borderY == 0.0D) {
         this.borderY = this.cellSizeY;
      }

      for(int y = 0; y < this.counterY; ++y) {
         boolean rotated_ = y % 2 == 0 == this.rotated;
         List<Point> pointList = new ArrayList();
         Cell currentCell = null;

         for(int x = 0; x < this.counterX; ++x) {
            currentCell = this.calcCell((double)x, (double)y);
            currentCell.setLayer_size(Point.newCoord(this.widthX, this.widthY));
            currentCell.calculateCoef(this.layerNumber);
            currentCell.calc_points(this.reflected, rotated_);
            currentCell.translate(this.coord_x - ModelParameters.MODEL_SETTINGS.getWidthX() / 2.0D, this.coord_y - ModelParameters.MODEL_SETTINGS.getWidthY() / 2.0D);
            pointList.addAll(currentCell.getPoints());
            rotated_ = !rotated_;
         }

         if (y % 2 == 0) {
            stringBuffer.append(currentCell.createLayerTrace(pointList));
         } else {
            stringBuffer.append(currentCell.createLayerTrace(Lists.reverse(pointList)));
         }
      }

      return stringBuffer.toString();
   }

   private Cell calcCell(double x, double y) {
      Cell cell = new Cell();
      if (x == 0.0D) {
         cell.setCoordX(0.0D);
         cell.setSizesX(this.borderX);
      } else if (x == (double)(this.counterX - 1) && x != 0.0D) {
         cell.setCoordX(this.borderX + (x - 1.0D) * this.cellSizeX);
         cell.setSizesX(this.borderX);
      } else {
         cell.setCoordX(this.borderX + (x - 1.0D) * this.cellSizeX);
         cell.setSizesX(this.cellSizeX);
      }

      if (y == 0.0D) {
         cell.setCoordY(0.0D);
         cell.setSizesY(this.borderY);
      } else if (y == (double)(this.counterY - 1) && y != 0.0D) {
         cell.setCoordY(this.borderY + (y - 1.0D) * this.cellSizeY);
         cell.setSizesY(this.borderY);
      } else {
         cell.setCoordY(this.borderY + (y - 1.0D) * this.cellSizeY);
         cell.setSizesY(this.cellSizeY);
      }

      if (this.widthX < 3.6D) {
         this.widthX = 3.6D;
         this.coord_x = ModelParameters.MODEL_SETTINGS.getWidthX() / 2.0D - 1.8D;
      }

      if (this.widthY < 3.6D) {
         this.widthY = 3.6D;
         this.coord_y = ModelParameters.MODEL_SETTINGS.getWidthY() / 2.0D - 1.8D;
      }

      return cell;
   }

   public void setRotatedAndReflected(boolean reflected, boolean rotated) {
      this.reflected = reflected;
      this.rotated = rotated;
   }
}
