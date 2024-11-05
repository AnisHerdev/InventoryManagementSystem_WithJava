import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// Abstract class for product properties
abstract class Product {
    private int productId;
    private String name;
    private double price;
    private int quantity;
    private Date expiryDate;

    public Product(int productId, String name, double price, int quantity, String expiryDate) throws ParseException {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        this.expiryDate = sdf.parse(expiryDate);
    }

    public abstract void displayProduct();

    // Getter methods
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Date getExpiryDate() { return expiryDate; }
    public boolean isExpired() {
        Date currentDate = new Date();
        return currentDate.after(expiryDate);
    }
}

// Concrete class for grocery items
class GroceryItem extends Product {
    public GroceryItem(int productId, String name, double price, int quantity, String expiryDate) throws ParseException {
        super(productId, name, price, quantity, expiryDate);
    }

    @Override
    public void displayProduct() {
        System.out.println("ID: " + getProductId() + " | Name: " + getName() + " | Price: RS. " + getPrice() +
                " | Quantity: " + getQuantity() + " | Expiry Date: " + new SimpleDateFormat("dd-MM-yyyy").format(getExpiryDate()));
    }
}

// Inventory class using an array of Product objects
class Inventory {
    private Product[] products;
    private int productCount = 0;
    private final int MAX_PRODUCTS = 100;

    public Inventory() {
        products = new Product[MAX_PRODUCTS]; // Array to hold up to 100 products
    }

    public void addProduct(Product product) {
        if (productCount >= MAX_PRODUCTS) {
            System.out.println("Error: Inventory is full.");
            return;
        }
        if (isDuplicateProduct(product.getName(), product.getProductId())) {
            System.out.println("Error: Product with the same ID or name already exists.");
            return;
        }
        products[productCount++] = product;
        System.out.println("Product added successfully!");
    }

    public void removeExpiredProducts() {
        for (int i = 0; i < productCount; i++) {
            if (products[i] != null && products[i].isExpired()) {
                System.out.println("Removing expired product: " + products[i].getName());
                products[i] = null; // Mark the slot as empty
            }
        }
        compactArray();
        System.out.println("Expired products have been removed.");
    }

    public Product findProductById(int productId) {
        for (int i = 0; i < productCount; i++) {
            if (products[i] != null && products[i].getProductId() == productId) {
                return products[i];
            }
        }
        return null;
    }

    public boolean isDuplicateProduct(String name, int productId) {
        for (int i = 0; i < productCount; i++) {
            if (products[i] != null && (products[i].getProductId() == productId || products[i].getName().equalsIgnoreCase(name))) {
                return true;
            }
        }
        return false;
    }

    public void displayAllProducts() {
        boolean hasProducts = false;
        for (int i = 0; i < productCount; i++) {
            if (products[i] != null) {
                products[i].displayProduct();
                hasProducts = true;
            }
        }
        if (!hasProducts) {
            System.out.println("No products available in inventory.");
        }
    }

    // Helper method to remove nulls from the array after removing expired products
    private void compactArray() {
        int shiftIndex = 0;
        for (int i = 0; i < productCount; i++) {
            if (products[i] != null) {
                products[shiftIndex++] = products[i];
            }
        }
        // Set the remaining elements to null
        for (int i = shiftIndex; i < productCount; i++) {
            products[i] = null;
        }
        productCount = shiftIndex; // Update the product count
    }
}

// Billing class to handle purchases and bill generation
class Billing {
    private Inventory inventory;

    public Billing(Inventory inventory) {
        this.inventory = inventory;
    }

    public void processOrder() {
        Scanner scanner = new Scanner(System.in);
        double totalBill = 0.0;

        while (true) {
            System.out.println("Enter Product ID to buy (or -1 to finish): ");
            int productId = 0;
            try {
                productId = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number for Product ID.");
                scanner.next(); // Clear the invalid input
                continue;
            }

            if (productId == -1) break;

            Product product = inventory.findProductById(productId);
            if (product == null) {
                System.out.println("Product not found.");
                continue;
            }

            if (product.isExpired()) {
                System.out.println("Product is expired and cannot be sold.");
                continue;
            }

            System.out.println("Enter quantity to buy: ");
            int quantity = 0;
            try {
                quantity = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number for quantity.");
                scanner.next(); // Clear the invalid input
                continue;
            }

            if (quantity > product.getQuantity()) {
                System.out.println("Insufficient stock. Available quantity: " + product.getQuantity());
            } else {
                product.setQuantity(product.getQuantity() - quantity);
                totalBill += product.getPrice() * quantity;
                System.out.println("Added " + product.getName() + " to your bill.");
            }
        }

        // Display the total bill
        System.out.println("\nYour Bill:");
        System.out.println("Total Amount: $" + totalBill);

        // Remove expired products after the purchase
        inventory.removeExpiredProducts();
    }
}

// Main class for the inventory management system
public class InventoryManagementSystem {
    public static void main(String[] args) {
        try {
            Inventory inventory = new Inventory();
            Scanner scanner = new Scanner(System.in);

            try{
                File myFileObj = new File("inventory.txt");
                Scanner myData = new Scanner(myFileObj);
                while(myData.hasNextLine()){
                    String[] product = myData.nextLine().split(",");
                    inventory.addProduct(new GroceryItem(Integer.parseInt(product[0]), product[1], Double.parseDouble(product[2]), Integer.parseInt(product[3]), product[4]));
                }
                myData.close();
            }catch (FileNotFoundException e){
                System.out.println("An error getting data occurred.");
                e.printStackTrace();
            }

            Billing billing = new Billing(inventory);
            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Display Inventory");
                System.out.println("2. Add Product");
                System.out.println("3. Generate Bill");
                System.out.println("4. Remove Expired Products");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                int choice = 0;
                try {
                    choice = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                    scanner.next(); // Clear the invalid input
                    continue;
                }

                switch (choice) {
                    case 1:
                        inventory.displayAllProducts();
                        break;
                    case 2:
                        int id = 0;
                        try {
                            System.out.print("Enter Product ID: ");
                            id = scanner.nextInt();
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please enter a valid number for Product ID.");
                            scanner.next(); // Clear the invalid input
                            continue;
                        }
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter Product Name: ");
                        String name = scanner.nextLine();
                        double price = 0.0;
                        try {
                            System.out.print("Enter Product Price: ");
                            price = scanner.nextDouble();
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please enter a valid number for Product Price.");
                            scanner.next(); // Clear the invalid input
                            continue;
                        }
                        int quantity = 0;
                        try {
                            System.out.print("Enter Product Quantity: ");
                            quantity = scanner.nextInt();
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please enter a valid number for Product Quantity.");
                            scanner.next(); // Clear the invalid input
                            continue;
                        }
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter Expiry Date (dd-MM-yyyy): ");
                        String expiryDate = scanner.nextLine();
                        try {
                            Product product = new GroceryItem(id, name, price, quantity, expiryDate);
                            inventory.addProduct(product);
                        } catch (ParseException e) {
                            System.out.println("Invalid date format. Please try again.");
                        }
                        break;
                    case 3:
                        billing.processOrder();
                        break;
                    case 4:
                        inventory.removeExpiredProducts();
                        break;
                    case 5:
                        System.out.println("Exiting the system. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
