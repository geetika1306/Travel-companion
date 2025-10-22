import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
public class TravelCompanion {
    static class Destination {
        private String city;
        private double travelCost; 
        private String bestSeason;
        private double rating; 
        public Destination(String city, double travelCost, String bestSeason, double rating) {
            this.city = city;
            this.travelCost = travelCost;
            this.bestSeason = bestSeason;
            this.rating = rating;
        }
        public String getCity() { return city; }
        public double getTravelCost() { return travelCost; }
        public String getBestSeason() { return bestSeason; }
        public double getRating() { return rating; }
        @Override
        public String toString() {
            return city + " (₹" + String.format("%.2f", travelCost) + ", " + bestSeason + ", ⭐" + rating + ")";
        }
        public String toCSV() {
            return escapeCSV(city) + "," + travelCost + "," + escapeCSV(bestSeason) + "," + rating;
        }
        private String escapeCSV(String s) {
            if (s.contains(",") || s.contains("\"")) {
                return "\"" + s.replace("\"", "\"\"") + "\"";
            }
            return s;
        }
    }
    static class Trip {
        private Destination dest;
        private int nights;
        private double hotelPerNight;
        private double foodPerDay;

        public Trip(Destination dest, int nights, double hotelPerNight, double foodPerDay) {
            this.dest = dest;
            this.nights = nights;
            this.hotelPerNight = hotelPerNight;
            this.foodPerDay = foodPerDay;
        }

        public Destination getDestination() { return dest; }
        public int getNights() { return nights; }
        public double getHotelPerNight() { return hotelPerNight; }
        public double getFoodPerDay() { return foodPerDay; }

        public double totalCost() {
            double travel = dest.getTravelCost();
            double hotel = hotelPerNight * nights;
            double food = foodPerDay * nights;
            return travel + hotel + food;
        }

        public String toCSV() {
            return dest.toCSV() + "," + nights + "," + hotelPerNight + "," + foodPerDay + "," + totalCost();
        }
    }
    static class TravelManager {
        private final List<Destination> destinations = new ArrayList<>();
        private final List<Trip> trips = new ArrayList<>();

        public void addDestination(Destination d) { destinations.add(d); }
        public List<Destination> getDestinations() { return destinations; }
        public void addTrip(Trip t) { trips.add(t); }
        public List<Trip> getTrips() { return trips; }

        public Optional<Destination> cheapestDestination() {
            return destinations.stream().min(Comparator.comparingDouble(Destination::getTravelCost));
        }

        public Optional<Destination> bestRatedDestination() {
            return destinations.stream().max(Comparator.comparingDouble(Destination::getRating));
        }

