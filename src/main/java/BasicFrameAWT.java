import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.Transaction;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import projectComponents.*;
/**
 * The `BasicFrameAWT` class is the main entry point for the Graphical Objects Tool application.
 * It extends JFrame and provides a graphical user interface for creating, managing,
 * and exporting various shapes such as points, lines, polygons, and more.
 */
public class BasicFrameAWT extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;

    private double scale = 1.0; // Initial scale factor
    private double translateX = 0; // X-axis translation for panning
    private double translateY = 0; // Y-axis translation for panning
    private final Map<String, ShapeManager> shapeManagers = new HashMap<>(); // Manages different types of shapes
    private final DatabaseManager databaseManager = new DatabaseManager(); // Handles database interactions
    private final JLabel infoLabel = new JLabel("Welcome to the Graphical Objects Tool!"); // Status label
    private ShapeManager currentShapeManager; // The currently active ShapeManager
    private ActionMode currentMode = ActionMode.CREATE; //Current interaction mode (CREATE, SELECT, etc.)
    
    /**
     * Constructs the main application frame.
     * Initialises the GUI components, sets up the layout, and prepares the application.
     */
    public BasicFrameAWT() {
        super();

        // Main Window Setup
        setTitle("Graphical Objects Tool");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 0, 139)); 
        
        // Initialise shape managers
        initializeShapeManagers();

        // Set Application logo 
        setApplicationLogo();

        // Add MenuBar
        createMenuBar();

        // Add Toolbar for Graphical Objects
        createToolBar();

        // Add Drawing Panel
        DrawingPanel drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);


        // Create Welcome Label
        this.infoLabel.setForeground(Color.WHITE);
        this.infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(this.infoLabel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the menu bar with options for file operations, help, and more.
     */
    private void createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        // Add 'New' menu item
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(e -> {
        	 // Reset all shapes in each shape manager
            for (ShapeManager shapeManager : shapeManagers.values()) {
                shapeManager.newFrame();
            }
            repaint();
        });
        fileMenu.add(newMenuItem);

        JMenu importMenuItem = new JMenu("import");
        fileMenu.add(importMenuItem);
     // Add import options
        JMenuItem importFromShapefile = new JMenuItem("... from Shapefile");
        importMenuItem.add(importFromShapefile);
        importFromShapefile.addActionListener(e -> {
            try {
                File file = JFileDataStoreChooser.showOpenFile("shp", null);
                if (file == null) {
                    return;
                }

                FileDataStore store = FileDataStoreFinder.getDataStore(file);
                SimpleFeatureSource featureSource = store.getFeatureSource();
                SimpleFeatureCollection featureCollection = featureSource.getFeatures();

                ReferencedEnvelope envelope = featureCollection.getBounds();
                double minX = envelope.getMinX();
                double maxX = envelope.getMaxX();
                double minY = envelope.getMinY();
                double maxY = envelope.getMaxY();

                double width = maxX - minX;
                double height = maxY - minY;

                // Define a padding percentage
                double paddingFactor = 0.1; // 10% padding

                // Calculate the width and height with padding
                double paddedWidth = width * (1 + 2 * paddingFactor);
                double paddedHeight = height * (1 + 2 * paddingFactor);

                // Calculate the scale
                double scaleX = FRAME_WIDTH / paddedWidth;
                double scaleY = FRAME_HEIGHT / paddedHeight;
                scale = Math.min(scaleX, scaleY);

                // Calculate the new translateX and translateY with padding
                double paddingX = width * paddingFactor;
                double paddingY = height * paddingFactor;
                 
                translateX = -(minX * scale) + (FRAME_WIDTH - (width + 2 * paddingX) * scale) / 2;
                translateY = -(minY * scale) + (FRAME_HEIGHT - (height + 2 * paddingY) * scale) / 2;

                try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
                    while (featureIterator.hasNext()) {
                        SimpleFeature feature = featureIterator.next();
                        Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();

                        List<Double> xValues = new ArrayList<>();
                        List<Double> yValues = new ArrayList<>();

                        if (geometry instanceof MultiPolygon || geometry instanceof Polygon) {
                            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                Geometry polygon = geometry.getGeometryN(i);
                                Coordinate[] coordinates = polygon.getCoordinates();

                                for (Coordinate coord : coordinates) {
                                    xValues.add(coord.x);
                                    yValues.add(coord.y);
                                }

                                shapeManagers.get("Polygon").addShape(xValues, yValues);
                            }
                        } else if (geometry instanceof MultiPoint || geometry instanceof org.locationtech.jts.geom.Point) {
                            xValues = IntStream
                                    .range(0, geometry.getNumGeometries())
                                    .mapToObj(i -> geometry.getGeometryN(i).getCoordinate().x)
                                    .toList();
                            yValues = IntStream
                                    .range(0, geometry.getNumGeometries())
                                    .mapToObj(i -> geometry.getGeometryN(i).getCoordinate().y)
                                    .toList();

                            shapeManagers.get("Point").addShape(xValues, yValues);
                        } else if (geometry instanceof MultiLineString || geometry instanceof LineString) {
                            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                Geometry lineString = geometry.getGeometryN(i);
                                Coordinate[] coordinates = lineString.getCoordinates();

                                for (Coordinate coord : coordinates) {
                                    xValues.add(coord.x);
                                    yValues.add(coord.y);
                                }

                                shapeManagers.get("Line").addShape(xValues, yValues);
                            }
                        }
                    }

                    repaint();
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JMenuItem importFromCSV = new JMenuItem("... from csv");
        importMenuItem.add(importFromCSV);
        importFromCSV.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a CSV File");
            
            // Set the filter for CSV files only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showOpenDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File csvFile = fileChooser.getSelectedFile();
                
                try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String line;
                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2) {
                            String type = parts[0];
                            ShapeManager shapeManager = this.shapeManagers.get(type);

                            String description = parts[1].trim();
                            String[] descriptions = description.split(" ");
                            switch (type) {
                                case "Arc":
                                    ((ArcManager) shapeManager).addShape(new Arc2D.Double(
                                        Double.parseDouble(descriptions[0]),
                                        Double.parseDouble(descriptions[1]),
                                        Double.parseDouble(descriptions[2]),
                                        Double.parseDouble(descriptions[3]),
                                        Double.parseDouble(descriptions[4]),
                                        Integer.parseInt(descriptions[5]),
                                        Integer.parseInt(descriptions[6])
                                    ));
                                    break;
                                case "Circle":
                                    ((CircleManager) shapeManager).addShape(new Ellipse2D.Double(
                                        Double.parseDouble(descriptions[0]),
                                        Double.parseDouble(descriptions[1]),
                                        Double.parseDouble(descriptions[2]),
                                        Double.parseDouble(descriptions[2])
                                    ));
                                    break;
                                case "Ellipse":
                                    ((EllipseManager) shapeManager).addShape(new Ellipse2D.Double(
                                        Double.parseDouble(descriptions[0]),
                                        Double.parseDouble(descriptions[1]),
                                        Double.parseDouble(descriptions[2]),
                                        Double.parseDouble(descriptions[3])
                                    ));
                                    break;
                                case "Line":
                                case "Point":
                                case "Polygon":
                                case "Triangle":
                                    addShapeByPoints(shapeManager, description);
                                    break;
                                case "Rectangle":
                                    ((RectangleManager) shapeManager).addShape(new Rectangle(
                                        (int) Double.parseDouble(descriptions[0]), 
                                        (int) Double.parseDouble(descriptions[1]), 
                                        (int) Double.parseDouble(descriptions[2]), 
                                        (int) Double.parseDouble(descriptions[3])
                                    ));
                                    break;
                                case "Square":
                                    ((SquareManager) shapeManager).addShape(new Rectangle(
                                        (int) Double.parseDouble(descriptions[0]), 
                                        (int) Double.parseDouble(descriptions[1]), 
                                        (int) Double.parseDouble(descriptions[2]), 
                                        (int) Double.parseDouble(descriptions[2])
                                    ));
                                    break;
                                default:
                                    break;
                            }
                        } 
                    }
                } catch (Exception ex1) {
                    JOptionPane.showMessageDialog(this, ex1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            repaint();
        });

        JMenuItem importFromDB = new JMenuItem("... from database");
        importMenuItem.add(importFromDB);
        importFromDB.addActionListener(e -> {
            String dbName = JOptionPane.showInputDialog(this, "Enter Database name");
            String password = JOptionPane.showInputDialog(this, "Enter Database password");

            databaseManager.importConnect(dbName, password);
            if (databaseManager.isConnected()) {
                List<String> lines = databaseManager.getAllObjects();
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String type = parts[0];
                        ShapeManager shapeManager = this.shapeManagers.get(type);
                            
                        String description = parts[1].trim();
                        String[] descriptions = description.split(" ");
                        switch (type) {
                            case "Arc":
                                ((ArcManager) shapeManager).addShape(new Arc2D.Double(
                                    Double.parseDouble(descriptions[0]),
                                    Double.parseDouble(descriptions[1]),
                                    Double.parseDouble(descriptions[2]),
                                    Double.parseDouble(descriptions[3]),
                                    Double.parseDouble(descriptions[4]),
                                    Integer.parseInt(descriptions[5]),
                                    Integer.parseInt(descriptions[6])
                                ));
                                break;
                            case "Circle":
                                ((CircleManager) shapeManager).addShape(new Ellipse2D.Double(
                                    Double.parseDouble(descriptions[0]),
                                    Double.parseDouble(descriptions[1]),
                                    Double.parseDouble(descriptions[2]),
                                    Double.parseDouble(descriptions[2])
                                ));
                                break;
                            case "Ellipse":
                                ((EllipseManager) shapeManager).addShape(new Ellipse2D.Double(
                                    Double.parseDouble(descriptions[0]),
                                    Double.parseDouble(descriptions[1]),
                                    Double.parseDouble(descriptions[2]),
                                    Double.parseDouble(descriptions[3])
                                ));
                                break;
                            case "Line":
                            case "Point":
                            case "Polygon":
                            case "Triangle":
                                addShapeByPoints(shapeManager, description);
                                break;
                            case "Rectangle":
                                ((RectangleManager) shapeManager).addShape(new Rectangle(
                                    (int) Double.parseDouble(descriptions[0]), 
                                    (int) Double.parseDouble(descriptions[1]), 
                                    (int) Double.parseDouble(descriptions[2]), 
                                    (int) Double.parseDouble(descriptions[3])
                                ));
                                break;
                            case "Square":
                                ((SquareManager) shapeManager).addShape(new Rectangle(
                                    (int) Double.parseDouble(descriptions[0]), 
                                    (int) Double.parseDouble(descriptions[1]), 
                                    (int) Double.parseDouble(descriptions[2]), 
                                    (int) Double.parseDouble(descriptions[2])
                                ));
                                break;
                            default:
                                break;
                        }
                    }                
                        
                }
                repaint();
                databaseManager.abortConnection();
            } else {
                JOptionPane.showMessageDialog(this, "Connection failed. Make sure the database exist and the connection is available.", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JMenu exportMenuItem = new JMenu("export");
        fileMenu.add(exportMenuItem);
        
        JMenuItem exportToShapefile = new JMenuItem("... as Shapefile");
        exportMenuItem.add(exportToShapefile);
        exportToShapefile.addActionListener(e -> {
            List<org.locationtech.jts.geom.Point> pointGeometries = ((PointManager) shapeManagers.get("Point")).getGeometries();
            List<org.locationtech.jts.geom.LineString> lineGeometries = ((LineManager) shapeManagers.get("Line")).getGeometries();
            lineGeometries.addAll(((ArcManager) shapeManagers.get("Arc")).getGeometries());
            List<org.locationtech.jts.geom.Polygon> polygonGeometries = ((PolygonManager) shapeManagers.get("Polygon")).getGeometries();
            polygonGeometries.addAll(((TriangleManager) shapeManagers.get("Triangle")).getGeometries());
            polygonGeometries.addAll(((RectangleManager) shapeManagers.get("Rectangle")).getGeometries());
            polygonGeometries.addAll(((SquareManager) shapeManagers.get("Square")).getGeometries());
            polygonGeometries.addAll(((EllipseManager) shapeManagers.get("Ellipse")).getGeometries());
            polygonGeometries.addAll(((CircleManager) shapeManagers.get("Circle")).getGeometries());

            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setDialogTitle("Select a Directory to Save Shapefile");
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int userSelection = directoryChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directory = directoryChooser.getSelectedFile();
                String filename = JOptionPane.showInputDialog(null, "Enter Filename");

                if (filename == null || filename.trim().isEmpty()) {
                    filename = "shapes";
                }

                File pointShapeFile = new File(directory, filename + "_points.shp");
                File lineShapeFile = new File(directory, filename + "_lines.shp");
                File polygonShapeFile = new File(directory, filename + "_polygons.shp");

                try {
                    // Define feature types for point, line, and polygon
                    SimpleFeatureType pointFeatureType = createFeatureType("PointFeatures", org.locationtech.jts.geom.Point.class);
                    SimpleFeatureType lineFeatureType = createFeatureType("LineFeatures", org.locationtech.jts.geom.LineString.class);
                    SimpleFeatureType polygonFeatureType = createFeatureType("PolygonFeatures", org.locationtech.jts.geom.Polygon.class);

                    // Export Points
                    exportGeometries(pointGeometries, pointShapeFile, pointFeatureType);

                    // Export Lines
                    exportGeometries(lineGeometries, lineShapeFile, lineFeatureType);

                    // Export Polygons
                    exportGeometries(polygonGeometries, polygonShapeFile, polygonFeatureType);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            repaint();
        });

        JMenuItem exportToCSV = new JMenuItem("... as csv");
        exportMenuItem.add(exportToCSV);
        exportToCSV.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setDialogTitle("Select a Directory to Save CSV");
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int userSelection = directoryChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directory = directoryChooser.getSelectedFile();
                String filename = JOptionPane.showInputDialog(null, "Enter Filename");

                if (filename == null || filename.trim().isEmpty()) {
                    filename = "shapes";
                }

                File csvFile = new File(directory, filename + ".csv");
                if (!directory.exists()) directory.mkdir();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                    writer.write("Type,Description");
                    writer.newLine();

                    for (ShapeManager shapeManager : shapeManagers.values()) {
                        String csvString = shapeManager.toCSVString();
                        if (!csvString.isEmpty()) {
                            writer.write(csvString);
                        }
                    }
                } catch (Exception ex1) {
                    JOptionPane.showMessageDialog(this, ex1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            repaint();
        });

        JMenuItem exportToDB = new JMenuItem("... to database");
        exportMenuItem.add(exportToDB);
        exportToDB.addActionListener(e -> {
            String dbName = JOptionPane.showInputDialog(this, "Enter Database name");
            String password = JOptionPane.showInputDialog(this, "Enter Database password");

            databaseManager.exportConnect(dbName, password);
            if (databaseManager.isConnected()) {
                databaseManager.clearTable();
                for (ShapeManager shapeManager : shapeManagers.values()) {
                    String csvString = shapeManager.toCSVString();
                    if (!csvString.isEmpty()) {
                        databaseManager.insertObject(csvString);
                    }
                }
            } else JOptionPane.showMessageDialog(this, "Connection failed. Make the connection is available.", "Error", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to quit?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(quitMenuItem);

        bar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutUs = new JMenuItem("About Us");
        aboutUs.addActionListener(e -> JOptionPane.showMessageDialog(this, "Hello", "About Us", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutUs);
        bar.add(helpMenu);
        setJMenuBar(bar);
    }

    private void initializeShapeManagers() {

        shapeManagers.put("Point", new PointManager());
        shapeManagers.put("Line", new LineManager());
        shapeManagers.put("Polygon", new PolygonManager());
        shapeManagers.put("Circle", new CircleManager());
        shapeManagers.put("Ellipse", new EllipseManager());
        shapeManagers.put("Arc", new ArcManager());
        shapeManagers.put("Square", new SquareManager());
        shapeManagers.put("Rectangle", new RectangleManager());
        shapeManagers.put("Triangle", new TriangleManager());

        currentShapeManager = shapeManagers.get("Point");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BasicFrameAWT mainWindow = new BasicFrameAWT();
            mainWindow.setVisible(true);
        });
    }

    private void setApplicationLogo() {
        try {
            Image icon = new ImageIcon("logos/logo.png").getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Failed to load logo: " + e.getMessage());
        }
    }

    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // Prevent toolbar from being moved

        // Add Buttons with Icons
        addButtonToToolBar(toolBar, "Point", "logos/point.png");
        addButtonToToolBar(toolBar, "Line", "logos/line.png");
        addButtonToToolBar(toolBar, "Polygon", "logos/polygon.png");
        addButtonToToolBar(toolBar, "Circle", "logos/circle.png");
        addButtonToToolBar(toolBar, "Ellipse", "logos/ellipse.png");
        addButtonToToolBar(toolBar, "Arc", "logos/arc.png");
        addButtonToToolBar(toolBar, "Square", "logos/square.png");
        addButtonToToolBar(toolBar, "Rectangle", "logos/rectangle.png");
        addButtonToToolBar(toolBar, "Triangle", "logos/triangle.png");

        add(toolBar, BorderLayout.NORTH); // Add toolbar to the top
    }

    private void addButtonToToolBar(JToolBar toolBar, String actionCommand, String iconPath) {
        JButton button = new JButton();
        button.setToolTipText(actionCommand);
        button.setPreferredSize(new Dimension(64, 64)); // Adjust button size

        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image scaledImage = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            button.setText(actionCommand); // Fallback to text if icon is not found
        }

        JPopupMenu popupMenu = createPopupMenu(actionCommand);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                popupMenu.show(button, e.getX(), e.getY());
            }
        });

        toolBar.add(button);
    }

    private JPopupMenu createPopupMenu(String actionCommand) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem createItem = new JMenuItem("Create");
        createItem.addActionListener(e -> {
            currentShapeManager.resetDrawing();
            if (currentShapeManager != shapeManagers.get(actionCommand)) currentShapeManager.resetSelection();
            currentShapeManager = shapeManagers.get(actionCommand);
            repaint();
            infoLabel.setText("Mode: " + "Create " + actionCommand);
            currentMode = ActionMode.CREATE;
        });

        JMenuItem selectItem = new JMenuItem("Select");
        selectItem.addActionListener(e -> {
            currentShapeManager.resetDrawing();
            if (currentShapeManager != shapeManagers.get(actionCommand)) currentShapeManager.resetSelection();
            currentShapeManager = shapeManagers.get(actionCommand);
            repaint();
            infoLabel.setText("Mode: " + "Select " + actionCommand);
            currentMode = ActionMode.SELECT;
        });

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            currentShapeManager.resetDrawing();
            if (currentShapeManager != shapeManagers.get(actionCommand)) currentShapeManager.resetSelection();
            currentShapeManager = shapeManagers.get(actionCommand);
            repaint();
            infoLabel.setText("Mode: " + "Delete " + actionCommand);
            currentMode = ActionMode.DELETE;
        });

        JMenuItem moveItem = new JMenuItem("Move");
        moveItem.addActionListener(e -> {
            currentShapeManager.resetDrawing();
            if (currentShapeManager != shapeManagers.get(actionCommand)) currentShapeManager.resetSelection();
            currentShapeManager = shapeManagers.get(actionCommand);
            repaint();
            infoLabel.setText("Mode: " + "Move " + actionCommand);
            currentMode = ActionMode.MOVE;
        });

        popupMenu.add(createItem);
        popupMenu.add(selectItem);
        popupMenu.add(deleteItem);
        popupMenu.add(moveItem);

        return popupMenu;
    }

    private static void addShapeByPoints(ShapeManager shapeManager, String points) {
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();
        String[] pointsArray = points.split("\\) \\(");

        for (String point : pointsArray) {
            point = point.replace("(", "").replace(")", "");
            String[] coordinates = point.trim().split(" ");

            if (coordinates.length == 2) {
                xValues.add(Double.parseDouble(coordinates[0]));
                yValues.add(Double.parseDouble(coordinates[1]));
            }
        }

        shapeManager.addShape(xValues, yValues);
    }

    private SimpleFeatureType createFeatureType(String typeName, Class<?> geometryClass) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(typeName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("the_geom", geometryClass);
        typeBuilder.setDefaultGeometry("the_geom");
        return typeBuilder.buildFeatureType();
    }

    private void exportGeometries(List<? extends Geometry> geometries, File shapFile, SimpleFeatureType featureType) throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shapFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        dataStore.createSchema(featureType);

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

        for (Geometry geometry : geometries) {
            featureBuilder.add(geometry);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }

        Transaction transaction = new DefaultTransaction("create");
        try {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource();
            DefaultFeatureCollection collection = new org.geotools.feature.DefaultFeatureCollection(null, featureType);
            collection.addAll(features);
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            transaction.close();
            dataStore.dispose();
        }
    }

    private class DrawingPanel extends JPanel {

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentShapeManager.setCurrentMode(currentMode);
                    currentShapeManager.setMouseClickedBehaviour(new MouseEvent(
                            e.getComponent(),
                            e.getID(),
                            e.getWhen(),
                            e.getModifiersEx(),
                            (int) ((e.getX() - translateX) / scale),
                            (int) ((e.getY() - translateY) / scale),
                            e.getClickCount(),
                            e.isPopupTrigger(),
                            e.getButton())
                    );
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentShapeManager.setMousePressedBehaviour(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentShapeManager.setMouseReleasedBehaviour(e);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    currentShapeManager.setMouseDraggedBehaviour(e);
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    currentShapeManager.setCurrentMousePosition(new MouseEvent(
                            e.getComponent(),
                            e.getID(),
                            e.getWhen(),
                            e.getModifiersEx(),
                            (int) ((e.getX() - translateX) / scale),
                            (int) ((e.getY() - translateY) / scale),
                            e.getClickCount(),
                            e.isPopupTrigger(),
                            e.getButton())
                    );
                    repaint();
                }
            });

            addMouseWheelListener(e -> {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    // Determine the mouse position in the component
                    Point mousePosition = e.getPoint();

                    // Calculate new scale factor
                    double delta = 0.1 * e.getWheelRotation();
                    double newScale = Math.max(0.1, scale - delta); // Ensure scale is positive

                    // Calculate scaling ratio
                    double zoomFactor = newScale / scale;

                    // Adjust translation to keep the mouse focus
                    translateX += (1 - zoomFactor) * (mousePosition.x - translateX);
                    translateY += (1 - zoomFactor) * (mousePosition.y - translateY);

                    // Update scale
                    scale = newScale;

                    // Repaint the panel
                    repaint();
                }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalTransform = g2d.getTransform();
            g2d.translate(translateX, translateY);
            g2d.scale(scale, scale);

            setBackground(Color.WHITE);
            for (ShapeManager manager : shapeManagers.values()) {
                manager.paint(g2d);
            }
            g2d.transform(originalTransform);
        }

    }
}
