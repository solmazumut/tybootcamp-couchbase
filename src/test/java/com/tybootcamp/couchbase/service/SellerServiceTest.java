package com.tybootcamp.couchbase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tybootcamp.couchbase.domain.Product;
import com.tybootcamp.couchbase.domain.Seller;
import com.tybootcamp.couchbase.repository.SellerRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SellerServiceTest {

  @Autowired
  private SellerService sellerService;
  @Autowired
  private SellerRepository sellerRepository;


  /**
   * Fetches a document by it's id. Therefore it does not need any additional secondary index.
   */
  @Test
  @Order(1)
  public void findById() throws InterruptedException{
    //Given
    sellerRepository.deleteByName("myShop"); //to be make sure of uniqueness
    Thread.sleep(50);

    Seller createdSeller = sellerService.create("myShop");

    //When
    Seller foundSeller = sellerService.findById(createdSeller.getId());

    //Then
    assertEquals("myShop", foundSeller.getName());
  }

  /**
   * This test does operations with querying the 'name' field. For this example we need a secondary
   * index to query.
   * <p>
   * You can create the index by 'CREATE INDEX idx_sellers_name on sellers(name)'.
   */
  @Test
  @Order(2)
  public void findByName() throws InterruptedException {
    //Given
    sellerRepository.deleteByName("myShop"); //to be make sure of uniqueness
    Thread.sleep(100);

    Seller myShop = sellerService.create("myShop");

    Thread.sleep(50);
    //When
    Seller foundSeller = sellerService.findByName("myShop");

    //Then
    assertEquals(myShop.getId(), foundSeller.getId());
  }

  /**
   * Let's say that we will only allow sellers with unique names.
   * <p>
   * Throw an exception when create method is called twice with the same argument. You must also add
   * a meaningful error message as "There is already a shop named as: %s"
   */
  @Test
  @Order(3)
  public void ensureSellerNameUniqueness() throws InterruptedException{
    //Given
    sellerRepository.deleteByName("myShop"); //clean up first
    Thread.sleep(50);
    sellerService.create("myShop"); //first time
    Thread.sleep(50);
    //When
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        sellerService.create("myShop") //second time
    );

    //Then
    assertEquals("There is already a shop named as: myshop", exception.getMessage());
  }

  /**
   * Make myShop sell some items.
   * <p>
   * Remember that on creation, the seller does not sell any items.
   */
  @Test
  @Order(4)
  public void addProducts() throws InterruptedException {
    //Given
    sellerRepository.deleteByName("myShop"); //clean up first
    Thread.sleep(50);
    sellerService.create("myShop");
    Thread.sleep(50);
    List<Product> products = List.of(
        new Product("glasses", 10.5),
        new Product("shirt", 5.0)
    );

    //When
    sellerService.addProductsToSeller("myShop", products);
    Thread.sleep(50);
    //Then
    Seller myShop = sellerService.findByName("myShop");
    assertEquals(2, myShop.getProducts().size());
    assertTrue(myShop.getProducts().stream()
        .map(Product::getName)
        .collect(Collectors.toList())
        .containsAll(List.of("glasses", "shirt")));
  }


  /**
   * does the function work when we add more than one
   */
  @Test
  @Order(5)
  public void addProductsTwoTimes() throws InterruptedException {
    //Given
    sellerRepository.deleteByName("myShop"); //clean up first
    Thread.sleep(50);
    sellerService.create("myShop");
    Thread.sleep(50);
    List<Product> products = List.of(
            new Product("glasses", 10.5),
            new Product("shirt", 5.0)
    );

    List<Product> products2 = List.of(
            new Product("phone", 100.5),
            new Product("computer", 200.0),
            new Product("book", 40.0)
    );


    //When
    sellerService.addProductsToSeller("myShop", products);
    Thread.sleep(50);
    sellerService.addProductsToSeller("myShop", products2);
    Thread.sleep(50);
    //Then
    Seller myShop = sellerService.findByName("myShop");
    assertEquals(5, myShop.getProducts().size());
    assertTrue(myShop.getProducts().stream()
            .map(Product::getName)
            .collect(Collectors.toList())
            .containsAll(List.of("glasses", "shirt", "phone", "computer", "book")));
  }


  /**
   * Implement a category filter for seller's products.
   * <p>
   * In this example we want to query the products by their categories. According to When and Then
   * statements. The following result is expected.
   * <p>
   * category1 -> ["glasses", "shirt"] category2 -> ["keyboard", "monitor"]
   */
  @Test
  @Order(6)
  public void getProductsByCategory() throws InterruptedException {
    //Given
    sellerRepository.deleteByName("myShop"); //clean up first
    Thread.sleep(50);
    Seller myShop = sellerService.create("myShop");

    // TODO: Add products ["glasses", "shirt", "monitor", "keyboard"] to myShop here
    // You need to find a way to query them with their categories.
    // Think about the performance on scale

    // ------------------ implementation
    List<Product> category1 = List.of(
            new Product("glasses", 130.5),
            new Product("shirt", 55.0)
    );

    List<Product> category2 = List.of(
            new Product("monitor", 1300.5),
            new Product("keyboard", 250.0)
    );
    Thread.sleep(50);
    sellerService.addProductsToSeller("myShop", category1, "category1");
    Thread.sleep(50);
    sellerService.addProductsToSeller("myShop", category2, "category2");
    Thread.sleep(50);
    // ------------------ implementation

    //When
    List<Product> productsByCategory1 = sellerService.getProductsByCategory("myShop", "category1");
    List<Product> productsByCategory2 = sellerService.getProductsByCategory("myShop", "category2");

    //Then
    assertEquals(2, productsByCategory1.size());
    assertTrue(productsByCategory1.stream()
        .map(Product::getName)
        .collect(Collectors.toList())
        .containsAll(List.of("glasses", "shirt")));
    assertEquals(2, productsByCategory2.size());
    assertTrue(productsByCategory2.stream()
        .map(Product::getName)
        .collect(Collectors.toList())
        .containsAll(List.of("monitor", "keyboard")));
  }
}