        public void saveTripsToCSV(File file) throws IOException {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("city,travelCost,bestSeason,rating,nights,hotelPerNight,foodPerDay,totalCost");
                bw.newLine();
                for (Trip t : trips) {
                    bw.write(t.toCSV());
                    bw.newLine();
                }
            }
        }
    }
    private final TravelManager manager = new TravelManager();
    private final JFrame frame = new JFrame("Smart Travel Companion");
    private final DefaultListModel<Destination> destListModel = new DefaultListModel<>();
    private final JList<Destination> destJList = new JList<>(destListModel);
    private final DefaultTableModel tripsTableModel = new DefaultTableModel(
            new String[]{"City", "Nights", "Hotel/Night", "Food/Day", "Total"}, 0);
    private final JTextField cityField = new JTextField();
    private final JTextField travelCostField = new JTextField();
    private final JTextField seasonField = new JTextField();
    private final JTextField ratingField = new JTextField();
    private final JTextField nightsField = new JTextField("1");
    private final JTextField hotelPerNightField = new JTextField("0");
    private final JTextField foodPerDayField = new JTextField("0");
    private final JTextArea outputArea = new JTextArea(6, 40);

    public TravelCompanion() {
        initializeSampleData();
        buildGUI();
    }
    private void initializeSampleData() {
        manager.addDestination(new Destination("Goa", 5000, "Winter", 4.5));
        manager.addDestination(new Destination("Manali", 4200, "Winter", 4.7));
        manager.addDestination(new Destination("Rishikesh", 3200, "Autumn", 4.4));
        manager.addDestination(new Destination("Kolkata", 2500, "Winter", 4.0));
        for (Destination d : manager.getDestinations()) destListModel.addElement(d);
    }

    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        frame.setContentPane(root);
        JPanel left = new JPanel(new BorderLayout(8,8));
        left.setPreferredSize(new Dimension(380, 0));
        left.add(buildDestinationForm(), BorderLayout.NORTH);
        left.add(buildDestinationListPanel(), BorderLayout.CENTER);
        root.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.add(buildTripBuilderPanel(), BorderLayout.NORTH);
        right.add(buildTripsTablePanel(), BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outScroll = new JScrollPane(outputArea);
        outScroll.setPreferredSize(new Dimension(0, 120));
        root.add(outScroll, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
    private JPanel buildDestinationForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add Destination"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("City:"), c);
        c.gridx = 1; c.gridy = 0; panel.add(cityField, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Travel Cost (₹):"), c);
        c.gridx = 1; c.gridy = 1; panel.add(travelCostField, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Best Season:"), c);
        c.gridx = 1; c.gridy = 2; panel.add(seasonField, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Rating (0-5):"), c);
        c.gridx = 1; c.gridy = 3; panel.add(ratingField, c);

        JButton addBtn = new JButton("Add Destination");
        addBtn.addActionListener(e -> onAddDestination());
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; panel.add(addBtn, c);

        return panel;
    }

    private JPanel buildDestinationListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Destinations"));

        destJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        destJList.setVisibleRowCount(8);
        JScrollPane jsp = new JScrollPane(destJList);
        panel.add(jsp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> {
            Destination sel = destJList.getSelectedValue();
            if (sel != null) {
                manager.getDestinations().remove(sel);
                destListModel.removeElement(sel);
                appendOutput("Removed destination: " + sel.getCity());
            }
        });
        btns.add(removeBtn);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshDestList());
        btns.add(refreshBtn);

        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTripBuilderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Build Trip"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Pick Destination (select on left)"), c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Nights:"), c);
        c.gridx = 1; c.gridy = 1; panel.add(nightsField, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Hotel per Night (₹):"), c);
        c.gridx = 1; c.gridy = 2; panel.add(hotelPerNightField, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Food per Day (₹):"), c);
        c.gridx = 1; c.gridy = 3; panel.add(foodPerDayField, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton calcBtn = new JButton("Calculate Total");
        calcBtn.addActionListener(e -> onCalculateTotal());
        actions.add(calcBtn);

        JButton addTripBtn = new JButton("Add Trip");
        addTripBtn.addActionListener(e -> onAddTrip());
        actions.add(addTripBtn);

        JButton cheapestBtn = new JButton("Suggest Cheapest");
        cheapestBtn.addActionListener(e -> onSuggestCheapest());
        actions.add(cheapestBtn);

        JButton bestBtn = new JButton("Suggest Best Rated");
        bestBtn.addActionListener(e -> onSuggestBestRated());
        actions.add(bestBtn);

        JButton saveBtn = new JButton("Save Trips to CSV");
        saveBtn.addActionListener(e -> onSaveTrips());
        actions.add(saveBtn);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; panel.add(actions, c);

        return panel;
    }

    private JScrollPane buildTripsTablePanel() {
        JTable tripsTable = new JTable(tripsTableModel);
        JScrollPane jsp = new JScrollPane(tripsTable);
        jsp.setBorder(BorderFactory.createTitledBorder("Saved Trips (Session)"));
        return jsp;
    }
    private void onAddDestination() {
        String city = cityField.getText().trim();
        String costStr = travelCostField.getText().trim();
        String season = seasonField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        if (city.isEmpty() || costStr.isEmpty() || ratingStr.isEmpty()) {
            appendOutput("Please fill City, Travel Cost and Rating.");
            return;
        }
        double cost, rating;
        try {
            cost = Double.parseDouble(costStr);
            rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) throw new NumberFormatException("Rating out of range");
        } catch (NumberFormatException ex) {
            appendOutput("Invalid number: " + ex.getMessage());
            return;
        }

        Destination d = new Destination(city, cost, season.isEmpty() ? "Any" : season, rating);
        manager.addDestination(d);
        destListModel.addElement(d);
        appendOutput("Added destination: " + d.toString());
        cityField.setText("");
        travelCostField.setText("");
        seasonField.setText("");
        ratingField.setText("");
    }

    private void onCalculateTotal() {
        Destination sel = destJList.getSelectedValue();
        if (sel == null) {
            appendOutput("Select a destination from the list to calculate.");
            return;
        }
        Trip t;
        try {
            t = readTripFromInputs(sel);
        } catch (IllegalArgumentException ex) {
            appendOutput("Error: " + ex.getMessage());
            return;
        }
        double total = t.totalCost();
        appendOutput(String.format("Total cost for %s (%d nights): ₹%.2f (travel ₹%.2f + hotel ₹%.2f + food ₹%.2f)",
                sel.getCity(), t.getNights(), total, sel.getTravelCost(),
                t.getHotelPerNight() * t.getNights(), t.getFoodPerDay() * t.getNights()));
    }
    private void onAddTrip() {
        Destination sel = destJList.getSelectedValue();
        if (sel == null) {
            appendOutput("Select a destination first to add a trip.");
            return;
        }
        Trip t;
        try {
            t = readTripFromInputs(sel);
        } catch (IllegalArgumentException ex) {
            appendOutput("Error: " + ex.getMessage());
            return;
        }
        manager.addTrip(t);
        tripsTableModel.addRow(new Object[]{
                sel.getCity(),
                t.getNights(),
                String.format("₹%.2f", t.getHotelPerNight()),
                String.format("₹%.2f", t.getFoodPerDay()),
                String.format("₹%.2f", t.totalCost())
        });
        appendOutput("Trip added for " + sel.getCity() + ". Total: ₹" + String.format("%.2f", t.totalCost()));
    }
    private Trip readTripFromInputs(Destination sel) {
        String nightsS = nightsField.getText().trim();
        String hotelS = hotelPerNightField.getText().trim();
        String foodS = foodPerDayField.getText().trim();
        int nights;
        double hotel, food;
        try {
            nights = Integer.parseInt(nightsS);
            hotel = Double.parseDouble(hotelS);
            food = Double.parseDouble(foodS);
            if (nights < 0) throw new IllegalArgumentException("Nights cannot be negative.");
            if (hotel < 0 || food < 0) throw new IllegalArgumentException("Costs cannot be negative.");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric input.");
        }
        return new Trip(sel, nights, hotel, food);
    }

    private void onSuggestCheapest() {
        Optional<Destination> opt = manager.cheapestDestination();
        if (opt.isPresent()) {
            Destination d = opt.get();
            appendOutput("Cheapest destination: " + d.toString());
            destJList.setSelectedValue(d, true);
        } else {
            appendOutput("No destinations available.");
        }
    }

    private void onSuggestBestRated() {
        Optional<Destination> opt = manager.bestRatedDestination();
        if (opt.isPresent()) {
            Destination d = opt.get();
            appendOutput("Best rated destination: " + d.toString());
            destJList.setSelectedValue(d, true);
        } else {
            appendOutput("No destinations available.");
        }
    }

    private void onSaveTrips() {
        if (manager.getTrips().isEmpty()) {
            appendOutput("No trips to save.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("trips.csv"));
        int res = chooser.showSaveDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                manager.saveTripsToCSV(file);
                appendOutput("Saved " + manager.getTrips().size() + " trips to " + file.getAbsolutePath());
            } catch (IOException ex) {
                appendOutput("Failed to save: " + ex.getMessage());
            }
        } else {
            appendOutput("Save cancelled.");
        }
    }

    private void refreshDestList() {
        destListModel.clear();
        for (Destination d : manager.getDestinations()) destListModel.addElement(d);
        appendOutput("Destination list refreshed.");
    }

    private void appendOutput(String s) {
        outputArea.append(s + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(TravelCompanion::new);
    }
}
