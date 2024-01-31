package filler;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import filler.entities.Figure;
import filler.utils.GCodeUtils;
import filler.utils.ModelParameters;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame {
   private JPanel panel1;
   public JSpinner wx;
   public JSpinner wy;
   public JSpinner wz;
   public JSpinner defaultCellSize;
   public JSpinner layerHeight;
   public JSpinner epsilonZero;
   public JSpinner epsilonMinimum;
   public JSpinner extruderSize;
   public JSpinner extruderTemperature;
   public JSpinner tableTemperature;
   public JButton buttonCalculate;
   public JComboBox comboBox1;
   public JLabel statusBar;
   private JSpinner fwdExtr;
   private JSpinner bckExtr;
   private SpinnerNumberModel m_wx;
   private SpinnerNumberModel m_wy;
   private SpinnerNumberModel m_wz;
   private SpinnerNumberModel m_layer_height;
   private SpinnerNumberModel m_default_cell;
   private SpinnerNumberModel m_extruder_size;
   private SpinnerNumberModel m_epsilon_0;
   private SpinnerNumberModel m_epsilon_minimum;
   private SpinnerNumberModel m_extruder_temp;
   private SpinnerNumberModel m_table_temp;
   private SpinnerNumberModel fwd;
   private SpinnerNumberModel bck;

   public GUI() {
      this.$$$setupUI$$$();
      this.setContentPane(this.panel1);
      this.panel1.setBorder(new EmptyBorder(20, 10, 10, 10));
      this.setSize(430, 280);
      this.setResizable(false);
      this.setTitle("Dielectric filler");
      this.setDefaultCloseOperation(3);
      this.setSpinnerModels();
      this.buttonCalculate.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (GUI.this.saveSettings()) {
               GUI.this.statusBar.setText("Error. Epsilon rate over 1.0. Choose another material (Epsilon 0).");
            } else {
               if (GUI.this.comboBox1.getSelectedIndex() == 0) {
                  GUI.this.statusBar.setText("Calculating... Pleas wait.");

                  GCodeUtils.G_CODE_UTILS.setDefaultAValue();

                  Figure figure = new Figure();

                  if (figure.addHeader() && figure.makePyramid()) {
                     GUI.this.statusBar.setText("Done");
                  }
               }
            }
         }
      });
   }

   private boolean saveSettings() {
      ModelParameters.MODEL_SETTINGS.setWidthX(this.m_wx.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setWidthY(this.m_wy.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setWidthZ(this.m_wz.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setDefaultCellSize(this.m_default_cell.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setLayerHeight(this.m_layer_height.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setEpsilonZero(this.m_epsilon_0.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setEpsilonMinimum(this.m_epsilon_minimum.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setExtruderSize(this.m_extruder_size.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setExtruderTemp(this.m_extruder_temp.getNumber().intValue());
      ModelParameters.MODEL_SETTINGS.setTableTemp(this.m_table_temp.getNumber().intValue());
      ModelParameters.MODEL_SETTINGS.setFwd(this.fwd.getNumber().doubleValue());
      ModelParameters.MODEL_SETTINGS.setBck(this.bck.getNumber().doubleValue());

      double w = Math.pow(ModelParameters.MODEL_SETTINGS.getWidthX() / 2.0D, 2.0D) + Math.pow(ModelParameters.MODEL_SETTINGS.getWidthY() / 2.0D, 2.0D);
      double h_sq = Math.pow(ModelParameters.MODEL_SETTINGS.getWidthZ(), 2.0D);
      double cos_2 = h_sq / (h_sq + w);

      ModelParameters.MODEL_SETTINGS.setEpsilonRate(ModelParameters.MODEL_SETTINGS.getEpsilonMinimum() / cos_2 / ModelParameters.MODEL_SETTINGS.getEpsilonZero());

      return ModelParameters.MODEL_SETTINGS.getEpsilonRate() > 1.0D;
   }

   private void setSpinnerModels() {
      this.m_wx = new SpinnerNumberModel(36.0D, 0.0D, 1000.0D, 0.05D);
      this.m_wy = new SpinnerNumberModel(28.0D, 0.0D, 1000.0D, 0.05D);
      this.m_wz = new SpinnerNumberModel(36.0D, 0.0D, 1000.0D, 0.05D);
      this.m_layer_height = new SpinnerNumberModel(0.2D, 0.1D, 0.6D, 0.1D);
      this.m_default_cell = new SpinnerNumberModel(3.6D, 2.0D, 6.0D, 0.1D);
      this.m_epsilon_0 = new SpinnerNumberModel(2.4D, 1.0D, 100.0D, 0.01D);
      this.m_epsilon_minimum = new SpinnerNumberModel(1.3D, 1.0D, 100.0D, 0.01D);
      this.m_extruder_size = new SpinnerNumberModel(0.4D, 0.1D, 1.0D, 0.1D);
      this.m_extruder_temp = new SpinnerNumberModel(220, 80, 350, 1);
      this.m_table_temp = new SpinnerNumberModel(130, 50, 200, 1);
      this.fwd = new SpinnerNumberModel(0.0D, 0.0D, 10.0D, 0.001D);
      this.bck = new SpinnerNumberModel(1.0E-4D, 0.0D, 10.0D, 0.001D);
      this.fwdExtr.setModel(this.fwd);
      this.bckExtr.setModel(this.bck);
      this.wx.setModel(this.m_wx);
      this.wy.setModel(this.m_wy);
      this.wz.setModel(this.m_wz);
      this.defaultCellSize.setModel(this.m_default_cell);
      this.extruderSize.setModel(this.m_extruder_size);
      this.epsilonZero.setModel(this.m_epsilon_0);
      this.epsilonMinimum.setModel(this.m_epsilon_minimum);
      this.extruderTemperature.setModel(this.m_extruder_temp);
      this.tableTemperature.setModel(this.m_table_temp);
      this.layerHeight.setModel(this.m_layer_height);
   }

   // $FF: synthetic method
   private void $$$setupUI$$$() {
      JPanel var1 = new JPanel();
      this.panel1 = var1;
      var1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var1.setOpaque(true);
      JPanel var2 = new JPanel();
      var2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var1.add(var2, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var3 = new JPanel();
      var3.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var2.add(var3, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var4 = new JLabel();
      var4.setText("Width X");
      var3.add(var4, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var5 = new JLabel();
      var5.setText("Width Y");
      var3.add(var5, new GridConstraints(1, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var6 = new JLabel();
      var6.setText("Height");
      var3.add(var6, new GridConstraints(2, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var7 = new JLabel();
      var7.setText("Default cell size");
      var3.add(var7, new GridConstraints(3, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var8 = new JLabel();
      var8.setText("Layer height");
      var3.add(var8, new GridConstraints(4, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var9 = new JPanel();
      var9.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var2.add(var9, new GridConstraints(0, 1, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var10 = new JSpinner();
      this.wx = var10;
      var10.setDoubleBuffered(false);
      var9.add(var10, new GridConstraints(0, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, new Dimension(50, -1), (Dimension)null));
      JSpinner var11 = new JSpinner();
      this.wy = var11;
      var9.add(var11, new GridConstraints(1, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var12 = new JSpinner();
      this.wz = var12;
      var9.add(var12, new GridConstraints(2, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var13 = new JSpinner();
      this.defaultCellSize = var13;
      var9.add(var13, new GridConstraints(3, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var14 = new JSpinner();
      this.layerHeight = var14;
      var9.add(var14, new GridConstraints(4, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var15 = new JPanel();
      var15.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var2.add(var15, new GridConstraints(0, 3, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var16 = new JLabel();
      var16.setText("Epsilon 0");
      var15.add(var16, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var17 = new JLabel();
      var17.setText("Epsilon minimum");
      var15.add(var17, new GridConstraints(1, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var18 = new JLabel();
      var18.setText("Extrudet size");
      var15.add(var18, new GridConstraints(2, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var19 = new JLabel();
      var19.setText("Extruder temperature");
      var15.add(var19, new GridConstraints(3, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var20 = new JLabel();
      var20.setText("Table temperature");
      var15.add(var20, new GridConstraints(4, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var21 = new JPanel();
      var21.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var2.add(var21, new GridConstraints(0, 4, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var22 = new JSpinner();
      this.epsilonZero = var22;
      var21.add(var22, new GridConstraints(0, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, new Dimension(50, -1), (Dimension)null));
      JSpinner var23 = new JSpinner();
      this.epsilonMinimum = var23;
      var21.add(var23, new GridConstraints(1, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var24 = new JSpinner();
      this.extruderSize = var24;
      var21.add(var24, new GridConstraints(2, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var25 = new JSpinner();
      this.extruderTemperature = var25;
      var21.add(var25, new GridConstraints(3, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var26 = new JSpinner();
      this.tableTemperature = var26;
      var21.add(var26, new GridConstraints(4, 0, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      Spacer var27 = new Spacer();
      var2.add(var27, new GridConstraints(0, 2, 1, 1, 0, 1, 6, 1, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var28 = new JPanel();
      var28.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var1.add(var28, new GridConstraints(2, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      Spacer var29 = new Spacer();
      var28.add(var29, new GridConstraints(0, 0, 1, 1, 0, 2, 1, 6, (Dimension)null, (Dimension)null, (Dimension)null));
      JButton var30 = new JButton();
      this.buttonCalculate = var30;
      var30.setText("Calculate");
      var28.add(var30, new GridConstraints(1, 1, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JComboBox var31 = new JComboBox();
      this.comboBox1 = var31;
      DefaultComboBoxModel var32 = new DefaultComboBoxModel();
      var32.addElement("Pyramid");
      var32.addElement("Rect. sample");
      var31.setModel(var32);
      var28.add(var31, new GridConstraints(1, 0, 1, 1, 8, 1, 2, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var33 = new JPanel();
      var33.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var1.add(var33, new GridConstraints(3, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, new Dimension(-1, 20), (Dimension)null, 0, true));
      JLabel var34 = new JLabel();
      this.statusBar = var34;
      var34.setText("");
      var33.add(var34, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JPanel var35 = new JPanel();
      var35.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1, false, false));
      var1.add(var35, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var36 = new JLabel();
      var36.setText("Forward extr.");
      var35.add(var36, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var37 = new JSpinner();
      this.fwdExtr = var37;
      var35.add(var37, new GridConstraints(0, 1, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JLabel var38 = new JLabel();
      var38.setText("Backward extr.");
      var35.add(var38, new GridConstraints(0, 2, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null));
      JSpinner var39 = new JSpinner();
      this.bckExtr = var39;
      var35.add(var39, new GridConstraints(0, 3, 1, 1, 8, 1, 6, 0, (Dimension)null, (Dimension)null, (Dimension)null));
   }

   // $FF: synthetic method
   public JComponent $$$getRootComponent$$$() {
      return this.panel1;
   }
}
