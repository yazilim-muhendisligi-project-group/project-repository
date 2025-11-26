package erciyes.edu.tr.bahar19;

import erciyes.edu.tr.bahar19.Model.Product;
import erciyes.edu.tr.bahar19.Model.OrderItem;
import erciyes.edu.tr.bahar19.Model.Order;
import erciyes.edu.tr.bahar19.Model.InventoryItem;
import erciyes.edu.tr.bahar19.Model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MockDataUtility {

    // AKTİF VERİ (CONTROLLER SIMÜLASYONU)
    private static Map<Integer, Order> liveMockOrders = new HashMap<>(); // Aktif masalar

    // YENİ ALAN: Tamamlanmış (kapatılmış) siparişlerin kaydı (Z Raporu)
    private static List<Order> completedOrders = new ArrayList<>();

    // MODEL VERİSİ
    private static Map<String, Product> productMap;
    private static Map<String, InventoryItem> inventoryMap;

    // ------------------- INVENTORY ITEM (STOK) MOCKING -------------------

    public static List<InventoryItem> getMockInventoryList() {
        if (inventoryMap == null) {
            inventoryMap = new HashMap<>();
            // Fiziksel Stoklar (KG, ADET, KUTU bazında)
            inventoryMap.put("Paket Cay", new InventoryItem(101, "Paket Çay", 20, "KG", 5));
            inventoryMap.put("Öğütülmüş Kahve", new InventoryItem(102, "Öğütülmüş Kahve", 15, "KG", 3));
            inventoryMap.put("Kutu Oralet Tozu", new InventoryItem(103, "Kutu Oralet Tozu", 10, "Kutu", 2));
            inventoryMap.put("Süt", new InventoryItem(104, "Kutu Süt", 30, "ADET", 5));
            inventoryMap.put("Şeker", new InventoryItem(105, "Şeker", 50, "KG", 10));
        }
        return new ArrayList<>(inventoryMap.values());
    }

    // ------------------- PRODUCT (MENÜ) MOCKING -------------------

    public static List<Product> getMockProductsList() {
        if (productMap == null) {
            getMockInventoryList();

            List<Product> products = new ArrayList<>();
            Product p1 = new Product(); p1.setname("Çay"); p1.setPrice(10.0); products.add(p1);
            Product p2 = new Product(); p2.setname("Oralet"); p2.setPrice(7.0); products.add(p2);
            Product p3 = new Product(); p3.setname("Türk Kahvesi"); p3.setPrice(15.0); products.add(p3);
            Product p4 = new Product(); p4.setname("Irish Coffee"); p4.setPrice(30.0); products.add(p4);
            Product p5 = new Product(); p5.setname("Ihlamur"); p5.setPrice(12.0); products.add(p5);
            Product p6 = new Product(); p6.setname("Kaçak Çay"); p6.setPrice(15.0); products.add(p6);
            Product p7 = new Product(); p7.setname("Bubble Tea"); p7.setPrice(20.0); products.add(p7);

            productMap = products.stream().collect(Collectors.toMap(Product::getname, p -> p));

            // TARİF EKLEME
            productMap.get("Çay").addRecipe(new Recipe(inventoryMap.get("Paket Cay"), 0.005));
            productMap.get("Türk Kahvesi").addRecipe(new Recipe(inventoryMap.get("Öğütülmüş Kahve"), 0.01));

            // Simülasyon verisi ekleyelim, böylece Z raporu hemen boş gelmez.
            getCompletedOrders();
        }
        return new ArrayList<>(productMap.values());
    }

    // ------------------- ORDER (SİPARİŞ) MOCKING -------------------

    public static Order getMockOrder(int tableNumber) {
        if (liveMockOrders.containsKey(tableNumber)) {
            return liveMockOrders.get(tableNumber);
        }

        Order mockOrder = new Order();
        mockOrder.setTableNumber(tableNumber);

        Map<String, Product> productMap = getMockProductsMap();

        // KRİTİK DÜZELTME: Tüm masalar için başlangıç simülasyon ürünlerini ekliyoruz.
        if (productMap.containsKey("Irish Coffee")) mockOrder.addItem(new OrderItem(productMap.get("Irish Coffee"), 2));
        if (productMap.containsKey("Oralet")) mockOrder.addItem(new OrderItem(productMap.get("Oralet"), 5));
        if (productMap.containsKey("Bubble Tea")) mockOrder.addItem(new OrderItem(productMap.get("Bubble Tea"), 1));

        mockOrder.calculateTotalAmount();

        liveMockOrders.put(tableNumber, mockOrder);
        return mockOrder;
    }

    public static Order addOrderItemToMockOrder(int tableNumber, Product product, int quantity) {
        Order order = getMockOrder(tableNumber);
        OrderItem newItem = new OrderItem(product, quantity);
        order.addItem(newItem);
        return order;
    }

    public static void closeOrder(int tableNumber) {
        if (liveMockOrders.containsKey(tableNumber)) {
            Order completedOrder = liveMockOrders.remove(tableNumber);

            // YENİ İŞLEV: Kapanan siparişi rapora ekle
            completedOrders.add(completedOrder);
            System.out.println("MOCK: Masa " + tableNumber + " siparişi kapatıldı ve rapora eklendi.");
        }
    }

    // ------------------- RAPOR (Z RAPORU) MOCKING -------------------

    public static List<Order> getCompletedOrders() {
        // Başlangıç Mock verisi ekle
        if (completedOrders.isEmpty()) {
            Order mock1 = new Order(); mock1.setId(1001); mock1.setTableNumber(99); mock1.setTotalAmount(150.00);
            Order mock2 = new Order(); mock2.setId(1002); mock2.setTableNumber(98); mock2.setTotalAmount(75.50);
            completedOrders.add(mock1);
            completedOrders.add(mock2);
        }
        return completedOrders;
    }

    // Yardımcı metot
    private static Map<String, Product> getMockProductsMap() {
        if (productMap == null) {
            getMockProductsList();
        }
        return productMap;
    }
}