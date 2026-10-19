package com.shop.product;

import java.net.URI;
import java.util.List;

import com.shop.product.ProductDtos.CreateProductRequest;
import com.shop.product.ProductDtos.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ProductResponse> list() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{skuCode}")
    public ProductResponse get(@PathVariable String skuCode) {
        return repository.findBySkuCode(skuCode)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + skuCode));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        if (repository.existsBySkuCode(req.skuCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists: " + req.skuCode());
        }
        Product saved = repository.save(new Product(req.skuCode(), req.name(), req.description(), req.price()));
        return ResponseEntity.created(URI.create("/api/products/" + saved.getSkuCode()))
                .body(ProductResponse.from(saved));
    }
}
