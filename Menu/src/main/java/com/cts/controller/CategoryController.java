package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.CategoryDto;
import com.cts.service.CategoryService;
import com.cts.validation.Validation.Create;
import com.cts.validation.Validation.Update;

@RestController
@RequestMapping("/api/category")

public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	ResponseEntity<List<CategoryDto>> getAll() {
		List<CategoryDto> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
		CategoryDto category = categoryService.getCategoryById(id);
		return ResponseEntity.ok(category);
	}
	@GetMapping("name/{name}")
	public ResponseEntity<CategoryDto> getById(@PathVariable String name) {
		CategoryDto category = categoryService.getCategoryByName(name);
		return ResponseEntity.ok(category);
	}

	@PreAuthorize("hasAnyRole('VENDOR','ADMIN')")
	@PostMapping
	public ResponseEntity<CategoryDto> create(@Validated(Create.class) @RequestBody CategoryDto dto) {
		CategoryDto created = categoryService.createCategory(dto);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('VENDOR','ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<CategoryDto> update(@PathVariable Long id,
			@Validated(Update.class) @RequestBody CategoryDto dto) {
		CategoryDto updated = categoryService.updateCategory(id, dto);
		return new ResponseEntity<>(updated, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('VENDOR','ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
