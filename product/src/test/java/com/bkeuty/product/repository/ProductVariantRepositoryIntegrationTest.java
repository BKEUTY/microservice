package com.bkeuty.product.repository;

import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductVariantRepositoryIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private Product product;
    private ProductVariant activeVariantWithStock;
    private ProductVariant inactiveVariant;
    private ProductVariant activeVariantNoStock;

    @BeforeEach
    void seedData() {
        product = Product.builder()
                .name("Sua rua mat Centella")
                .description("Sua rua mat diu nhe cho da nhay cam")
                .build();
        entityManager.persist(product);

        activeVariantWithStock = ProductVariant.builder()
                .product(product)
                .productVariantName("Centella 100ml")
                .price(new BigDecimal("150000"))
                .promotionPrice(new BigDecimal("120000"))
                .stockQuantity(50)
                .status(ProductStatus.ACTIVE)
                .sold(10)
                .build();
        entityManager.persist(activeVariantWithStock);

        inactiveVariant = ProductVariant.builder()
                .product(product)
                .productVariantName("Centella 50ml (Ngung ban)")
                .price(new BigDecimal("90000"))
                .stockQuantity(20)
                .status(ProductStatus.INACTIVE)
                .sold(0)
                .build();
        entityManager.persist(inactiveVariant);

        activeVariantNoStock = ProductVariant.builder()
                .product(product)
                .productVariantName("Centella 200ml (Het hang)")
                .price(new BigDecimal("280000"))
                .stockQuantity(0)
                .status(ProductStatus.ACTIVE)
                .sold(5)
                .build();
        entityManager.persist(activeVariantNoStock);

        entityManager.flush();
    }

    @Test
    void findAllByProductId_ShouldReturnAllVariantsForProduct() {
        List<ProductVariant> variants = productVariantRepository.findAllByProductId(product.getId());
        assertEquals(3, variants.size(), "Should return all 3 variants of the product");
    }

    @Test
    void findActiveVariantsWithStock_ShouldReturnOnlyActiveAndInStockVariants() {
        List<ProductVariant> activeInStock = productVariantRepository.findActiveVariantsWithStock(PageRequest.of(0, 10));
        assertEquals(1, activeInStock.size());
        assertEquals("Centella 100ml", activeInStock.get(0).getProductVariantName(),
                "Only the 100ml variant is active and has stock > 0");
    }

    @Test
    void decreaseStockAndIncreaseSold_ShouldModifyDatabaseValuesCorrectly_WhenStockIsSufficient() {
        int updatedRows = productVariantRepository.decreaseStockAndIncreaseSold(activeVariantWithStock.getId(), 5);
        assertEquals(1, updatedRows, "Should update exactly 1 row");

        entityManager.clear(); // Clear persist context to read from H2 DB

        Optional<ProductVariant> updatedOpt = productVariantRepository.findById(activeVariantWithStock.getId());
        assertTrue(updatedOpt.isPresent());
        ProductVariant updated = updatedOpt.get();
        assertEquals(45, updated.getStockQuantity(), "Stock should decrease by 5 (50 -> 45)");
        assertEquals(15, updated.getSold(), "Sold count should increase by 5 (10 -> 15)");
    }

    @Test
    void decreaseStockAndIncreaseSold_ShouldNotModify_WhenStockIsInsufficient() {
        int updatedRows = productVariantRepository.decreaseStockAndIncreaseSold(activeVariantWithStock.getId(), 100);
        assertEquals(0, updatedRows, "Should not update any rows since stock is 50 < 100");

        entityManager.clear();

        ProductVariant unchanged = productVariantRepository.findById(activeVariantWithStock.getId()).orElseThrow();
        assertEquals(50, unchanged.getStockQuantity());
        assertEquals(10, unchanged.getSold());
    }

    @Test
    void findFirstByProductVariantName_ShouldReturnVariant_WhenNameMatches() {
        Optional<ProductVariant> found = productVariantRepository.findFirstByProductVariantName("Centella 100ml");
        assertTrue(found.isPresent());
        assertEquals(activeVariantWithStock.getId(), found.get().getId());
    }
}
