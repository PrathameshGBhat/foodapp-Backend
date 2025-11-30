package com.cts.service;

import java.util.List;

import com.cts.dto.CategoryDto;

public interface CategoryService {
	List<CategoryDto> getAllCategories();
	CategoryDto getCategoryById(Long id);
	CategoryDto getCategoryByName(String name);
	CategoryDto createCategory(CategoryDto dto);
	CategoryDto updateCategory(Long id, CategoryDto dto);
	void deleteCategory(Long id);
}
